package com.github.salilvnair.excelprocessor.v2.writer;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.StyledDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.writer.sheet.StyledStreamingDynamicSheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Large exports. Excluded from the default build; run with
 * {@code mvn test -Dsurefire.excludedGroups= -Dgroups=load}.
 */
@Tag("load")
class LargeExportLoadTest {

    private static final int COLUMNS = 10;

    private static LinkedHashMap<String, Object> row(int i) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (int c = 0; c < COLUMNS; c++) {
            m.put("Col" + c, c % 2 == 0 ? (Object) ("v" + i + "_" + c) : (Object) (double) (i * c));
        }
        return m;
    }

    /** The original failure: > 64,000 styled dynamic cells used to exhaust the XLSX style table. */
    @Test
    void listBasedDynamicSheetBeyondTheOldStyleLimit() {
        List<StyledDynamicSheet> rows = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {       // 100,000 styled cells
            StyledDynamicSheet s = new StyledDynamicSheet();
            s.setDynamicHeaderKeyedCellValueMap(row(i));
            rows.add(s);
        }
        Workbook wb = ExcelSheetWriterFactory.generate().workbook(rows, ExcelSheetContext.builder().build());
        assertTrue(wb.getNumCellStyles() < 10, "styles=" + wb.getNumCellStyles());
        assertEquals(10_000, wb.getSheet("Dynamic").getLastRowNum());
    }

    /** 200k rows x 10 columns, rows generated lazily and never retained. */
    @Test
    void rowStreaming200kRows() throws Exception {
        long start = System.nanoTime();
        Runtime rt = Runtime.getRuntime();
        List<String> headers = new ArrayList<>(row(0).keySet());
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
            writer.beginSheet(StyledStreamingDynamicSheet.class, headers);
            for (int i = 0; i < 200_000; i++) {
                StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
                s.setDynamicHeaderKeyedCellValueMap(row(i));
                writer.writeRow(s);
            }
            assertTrue(writer.workbook().getNumCellStyles() < 10, "styles=" + writer.workbook().getNumCellStyles());
            writer.finish(OutputStream.nullOutputStream());
        }
        System.out.printf("200k rows streamed in %d ms, used heap after: %d MB%n",
                (System.nanoTime() - start) / 1_000_000, (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
    }

    /** Write 300k rows with the streaming writer, then read them back with the streaming reader. */
    @Test
    void roundTrip300kRowsWithFlatMemory(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        java.nio.file.Path file = dir.resolve("big.xlsx");
        List<String> headers = new ArrayList<>(row(0).keySet());
        try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
            writer.beginSheet(StyledStreamingDynamicSheet.class, headers);
            for (int i = 0; i < 300_000; i++) {
                StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
                s.setDynamicHeaderKeyedCellValueMap(row(i));
                writer.writeRow(s);
            }
            writer.finish(file);
        }
        long start = System.nanoTime();
        long[] sum = {0};
        try (com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader reader =
                     com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory.openStreaming(file.toFile())) {
            long n = reader.forEach(StyledStreamingDynamicSheet.class, r -> sum[0] += ((Double) r.extract().get("Col1")).longValue());
            assertEquals(300_000, n);
        }
        Runtime rt = Runtime.getRuntime();
        System.out.printf("300k rows streamed back in %d ms, used heap after: %d MB%n",
                (System.nanoTime() - start) / 1_000_000, (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
        assertEquals(44_999_850_000L, sum[0]);   // sum of i*1 for i < 300k
    }

    /** Several concurrent exports, each with its own writer and workbook. */
    @Test
    void concurrentStreamingExports() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Long>> results = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                results.add(pool.submit(() -> {
                    try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming()) {
                        writer.beginSheet(StyledStreamingDynamicSheet.class, new ArrayList<>(row(0).keySet()));
                        for (int i = 0; i < 50_000; i++) {
                            StyledStreamingDynamicSheet s = new StyledStreamingDynamicSheet();
                            s.setDynamicHeaderKeyedCellValueMap(row(i));
                            writer.writeRow(s);
                        }
                        writer.finish(OutputStream.nullOutputStream());
                        return writer.totalRowsWritten();
                    }
                }));
            }
            for (Future<Long> r : results) {
                assertEquals(50_000L, r.get(5, TimeUnit.MINUTES));
            }
        }
        finally {
            pool.shutdownNow();
        }
    }
}
