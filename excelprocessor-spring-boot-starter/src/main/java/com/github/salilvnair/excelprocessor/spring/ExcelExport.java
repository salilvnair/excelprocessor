package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.csv.CsvSheetWriter;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A spreadsheet download written row by row straight to the HTTP response, so neither the rows
 * nor the file are held in memory. Return it from a Spring MVC handler method:
 *
 * <pre>{@code
 * @GetMapping("/orders.xlsx")
 * ExcelExport<OrderSheet> export() {
 *     return ExcelExport.of(OrderSheet.class, orderRepository::streamAll).fileName("orders.xlsx");
 * }
 * }</pre>
 *
 * The rows supplier is called once, while the response is written; a returned {@link Stream} is
 * closed afterwards (so database cursors are released). Without Spring MVC, call
 * {@link #writeTo(OutputStream)} yourself.
 *
 * @param <T> row type
 */
public final class ExcelExport<T extends BaseSheet> {
    public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String CSV_CONTENT_TYPE = "text/csv";

    private final Class<T> sheetClass;
    private final Supplier<? extends Stream<? extends T>> rows;
    private String fileName;
    private List<String> dynamicHeaders;
    private CsvFormat csvFormat;
    private ExcelSheetContext context;

    private ExcelExport(Class<T> sheetClass, Supplier<? extends Stream<? extends T>> rows) {
        this.sheetClass = Objects.requireNonNull(sheetClass, "sheetClass");
        this.rows = Objects.requireNonNull(rows, "rows");
    }

    /** Rows from a stream supplier, e.g. a repository method returning {@code Stream<T>}. */
    public static <T extends BaseSheet> ExcelExport<T> of(Class<T> sheetClass, Supplier<? extends Stream<? extends T>> rows) {
        return new ExcelExport<>(sheetClass, rows);
    }

    public static <T extends BaseSheet> ExcelExport<T> of(Class<T> sheetClass, Iterable<? extends T> rows) {
        Objects.requireNonNull(rows, "rows");
        return new ExcelExport<>(sheetClass, () -> {
            List<T> list = new ArrayList<>();
            Iterator<? extends T> iterator = rows.iterator();
            iterator.forEachRemaining(list::add);
            return list.stream();
        });
    }

    /** Download file name; defaults to the sheet name with .xlsx or .csv. */
    public ExcelExport<T> fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /** Ordered headers for {@code DynamicHeaderSheet} rows. */
    public ExcelExport<T> headers(List<String> orderedHeaders) {
        this.dynamicHeaders = orderedHeaders == null ? null : new ArrayList<>(orderedHeaders);
        return this;
    }

    /** Write CSV instead of XLSX. */
    public ExcelExport<T> csv(CsvFormat format) {
        this.csvFormat = format == null ? CsvFormat.csv() : format;
        return this;
    }

    /** Context for the XLSX writer (style template, password, valid values for dropdowns...). */
    public ExcelExport<T> context(ExcelSheetContext context) {
        this.context = context;
        return this;
    }

    public Class<T> sheetClass() {
        return sheetClass;
    }

    public boolean isCsv() {
        return csvFormat != null;
    }

    public String contentType() {
        return isCsv() ? CSV_CONTENT_TYPE + ";charset=" + csvFormat.charset().name() : XLSX_CONTENT_TYPE;
    }

    public String fileName() {
        if (fileName != null) {
            return fileName;
        }
        com.github.salilvnair.excelprocessor.v2.annotation.Sheet sheet = sheetClass.getAnnotation(com.github.salilvnair.excelprocessor.v2.annotation.Sheet.class);
        String base = sheet != null && !sheet.value().isEmpty() ? sheet.value() : "export";
        return base + (isCsv() ? ".csv" : ".xlsx");
    }

    /** Writes the whole export; {@code out} is flushed, not closed. */
    public void writeTo(OutputStream out) throws IOException {
        writeTo(out, null);
    }

    /**
     * Writes the whole export; {@code out} is flushed, not closed. {@code defaultContext} is used
     * for XLSX when no {@link #context(ExcelSheetContext)} was set.
     */
    public void writeTo(OutputStream out, ExcelSheetContext defaultContext) throws IOException {
        try (Stream<? extends T> stream = rows.get()) {
            if (isCsv()) {
                CsvSheetWriter writer = new CsvSheetWriter(new NonClosingOutputStream(out), csvFormat);
                begin(writer);
                stream.forEach(writer::writeRow);
                writer.close();
            }
            else {
                ExcelSheetContext ctx = context != null ? context : defaultContext;
                try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(ctx == null ? ExcelSheetContext.builder().build() : ctx)) {
                    begin(writer);
                    stream.forEach(writer::writeRow);
                    writer.finish(out);
                }
            }
        }
        out.flush();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void begin(StreamingExcelSheetWriter writer) {
        if (dynamicHeaders != null) {
            writer.beginSheet((Class) sheetClass.asSubclass(DynamicHeaderSheet.class), dynamicHeaders);
        }
        else {
            writer.beginSheet(sheetClass);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void begin(CsvSheetWriter writer) {
        if (dynamicHeaders != null) {
            writer.begin((Class) sheetClass.asSubclass(DynamicHeaderSheet.class), dynamicHeaders);
        }
        else {
            writer.begin(sheetClass);
        }
    }

    /** The servlet container owns the response stream. */
    private static final class NonClosingOutputStream extends FilterOutputStream {
        NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }
}
