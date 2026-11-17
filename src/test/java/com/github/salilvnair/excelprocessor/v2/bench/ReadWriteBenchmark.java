package com.github.salilvnair.excelprocessor.v2.bench;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Throughput benchmark (tagged "load"): 50k rows x 10 columns, fixed and dynamic headers.
 * Run with {@code mvn test -Dsurefire.excludedGroups= -Dgroups=load -Dtest=ReadWriteBenchmark}.
 */
@Tag("load")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReadWriteBenchmark {
    static final int ROWS = Integer.getInteger("bench.rows", 50_000);
    static final File FILE = new File("target/bench.xlsx");

    private static long time(String label, Runnable r) {
        r.run();                                   // warm-up
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            long t = System.nanoTime();
            r.run();
            best = Math.min(best, System.nanoTime() - t);
        }
        System.out.printf("BENCH %-28s %6d ms%n", label, best / 1_000_000);
        return best;
    }

    private static List<BenchSheet> rows() {
        List<BenchSheet> rows = new ArrayList<>(ROWS);
        for (int i = 0; i < ROWS; i++) rows.add(BenchSheet.row(i));
        return rows;
    }

    @Test
    @Order(1)
    void writeFixed() throws Exception {
        List<BenchSheet> rows = rows();
        time("write fixed (XSSF)", () -> {
            try (OutputStream out = OutputStream.nullOutputStream()) {
                ExcelSheetWriterFactory.generate().write(rows, ExcelSheetContext.builder().build(), out);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        try (FileOutputStream out = new FileOutputStream(FILE)) {
            ExcelSheetWriterFactory.generate().write(rows, ExcelSheetContext.builder().build(), out);
        }
    }

    @Test
    @Order(2)
    void readFixed() {
        Supplier<List<BenchSheet>> read = () -> ExcelSheetReaderFactory.generate()
                .read(BenchSheet.class, ExcelSheetContext.builder().excelFile(FILE).build());
        time("read fixed", () -> assertEquals(ROWS, read.get().size()));
    }

    @Test
    @Order(3)
    void readDynamic() {
        Supplier<List<BenchDynamicSheet>> read = () -> ExcelSheetReaderFactory.generate()
                .read(BenchDynamicSheet.class, ExcelSheetContext.builder().excelFile(FILE).build());
        time("read dynamic", () -> assertEquals(ROWS, read.get().size()));
    }

    @Test
    @Order(4)
    void readFixedConcurrent() {
        Supplier<List<BenchSheet>> read = () -> ExcelSheetReaderFactory.generate(true, 5_000)
                .read(BenchSheet.class, ExcelSheetContext.builder().excelFile(FILE).build());
        time("read fixed concurrent", () -> assertEquals(ROWS, read.get().size()));
    }

    @Test
    @Order(5)
    void writeFixedStreaming() {
        List<BenchSheet> rows = rows();
        time("write fixed (streaming)", () -> {
            try (com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
                writer.beginSheet(BenchSheet.class);
                rows.forEach(writer::writeRow);
                writer.finish(OutputStream.nullOutputStream());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    @Order(6)
    void readFixedStreaming() {
        time("read fixed (streaming)", () -> {
            try (com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(FILE)) {
                assertEquals(ROWS, reader.forEach(BenchSheet.class, r -> { }));
            }
        });
    }

    @Test
    @Order(7)
    void readDynamicStreaming() {
        time("read dynamic (streaming)", () -> {
            try (com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(FILE)) {
                assertEquals(ROWS, reader.forEach(BenchDynamicSheet.class, r -> { }));
            }
        });
    }

    @SuppressWarnings("unused")
    private static int count(List<? extends BaseSheet> l) { return l.size(); }
}
