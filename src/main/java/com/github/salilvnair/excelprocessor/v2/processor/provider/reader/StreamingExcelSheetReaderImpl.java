package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.helper.WorkbookCrypto;
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
import java.util.*;

/**
 * StAX based {@link com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader} for
 * XLSX. Header handling, duplicate-header keys and value conversion reuse the same helpers as the
 * in-memory readers, so mapped objects are identical.
 *
 * @author Salil V Nair
 */
public class StreamingExcelSheetReaderImpl extends AbstractStreamingSheetReader {
    private final OPCPackage pkg;
    private final XSSFReader xssfReader;
    private final SharedStrings sharedStrings;
    private final StylesTable styles;
    private final boolean date1904;
    private final Map<Integer, Boolean> dateStyleCache = new HashMap<>();
    /** decrypted copy of a password-protected file; deleted on close */
    private final java.nio.file.Path decryptedFile;

    public StreamingExcelSheetReaderImpl(File file, ExcelSheetContext sheetContext) {
        this(open(file, sheetContext), sheetContext);
    }

    public StreamingExcelSheetReaderImpl(InputStream inputStream, ExcelSheetContext sheetContext) {
        this(open(inputStream, sheetContext), sheetContext);
    }

    private StreamingExcelSheetReaderImpl(Opened opened, ExcelSheetContext sheetContext) {
        super(sheetContext);
        this.pkg = opened.pkg;
        this.decryptedFile = opened.decryptedFile;
        try {
            this.xssfReader = new XSSFReader(pkg);
            this.xssfReader.setUseReadOnlySharedStringsTable(true);
            this.sharedStrings = xssfReader.getSharedStringsTable();
            this.styles = xssfReader.getStylesTable();
            this.date1904 = readDate1904(xssfReader);
        }
        catch (Exception e) {
            release();
            throw new ExcelSheetReaderException(e);
        }
    }

    private static final class Opened {
        final OPCPackage pkg;
        final java.nio.file.Path decryptedFile;

        Opened(OPCPackage pkg, java.nio.file.Path decryptedFile) {
            this.pkg = pkg;
            this.decryptedFile = decryptedFile;
        }
    }

    private static Opened open(File file, ExcelSheetContext context) {
        String password = context == null ? null : context.password();
        if (password != null || WorkbookCrypto.isEncrypted(file)) {
            return openDecrypted(WorkbookCrypto.decryptToTempFile(file, password));
        }
        try {
            // READ access does not load the whole file into memory and never writes back
            return new Opened(OPCPackage.open(file, PackageAccess.READ), null);
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    private static Opened open(InputStream inputStream, ExcelSheetContext context) {
        String password = context == null ? null : context.password();
        if (password != null) {
            return openDecrypted(WorkbookCrypto.decryptToTempFile(inputStream, password));
        }
        try {
            return new Opened(OPCPackage.open(inputStream), null);
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    private static Opened openDecrypted(java.nio.file.Path decrypted) {
        try {
            return new Opened(OPCPackage.open(decrypted.toFile(), PackageAccess.READ), decrypted);
        }
        catch (Exception e) {
            deleteQuietly(decrypted);
            throw new ExcelSheetReaderException(e);
        }
    }

    private static void deleteQuietly(java.nio.file.Path path) {
        try {
            java.nio.file.Files.deleteIfExists(path);
        }
        catch (IOException ignored) {
            // temp directory clean-up will remove it
        }
    }

    /** Names of the sheets in workbook order. */
    public List<String> sheetNames() {
        List<String> names = new ArrayList<>();
        try {
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream ignored = sheets.next()) {
                    names.add(sheets.getSheetName());
                }
            }
        }
        catch (Exception e) {
            throw new ExcelSheetReaderException(e);
        }
        return names;
    }

    @Override
    RowSource openSource(String sheetName) {
        InputStream sheetXml = sheetStream(sheetName);
        if (sheetXml == null) {
            return null;
        }
        try {
            return new XlsxRowSource(sheetXml);
        }
        catch (XMLStreamException e) {
            closeQuietly(sheetXml);
            throw new ExcelSheetReaderException(e);
        }
    }

    @Override
    protected void release() {
        // revert() closes a read-only package without trying to save it
        pkg.revert();
        if (decryptedFile != null) {
            deleteQuietly(decryptedFile);
        }
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

    /** Pulls {@code <row>} elements of one worksheet part. */
    private final class XlsxRowSource implements RowSource {
        private final InputStream in;
        private final XMLStreamReader xml;
        private int rowCounter = -1;
        private int rowIndex = -1;
        private List<RawCell> cells = Collections.emptyList();
        private boolean closed;

        XlsxRowSource(InputStream in) throws XMLStreamException {
            this.in = in;
            this.xml = XMLHelper.newXMLInputFactory().createXMLStreamReader(in);
        }

        @Override
        public boolean nextRow() {
            if (closed) {
                return false;
            }
            try {
                while (xml.hasNext()) {
                    int event = xml.next();
                    if (event == XMLStreamConstants.START_ELEMENT && "row".equals(xml.getLocalName())) {
                        String r = xml.getAttributeValue(null, "r");
                        rowIndex = r != null ? Integer.parseInt(r) - 1 : rowCounter + 1;
                        rowCounter = rowIndex;
                        cells = readRowCells();
                        return true;
                    }
                    if (event == XMLStreamConstants.END_ELEMENT && "sheetData".equals(xml.getLocalName())) {
                        return false;
                    }
                }
                return false;
            }
            catch (XMLStreamException e) {
                throw new ExcelSheetReaderException(e);
            }
        }

        @Override
        public int rowIndex() {
            return rowIndex;
        }

        @Override
        public List<RawCell> cells() {
            return cells;
        }

        /** Parses the {@code <c>} children of the current {@code <row>}. */
        private List<RawCell> readRowCells() throws XMLStreamException {
            List<RawCell> rowCells = new ArrayList<>();
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
                        rowCells.add(cell);
                        cell = null;
                    }
                    else if ("row".equals(name)) {
                        break;
                    }
                }
            }
            return rowCells;
        }

        /** Converts a raw cell to the same Java types the in-memory reader produces. */
        @Override
        public Object value(RawCell raw, CellInfo cellInfo) {
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

        @Override
        public void decorate(RawCell raw, CellInfo cellInfo) {
            if (styles != null && raw.style < styles.getNumCellStyles()) {
                applyStyleColors(styles, styles.getStyleAt(raw.style), cellInfo);
            }
        }

        /** True when the cell's style is a date format (used by the outline's type inference). */
        boolean isDate(RawCell raw) {
            return raw.type == null && isDateStyle(raw.style);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                xml.close();
            }
            catch (XMLStreamException ignored) {
                // stream is closed below
            }
            closeQuietly(in);
        }
    }
}
