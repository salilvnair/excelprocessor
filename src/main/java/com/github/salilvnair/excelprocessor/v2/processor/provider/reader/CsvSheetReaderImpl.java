package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.csv.CsvParser;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CSV/TSV implementation of {@link com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader}.
 * The file is one sheet, whatever {@code @Sheet.value()} says; all other {@code @Sheet} options
 * (header row, value rows, ignored headers, duplicate and dynamic headers) apply as for XLSX.
 * Every field is text unless {@link CsvFormat#inferTypes()} is on; it is converted to the field
 * type when mapped.
 */
public class CsvSheetReaderImpl extends AbstractStreamingSheetReader {
    private static final Pattern NUMBER = Pattern.compile("[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?");

    private final File file;
    private InputStream inputStream;
    private final CsvFormat format;
    private final List<CsvRowSource> openSources = new ArrayList<>();

    public CsvSheetReaderImpl(File file, CsvFormat format, ExcelSheetContext sheetContext) {
        super(sheetContext);
        this.file = file;
        this.format = format == null ? CsvFormat.csv() : format;
    }

    /** The stream can be read once: a second {@code stream(...)} call fails. */
    public CsvSheetReaderImpl(InputStream inputStream, CsvFormat format, ExcelSheetContext sheetContext) {
        super(sheetContext);
        this.file = null;
        this.inputStream = inputStream;
        this.format = format == null ? CsvFormat.csv() : format;
    }

    @Override
    RowSource openSource(String sheetName) {
        InputStream in;
        if (file != null) {
            try {
                in = Files.newInputStream(file.toPath());
            }
            catch (IOException e) {
                throw new ExcelSheetReaderException(e);
            }
        }
        else {
            if (inputStream == null) {
                throw new IllegalStateException("The CSV input stream was already read; open the reader again");
            }
            in = inputStream;
            inputStream = null;
        }
        CsvRowSource source = new CsvRowSource(new CsvParser(in, format));
        openSources.add(source);
        return source;
    }

    @Override
    protected void release() {
        for (CsvRowSource source : openSources) {
            source.close();
        }
        openSources.clear();
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException ignored) {
                // nothing else to release
            }
        }
    }

    private final class CsvRowSource implements RowSource {
        private final CsvParser parser;
        private int rowIndex = -1;
        private List<RawCell> cells = Collections.emptyList();
        private boolean closed;

        CsvRowSource(CsvParser parser) {
            this.parser = parser;
        }

        @Override
        public boolean nextRow() {
            if (closed) {
                return false;
            }
            List<String> fields;
            try {
                fields = parser.next();
            }
            catch (IOException e) {
                throw new ExcelSheetReaderException(e);
            }
            if (fields == null) {
                return false;
            }
            rowIndex++;
            List<RawCell> rowCells = new ArrayList<>(fields.size());
            for (int c = 0; c < fields.size(); c++) {
                String field = fields.get(c);
                if (!field.isEmpty()) {
                    rowCells.add(RawCell.text(c, field));
                }
            }
            cells = rowCells;
            return true;
        }

        @Override
        public int rowIndex() {
            return rowIndex;
        }

        @Override
        public List<RawCell> cells() {
            return cells;
        }

        @Override
        public boolean typedCells() {
            return format.inferTypes();
        }

        @Override
        public Object value(RawCell cell, CellInfo cellInfo) {
            String v = cell.value;
            if (format.escapeFormulas() && v != null && v.length() > 1 && v.charAt(0) == '\'' && "=+-@\t\r".indexOf(v.charAt(1)) >= 0) {
                // undo the formula escaping CsvSheetWriter applies
                v = v.substring(1);
            }
            if (format.inferTypes() && v != null) {
                String trimmed = v.trim();
                if (NUMBER.matcher(trimmed).matches()) {
                    cellInfo.setCellType(Double.class);
                    cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DOUBLE);
                    return Double.parseDouble(trimmed);
                }
                if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                    cellInfo.setCellType(Boolean.class);
                    cellInfo.setCellTypeString(CellInfo.CELL_TYPE_BOOLEAN);
                    return Boolean.parseBoolean(trimmed);
                }
            }
            cellInfo.setCellType(String.class);
            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
            return v;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                parser.close();
            }
            catch (IOException ignored) {
                // nothing else to release
            }
        }
    }
}
