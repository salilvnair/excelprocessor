package com.github.salilvnair.excelprocessor.v2.csv;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.helper.ValueCoercer;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Writes {@code @Sheet}/{@code @Cell} classes (or {@code DynamicHeaderSheet} rows) as CSV/TSV, one
 * row at a time. The header row comes from the {@code @Cell} values in field order, or from the
 * ordered dynamic headers. Nothing but the current row is held in memory.
 *
 * <pre>{@code
 * try (CsvSheetWriter writer = ExcelSheetWriterFactory.openCsv(outputStream, CsvFormat.csv())) {
 *     writer.begin(OrderSheet.class);
 *     orders.forEach(writer::writeRow);
 * }
 * }</pre>
 *
 * Values: numbers in plain notation, {@code Date}/{@code LocalDateTime} as ISO-8601 (date only at
 * midnight), enums by their {@code @ExcelValue} label, everything else with {@code toString()}.
 * The streaming and CSV readers parse these forms back.
 */
public final class CsvSheetWriter implements Closeable, Flushable {
    private final Writer writer;
    private final CsvFormat format;
    private List<Field> fields;
    private List<String> dynamicHeaders;
    private Class<?> sheetClass;
    private long rowsWritten;
    private boolean closed;

    public CsvSheetWriter(Writer writer, CsvFormat format) {
        this.writer = writer instanceof BufferedWriter ? writer : new BufferedWriter(writer, 64 * 1024);
        this.format = format == null ? CsvFormat.csv() : format;
    }

    public CsvSheetWriter(OutputStream outputStream, CsvFormat format) {
        this(new OutputStreamWriter(outputStream, (format == null ? CsvFormat.csv() : format).charset()), format);
    }

    /** Starts the file with the headers of {@code sheetClass}'s {@code @Cell} fields. */
    public <T extends BaseSheet> CsvSheetWriter begin(Class<T> sheetClass) {
        Sheet sheet = start(sheetClass);
        if (sheet != null && sheet.dynamicHeaders()) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "' has dynamicHeaders=true: use begin(sheetClass, orderedHeaders)");
        }
        fields = new ArrayList<>(AnnotationUtil.getAnnotatedFields(sheetClass, Cell.class));
        List<String> headers = new ArrayList<>();
        for (Field field : fields) {
            headers.add(field.getAnnotation(Cell.class).value());
        }
        writeRecord(headers);
        return this;
    }

    /** Starts the file with {@code orderedHeaders}; rows are read from their dynamic cell map. */
    public <T extends DynamicHeaderSheet> CsvSheetWriter begin(Class<T> sheetClass, List<String> orderedHeaders) {
        start(sheetClass);
        if (orderedHeaders == null || orderedHeaders.isEmpty()) {
            throw new ExcelSheetWriterException("orderedHeaders must not be empty");
        }
        dynamicHeaders = new ArrayList<>(orderedHeaders);
        writeRecord(dynamicHeaders);
        return this;
    }

    public void writeRow(BaseSheet row) {
        ensureOpen();
        if (sheetClass == null) {
            throw new IllegalStateException("Call begin(...) before writeRow(...)");
        }
        if (row == null) {
            return;
        }
        if (!sheetClass.isInstance(row)) {
            throw new ExcelSheetWriterException("Row of type " + row.getClass().getName() + " written as " + sheetClass.getName());
        }
        List<String> values = new ArrayList<>();
        if (dynamicHeaders != null) {
            Map<String, Object> cells = ((DynamicHeaderSheet) row).dynamicHeaderKeyedCellValueMap();
            for (String header : dynamicHeaders) {
                values.add(text(cells == null ? null : cells.get(header)));
            }
        }
        else {
            for (Field field : fields) {
                values.add(text(ReflectionUtil.getFieldValue(row, field)));
            }
        }
        writeRecord(values);
        rowsWritten++;
    }

    public void writeRows(Iterable<? extends BaseSheet> rows) {
        for (BaseSheet row : rows) {
            writeRow(row);
        }
    }

    public long rowsWritten() {
        return rowsWritten;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /** Flushes and closes the underlying writer. */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        writer.close();
    }

    private Sheet start(Class<?> clazz) {
        ensureOpen();
        if (sheetClass != null) {
            throw new IllegalStateException("A CSV file holds one sheet: begin(...) was already called");
        }
        sheetClass = clazz;
        return clazz.getAnnotation(Sheet.class);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Writer is closed");
        }
    }

    private String text(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date) {
            value = LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            return dateTime.toLocalTime().equals(LocalTime.MIDNIGHT) ? dateTime.toLocalDate().toString() : dateTime.toString();
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return "";
            }
            BigDecimal decimal = BigDecimal.valueOf(d).stripTrailingZeros();
            return decimal.scale() <= 0 ? decimal.toBigInteger().toString() : decimal.toPlainString();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Enum) {
            return escapeFormula(ValueCoercer.enumLabel((Enum<?>) value));
        }
        return escapeFormula(String.valueOf(value));
    }

    private String escapeFormula(String text) {
        if (format.escapeFormulas() && !text.isEmpty()) {
            char c = text.charAt(0);
            if (c == '=' || c == '+' || c == '-' || c == '@' || c == '\t' || c == '\r') {
                return "'" + text;
            }
        }
        return text;
    }

    private void writeRecord(List<String> values) {
        try {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    writer.write(format.delimiter());
                }
                writeField(values.get(i));
            }
            writer.write(format.lineSeparator());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeField(String value) throws IOException {
        char quote = format.quote();
        boolean needsQuotes = value.indexOf(format.delimiter()) >= 0 || value.indexOf(quote) >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0
                || (!value.isEmpty() && (Character.isWhitespace(value.charAt(0)) || Character.isWhitespace(value.charAt(value.length() - 1))));
        if (!needsQuotes) {
            writer.write(value);
            return;
        }
        writer.write(quote);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == quote) {
                writer.write(quote);
            }
            writer.write(c);
        }
        writer.write(quote);
    }
}
