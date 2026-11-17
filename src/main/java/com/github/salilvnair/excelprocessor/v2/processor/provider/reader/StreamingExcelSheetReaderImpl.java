package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * StAX based {@link StreamingExcelSheetReader}. Header handling, duplicate-header keys and value
 * conversion reuse the same helpers as the in-memory readers, so mapped objects are identical.
 *
 * @author Salil V Nair
 */
public class StreamingExcelSheetReaderImpl extends BaseExcelSheetReader implements StreamingExcelSheetReader {
    private final OPCPackage pkg;
    private final XSSFReader xssfReader;
    private final SharedStrings sharedStrings;
    private final StylesTable styles;
    private final boolean date1904;
    private final ExcelSheetContext sheetContext;
    private final Map<Integer, Boolean> dateStyleCache = new HashMap<>();
    private boolean closed;

    public StreamingExcelSheetReaderImpl(File file, ExcelSheetContext sheetContext) {
        this(open(file), sheetContext);
    }

    public StreamingExcelSheetReaderImpl(InputStream inputStream, ExcelSheetContext sheetContext) {
        this(open(inputStream), sheetContext);
    }

    private StreamingExcelSheetReaderImpl(OPCPackage pkg, ExcelSheetContext sheetContext) {
        this.pkg = pkg;
        this.sheetContext = sheetContext == null ? ExcelSheetContext.builder().build() : sheetContext;
        try {
            this.xssfReader = new XSSFReader(pkg);
            this.xssfReader.setUseReadOnlySharedStringsTable(true);
            this.sharedStrings = xssfReader.getSharedStringsTable();
            this.styles = xssfReader.getStylesTable();
            this.date1904 = readDate1904(xssfReader);
        }
        catch (Exception e) {
            pkg.revert();
            throw new ExcelSheetReaderException(e);
        }
    }

    private static OPCPackage open(File file) {
        try {
            // READ access does not load the whole file into memory and never writes back
            return OPCPackage.open(file, PackageAccess.READ);
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    private static OPCPackage open(InputStream inputStream) {
        try {
            return OPCPackage.open(inputStream);
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    // BaseExcelSheetReader contract (list based API) is not used by this reader
    @Override
    void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        throw new UnsupportedOperationException("Use stream(sheetClass)");
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        throw new UnsupportedOperationException("Use stream(sheetClass)");
    }

    @Override
    public <T extends BaseSheet> Stream<T> stream(Class<T> sheetClass) {
        if (closed) {
            throw new IllegalStateException("Reader is closed");
        }
        Sheet sheet = sheetClass.getAnnotation(Sheet.class);
        if (sheet == null) {
            throw new ExcelSheetReaderException(sheetClass.getName() + " is not annotated with @Sheet");
        }
        if (sheet.vertical() || sheet.sectional() || sheet.mergedHeaders()) {
            throw new ExcelSheetReaderException("Sheet '" + sheet.value() + "': vertical, sectional and mergedHeaders sheets are not supported by the streaming reader");
        }
        String sheetName = sheetContext.sheetName() != null ? sheetContext.sheetName() : sheet.value();
        InputStream sheetXml = sheetStream(sheetName);
        if (sheetXml == null) {
            if (!sheetContext.suppressExceptions()) {
                throw new ExcelSheetReaderException("Sheet '" + sheetName + "' is not present in the excel.");
            }
            return Stream.empty();
        }
        RowIterator<T> iterator;
        try {
            iterator = new RowIterator<>(sheetClass, sheet, sheetXml);
        }
        catch (XMLStreamException e) {
            closeQuietly(sheetXml);
            throw new ExcelSheetReaderException(e);
        }
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false).onClose(iterator::close);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        // revert() closes a read-only package without trying to save it
        pkg.revert();
    }

    private InputStream sheetStream(String sheetName) {
        try {
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (sheets.hasNext()) {
                InputStream in = sheets.next();
                if (sheetName.equals(sheets.getSheetName())) {
                    return in;
                }
                in.close();
            }
            return null;
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    private static boolean readDate1904(XSSFReader reader) {
        try (InputStream in = reader.getWorkbookData()) {
            XMLStreamReader xml = XMLHelper.newXMLInputFactory().createXMLStreamReader(in);
            try {
                while (xml.hasNext()) {
                    if (xml.next() == XMLStreamConstants.START_ELEMENT && "workbookPr".equals(xml.getLocalName())) {
                        String v = xml.getAttributeValue(null, "date1904");
                        return "1".equals(v) || "true".equalsIgnoreCase(v);
                    }
                    if (xml.getEventType() == XMLStreamConstants.START_ELEMENT && "sheets".equals(xml.getLocalName())) {
                        return false;
                    }
                }
            }
            finally {
                xml.close();
            }
        }
        catch (Exception ignored) {
            // date1904 is optional; default to the 1900 date system
        }
        return false;
    }

    private boolean isDateStyle(int styleIndex) {
        return dateStyleCache.computeIfAbsent(styleIndex, idx -> {
            if (styles == null || idx >= styles.getNumCellStyles()) {
                return false;
            }
            XSSFCellStyle style = styles.getStyleAt(idx);
            return style != null && DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString());
        });
    }

    private static void closeQuietly(InputStream in) {
        try {
            in.close();
        }
        catch (IOException ignored) {
            // nothing to release
        }
    }

    /** Raw value of one parsed {@code <c>} element. */
    private static final class RawCell {
        int column;
        String type;
        int style;
        String value;
        boolean inlineText;
    }

    /** Pulls {@code <row>} elements and turns data rows into sheet objects. */
    private final class RowIterator<T extends BaseSheet> implements Iterator<T> {
        private final Class<T> clazz;
        private final Sheet sheet;
        private final InputStream in;
        private final XMLStreamReader xml;
        private final ExcelSheetReaderContext readerContext = new ExcelSheetReaderContext();

        private final int headerRowIndex;
        private final int headerColumnIndex;
        private final int headerColumnEndsAt;
        private final int valueRowIndex;
        private final int valueRowEndsAt;
        private final Set<Integer> ignoreHeaderColumns;
        private final List<String> ignoreHeaders;
        private final List<String> ignoreHeaderPatterns;
        private final Set<String> classCellHeaders;
        private final Map<Cell, Field> headerCellFieldMap;
        private final Field dynamicCellField;

        private final Map<Integer, String> columnHeaders = new HashMap<>();
        private final Map<String, String> originalHeaders = new HashMap<>();
        private final List<String> headerStringList;
        private final List<String> sheetHeaders;
        private int lastHeaderColumn = -1;
        private boolean headersRead;
        private int rowCounter = -1;

        private T next;
        private boolean finished;

        RowIterator(Class<T> clazz, Sheet sheet, InputStream in) throws XMLStreamException {
            this.clazz = clazz;
            this.sheet = sheet;
            this.in = in;
            this.xml = XMLHelper.newXMLInputFactory().createXMLStreamReader(in);
            readerContext.setSheet(sheet);
            readerContext.setHeaderFieldInfoMap(sheetContext.headerFieldInfo());
            readerContext.setSuppressExceptions(sheetContext.suppressExceptions());

            headerRowIndex = sheet.headerRowAt() - 1;
            headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt()) - 1;
            headerColumnEndsAt = StringUtils.isNotEmpty(sheet.headerColumnEndsAt()) ? ExcelSheetReader.toIndentNumber(sheet.headerColumnEndsAt()) : Integer.MAX_VALUE;
            int valueRowBeginsAt = sheet.valueRowBeginsAt() > -1 ? sheet.valueRowBeginsAt() - 1 : -1;
            valueRowIndex = valueRowBeginsAt > -1 ? valueRowBeginsAt : sheet.valueRowAt() > -1 ? sheet.valueRowAt() - 1 : headerRowIndex + 1;
            valueRowEndsAt = sheet.valueRowEndsAt() > -1 ? sheet.valueRowEndsAt() - 1 : Integer.MAX_VALUE;
            List<String> contextIgnoreColumns = sheetContext.ignoreHeaderColumns() == null ? Collections.emptyList() : sheetContext.ignoreHeaderColumns();
            ignoreHeaderColumns = contextIgnoreColumns.stream().map(col -> ExcelSheetReader.toIndentNumber(col) - 1).collect(Collectors.toCollection(HashSet::new));
            ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.asList(sheet.ignoreHeaders()) : sheetContext.ignoreHeaders();
            ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.asList(sheet.ignoreHeaderPatterns()) : Collections.emptyList();
            Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
            classCellHeaders = cellFields.stream().map(f -> f.getAnnotation(Cell.class).value()).collect(Collectors.toCollection(HashSet::new));
            if (sheet.dynamicHeaders()) {
                headerCellFieldMap = null;
                dynamicCellField = DynamicHeaderSheetReader.dynamicCellField(clazz);
            }
            else {
                headerCellFieldMap = StaticHeaderSheetReader.headerCellFieldMap(readerContext, sheet, cellFields);
                dynamicCellField = null;
            }
            headerStringList = BaseExcelSheetReader.orderedOrUnorderedList(sheet);
            sheetHeaders = BaseExcelSheetReader.orderedOrUnorderedList(sheet);
        }

        @Override
        public boolean hasNext() {
            if (next == null && !finished) {
                next = advance();
            }
            return next != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T row = next;
            next = null;
            return row;
        }

        void close() {
            finished = true;
            try {
                xml.close();
            }
            catch (XMLStreamException ignored) {
                // stream is closed below
            }
            closeQuietly(in);
        }

        /** Reads rows until a data row is produced or the sheet ends. */
        private T advance() {
            try {
                while (xml.hasNext()) {
                    int event = xml.next();
                    if (event == XMLStreamConstants.START_ELEMENT && "row".equals(xml.getLocalName())) {
                        String r = xml.getAttributeValue(null, "r");
                        int rowIndex = r != null ? Integer.parseInt(r) - 1 : rowCounter + 1;
                        rowCounter = rowIndex;
                        List<RawCell> cells = readRowCells();
                        if (rowIndex == headerRowIndex) {
                            readHeaders(cells);
                        }
                        else if (rowIndex > valueRowEndsAt) {
                            close();
                            return null;
                        }
                        else if (headersRead && rowIndex >= valueRowIndex) {
                            T row = toRow(rowIndex, cells);
                            if (row != null) {
                                return row;
                            }
                        }
                    }
                    else if (event == XMLStreamConstants.END_ELEMENT && "sheetData".equals(xml.getLocalName())) {
                        break;
                    }
                }
            }
            catch (XMLStreamException e) {
                close();
                throw new ExcelSheetReaderException(e);
            }
            close();
            return null;
        }

        /** Parses the {@code <c>} children of the current {@code <row>}. */
        private List<RawCell> readRowCells() throws XMLStreamException {
            List<RawCell> cells = new ArrayList<>();
            RawCell cell = null;
            int column = -1;
            StringBuilder text = null;
            while (xml.hasNext()) {
                int event = xml.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = xml.getLocalName();
                    if ("c".equals(name)) {
                        cell = new RawCell();
                        String ref = xml.getAttributeValue(null, "r");
                        column = ref != null ? new CellReference(ref).getCol() : column + 1;
                        cell.column = column;
                        cell.type = xml.getAttributeValue(null, "t");
                        String s = xml.getAttributeValue(null, "s");
                        cell.style = s != null ? Integer.parseInt(s) : 0;
                    }
                    else if (cell != null && ("v".equals(name) || "t".equals(name))) {
                        if ("t".equals(name)) {
                            cell.inlineText = true;
                        }
                        text = text == null ? new StringBuilder() : text;
                    }
                }
                else if ((event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) && text != null) {
                    text.append(xml.getText());
                }
                else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = xml.getLocalName();
                    if (("v".equals(name) || "t".equals(name)) && cell != null && text != null) {
                        // inline strings can be split into several <t> runs
                        cell.value = cell.value == null || !cell.inlineText ? text.toString() : cell.value + text;
                        text = null;
                    }
                    else if ("c".equals(name) && cell != null) {
                        cells.add(cell);
                        cell = null;
                    }
                    else if ("row".equals(name)) {
                        break;
                    }
                }
            }
            return cells;
        }

        private void readHeaders(List<RawCell> cells) {
            for (RawCell raw : cells) {
                int c = raw.column;
                if (c < headerColumnIndex || c >= headerColumnEndsAt) {
                    continue;
                }
                Object value = value(raw, new CellInfo());
                String headerString = value == null ? "" : ExcelSheetReaderUtil.cleanHeaderString(
                        value instanceof Double && ((Double) value) == Math.rint((Double) value) && !Double.isInfinite((Double) value)
                                ? String.valueOf(((Double) value).longValue()) : String.valueOf(value));
                if (headerString.isEmpty()
                        || (!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders())
                        || ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)
                        || ignoreHeaders.contains(headerString)) {
                    ignoreHeaderColumns.add(c);
                    continue;
                }
                sheetHeaders.add(headerString);
                String key = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, c, headerRowIndex, headerStringList);
                columnHeaders.put(c, key);
                originalHeaders.put(key, headerString);
                headerStringList.add(key);
                lastHeaderColumn = Math.max(lastHeaderColumn, c);
            }
            headersRead = true;
        }

        private T toRow(int rowIndex, List<RawCell> rawCells) {
            Map<Integer, RawCell> byColumn = new HashMap<>();
            boolean empty = true;
            for (RawCell raw : rawCells) {
                byColumn.put(raw.column, raw);
                if (raw.column >= headerColumnIndex && raw.column <= lastHeaderColumn && StringUtils.isNotBlank(raw.value)) {
                    empty = false;
                }
            }
            if (empty) {
                return null;
            }
            Map<String, CellInfo> cellInfoMap = BaseExcelSheetReader.orderedOrUnorderedMap(sheet);
            for (int c = headerColumnIndex; c <= lastHeaderColumn; c++) {
                String header = columnHeaders.get(c);
                if (header == null || ignoreHeaderColumns.contains(c)) {
                    continue;
                }
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(rowIndex);
                cellInfo.setRow(rowIndex + 1);
                cellInfo.setColumnIndex(c);
                cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                cellInfo.setHeader(header);
                cellInfo.setOriginalHeader(originalHeaders.get(header));
                RawCell raw = byColumn.get(c);
                if (raw != null) {
                    cellInfo.setValue(value(raw, cellInfo));
                    if (styles != null && raw.style < styles.getNumCellStyles()) {
                        applyStyleColors(styles, styles.getStyleAt(raw.style), cellInfo);
                    }
                }
                cellInfoMap.put(header, cellInfo);
            }
            try {
                BaseSheet row = sheet.dynamicHeaders()
                        ? DynamicHeaderSheetReader.dynamicCellValueResolver(clazz, headerStringList, cellInfoMap, rowIndex, dynamicCellField, readerContext)
                        : StaticHeaderSheetReader.cellValueResolver(clazz, cellInfoMap, rowIndex, headerCellFieldMap);
                row.setSheetHeaders(sheetHeaders);
                row.setCells(cellInfoMap);
                return clazz.cast(row);
            }
            catch (Exception e) {
                if (!sheetContext.suppressExceptions()) {
                    throw new ExcelSheetReaderException(e);
                }
                return null;
            }
        }

        /** Converts a raw cell to the same Java types the in-memory reader produces. */
        private Object value(RawCell raw, CellInfo cellInfo) {
            String v = raw.value;
            String t = raw.type;
            if ("s".equals(t)) {
                if (v == null) {
                    return null;
                }
                cellInfo.setCellType(String.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                return sharedStrings.getItemAt(Integer.parseInt(v.trim())).getString();
            }
            if ("inlineStr".equals(t) || "str".equals(t)) {
                cellInfo.setCellType(String.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                return v == null ? "" : v;
            }
            if (v == null || v.isEmpty()) {
                return null;
            }
            if ("b".equals(t)) {
                cellInfo.setCellType(Boolean.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_BOOLEAN);
                return "1".equals(v) || "true".equalsIgnoreCase(v);
            }
            if ("e".equals(t)) {
                return null;
            }
            double number;
            try {
                number = Double.parseDouble(v);
            }
            catch (NumberFormatException e) {
                cellInfo.setCellType(String.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_STRING);
                return v;
            }
            if (isDateStyle(raw.style) && DateUtil.isValidExcelDate(number)) {
                cellInfo.setCellType(Date.class);
                cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DATE);
                return DateUtil.getJavaDate(number, date1904);
            }
            cellInfo.setCellType(Double.class);
            cellInfo.setCellTypeString(CellInfo.CELL_TYPE_DOUBLE);
            return number;
        }
    }
}
