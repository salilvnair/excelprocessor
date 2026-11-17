package com.github.salilvnair.excelprocessor.v2.service;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Row-at-a-time XLSX reader. Sheet XML is pulled with StAX straight from the package, so the POI
 * in-memory model (XMLBeans) is never built: memory stays flat regardless of the number of rows and
 * reading is several times faster than {@link ExcelSheetReader} for large files.
 *
 * <pre>{@code
 * try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file, context);
 *      Stream<OrderSheet> rows = reader.stream(OrderSheet.class)) {
 *     rows.forEach(this::process);
 * }
 * }</pre>
 *
 * Supported: horizontal sheets with fixed {@code @Cell} headers or {@code DynamicHeaderSheet}
 * dynamic headers, header/value row and column ranges, ignored headers/columns, duplicate headers
 * and fill colours ({@code cells()}). Formulas yield their cached result. Not supported: vertical,
 * sectional and merged-header sheets. Not thread-safe; one stream at a time.
 */
public interface StreamingExcelSheetReader extends AutoCloseable {

    /** Lazily reads the rows of {@code sheetClass}'s sheet. Close the stream (or this reader) when done. */
    <T extends BaseSheet> Stream<T> stream(Class<T> sheetClass);

    /** Reads every row and hands it to {@code consumer}; returns the number of rows read. */
    default <T extends BaseSheet> long forEach(Class<T> sheetClass, Consumer<? super T> consumer) {
        long[] count = {0};
        try (Stream<T> rows = stream(sheetClass)) {
            rows.forEach(row -> {
                consumer.accept(row);
                count[0]++;
            });
        }
        return count[0];
    }

    /** Convenience: reads all rows into a list (holds every row in memory). */
    default <T extends BaseSheet> List<T> readAll(Class<T> sheetClass) {
        List<T> rows = new ArrayList<>();
        forEach(sheetClass, rows::add);
        return rows;
    }

    /** Closes the underlying package. Idempotent, never throws. */
    @Override
    void close();
}
