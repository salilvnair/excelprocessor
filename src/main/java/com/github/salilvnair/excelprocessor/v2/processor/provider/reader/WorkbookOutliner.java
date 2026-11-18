package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.csv.CsvFormat;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.outline.*;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.WorkbookCrypto;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Builds a {@link WorkbookOutline}. XLSX and CSV files are read with the streaming row sources
 * (flat memory, fast on large files); XLS files are loaded with POI.
 */
public final class WorkbookOutliner {
    private static final Pattern INTEGER = Pattern.compile("[+-]?\\d{1,15}");
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+\\.\\d*|\\.\\d+|\\d+)([eE][+-]?\\d+)?");

    private WorkbookOutliner() {}

    public static WorkbookOutline outline(File file, OutlineOptions options) {
        OutlineOptions opts = options == null ? OutlineOptions.defaults() : options;
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv")) {
            return outlineCsv(file, CsvFormat.csv(), opts);
        }
        if (name.endsWith(".tsv") || name.endsWith(".tab")) {
            return outlineCsv(file, CsvFormat.tsv(), opts);
        }
        if (name.endsWith(".xls")) {
            return outlineWithPoi(file, opts);
        }
        ExcelSheetContext context = ExcelSheetContext.builder().password(opts.password()).build();
        try (StreamingExcelSheetReaderImpl reader = new StreamingExcelSheetReaderImpl(file, context)) {
            List<SheetOutline> sheets = new ArrayList<>();
            for (String sheetName : reader.sheetNames()) {
                try (RowSource source = reader.openSource(sheetName)) {
                    sheets.add(outline(sheetName, source, opts));
                }
            }
            return new WorkbookOutline(sheets);
        }
    }

    public static WorkbookOutline outlineCsv(File file, CsvFormat format, OutlineOptions options) {
        OutlineOptions opts = options == null ? OutlineOptions.defaults() : options;
        try (CsvSheetReaderImpl reader = new CsvSheetReaderImpl(file, format, null);
             RowSource source = reader.openSource(file.getName())) {
            String sheetName = file.getName().replaceFirst("\\.[^.]*$", "");
            return new WorkbookOutline(Collections.singletonList(outline(sheetName, source, opts)));
        }
    }

    private static WorkbookOutline outlineWithPoi(File file, OutlineOptions opts) {
        try (Workbook workbook = WorkbookCrypto.open(new FileInputStream(file), opts.password())) {
            List<SheetOutline> sheets = new ArrayList<>();
            for (org.apache.poi.ss.usermodel.Sheet sheet : workbook) {
                try (RowSource source = new PoiRowSource(workbook, sheet)) {
                    sheets.add(outline(sheet.getSheetName(), source, opts));
                }
            }
            return new WorkbookOutline(sheets);
        }
        catch (IOException e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    static SheetOutline outline(String sheetName, RowSource source, OutlineOptions opts) {
        int wantedHeaderRow = opts.headerRow() > 0 ? opts.headerRow() - 1 : -1;
        int headerRow = -1;
        Map<Integer, String> headers = new TreeMap<>();
        Map<Integer, ColumnType> types = new HashMap<>();
        Map<Integer, Long> nonBlank = new HashMap<>();
        Map<Integer, String> samples = new HashMap<>();
        long dataRows = 0;
        long sampled = 0;
        while (source.nextRow()) {
            int rowIndex = source.rowIndex();
            List<RawCell> cells = source.cells();
            if (headerRow < 0) {
                boolean isHeader = wantedHeaderRow >= 0 ? rowIndex == wantedHeaderRow : hasText(cells);
                if (!isHeader) {
                    continue;
                }
                headerRow = rowIndex;
                for (RawCell raw : cells) {
                    String header = headerText(source.value(raw, new CellInfo()));
                    if (!header.isEmpty()) {
                        headers.put(raw.column, header);
                    }
                }
                continue;
            }
            boolean empty = true;
            for (RawCell raw : cells) {
                if (headers.containsKey(raw.column) && raw.value != null && !raw.value.trim().isEmpty()) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                continue;
            }
            dataRows++;
            if (sampled >= opts.sampleRows()) {
                continue;
            }
            sampled++;
            for (RawCell raw : cells) {
                if (!headers.containsKey(raw.column)) {
                    continue;
                }
                Object value = source.value(raw, new CellInfo());
                ColumnType type = typeOf(value, source.typedCells());
                if (type == ColumnType.BLANK) {
                    continue;
                }
                types.merge(raw.column, type, ColumnType::merge);
                nonBlank.merge(raw.column, 1L, Long::sum);
                samples.putIfAbsent(raw.column, String.valueOf(value));
            }
        }
        List<ColumnOutline> columns = new ArrayList<>();
        headers.forEach((column, header) -> columns.add(new ColumnOutline(header, column, ExcelSheetReader.toIndentName(column + 1),
                types.getOrDefault(column, ColumnType.BLANK), nonBlank.getOrDefault(column, 0L), samples.get(column))));
        return new SheetOutline(sheetName, headerRow < 0 ? -1 : headerRow + 1, columns, dataRows, sampled);
    }

    private static boolean hasText(List<RawCell> cells) {
        for (RawCell raw : cells) {
            if (raw.value != null && !raw.value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String headerText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Double) {
            double d = (Double) value;
            return ExcelSheetReaderUtil.cleanHeaderString(d == Math.rint(d) && !Double.isInfinite(d) ? String.valueOf((long) d) : String.valueOf(d));
        }
        return ExcelSheetReaderUtil.cleanHeaderString(String.valueOf(value));
    }

    private static ColumnType typeOf(Object value, boolean typed) {
        if (value == null) {
            return ColumnType.BLANK;
        }
        if (value instanceof Date) {
            return ColumnType.DATE;
        }
        if (value instanceof Boolean) {
            return ColumnType.BOOLEAN;
        }
        if (value instanceof Number) {
            return ColumnType.ofNumber(((Number) value).doubleValue());
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return ColumnType.BLANK;
        }
        if (typed) {
            return ColumnType.TEXT;
        }
        // CSV: every value is text, infer from its form
        if (INTEGER.matcher(text).matches()) {
            return text.length() > 1 && (text.startsWith("0") || text.startsWith("+0") || text.startsWith("-0")) ? ColumnType.TEXT : ColumnType.INTEGER;
        }
        if (DECIMAL.matcher(text).matches()) {
            return ColumnType.DECIMAL;
        }
        if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
            return ColumnType.BOOLEAN;
        }
        try {
            java.time.LocalDate.parse(text);
            return ColumnType.DATE;
        }
        catch (java.time.format.DateTimeParseException ignored) {
            // not an ISO date
        }
        try {
            java.time.LocalDateTime.parse(text);
            return ColumnType.DATE;
        }
        catch (java.time.format.DateTimeParseException ignored) {
            return ColumnType.TEXT;
        }
    }

    /** Rows of a POI usermodel sheet (XLS). */
    private static final class PoiRowSource implements RowSource {
        private final Workbook workbook;
        private final Iterator<Row> rows;
        private int rowIndex = -1;
        private List<RawCell> cells = Collections.emptyList();

        PoiRowSource(Workbook workbook, org.apache.poi.ss.usermodel.Sheet sheet) {
            this.workbook = workbook;
            this.rows = sheet.rowIterator();
        }

        @Override
        public boolean nextRow() {
            if (!rows.hasNext()) {
                return false;
            }
            Row row = rows.next();
            rowIndex = row.getRowNum();
            List<RawCell> rowCells = new ArrayList<>();
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbook, cell, new CellInfo());
                RawCell raw = new RawCell();
                raw.column = cell.getColumnIndex();
                raw.parsed = value;
                raw.value = value == null ? null : String.valueOf(value);
                rowCells.add(raw);
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
        public Object value(RawCell cell, CellInfo cellInfo) {
            return cell.parsed;
        }

        @Override
        public void close() {
            // the workbook is closed by the caller
        }
    }
}
