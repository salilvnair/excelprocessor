package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Default {@link StreamingExcelSheetWriter}. Reuses the horizontal writer's per-row code, so cells
 * and styles are identical to the list based writers.
 *
 * @author Salil V Nair
 */
public class StreamingExcelSheetWriterImpl extends BaseHorizontalSheetWriter implements StreamingExcelSheetWriter {
    private static final int MAX_ROW_INDEX = SpreadsheetVersion.EXCEL2007.getLastRowIndex();

    private final ExcelSheetContext sheetContext;
    private final ExcelSheetWriterContext context;
    private SXSSFWorkbook workbook;
    private boolean ownsWorkbook;
    private boolean finished;
    private boolean closed;

    private Class<? extends BaseSheet> sheetClass;
    private Sheet sheet;
    private SXSSFSheet workbookSheet;
    private BodyLayout<Field> fixedLayout;
    private BodyLayout<CellInfo> dynamicLayout;
    private int nextRowIndex;
    private long rowsWritten;
    private long totalRowsWritten;

    public StreamingExcelSheetWriterImpl(ExcelSheetContext sheetContext) {
        this.sheetContext = sheetContext;
        this.context = ExcelSheetWriterImpl.buildExcelSheetWriterContext(sheetContext, false);
        this.context.setStreamingWorkbook(true);
        sheetContext.setWriterContext(this.context);
    }

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext writerContext) {
        throw new UnsupportedOperationException("Use beginSheet/writeRow");
    }

    @Override
    public <T extends BaseSheet> StreamingExcelSheetWriter beginSheet(Class<T> sheetClass) {
        Sheet sheet = startSheet(sheetClass);
        if (sheet.dynamicHeaders()) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "' has dynamicHeaders=true: use beginSheet(sheetClass, orderedHeaders)");
        }
        T prototype = newInstance(sheetClass);
        List<T> prototypeList = Collections.singletonList(prototype);
        context.setSheetData(prototypeList);
        context.setSheetDataObj(prototype);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(sheetClass, Cell.class);
        List<Field> cells = new ArrayList<>(cellFields);
        writeDataToHeader(prototypeList, cellFields, workbookSheet, cells, sheet, context);
        fixedLayout = bodyLayout(cellFields, workbookSheet, cells, sheet);
        nextRowIndex = fixedLayout.firstValueRowIndex();
        applySheetStyles(sheet, workbook, workbookSheet, context);
        return this;
    }

    @Override
    public <T extends DynamicHeaderSheet> StreamingExcelSheetWriter beginSheet(Class<T> sheetClass, List<String> orderedHeaders) {
        Sheet sheet = startSheet(sheetClass);
        if (orderedHeaders == null || orderedHeaders.isEmpty()) {
            throw new ExcelSheetWriterException("orderedHeaders must not be empty for sheet '" + sheet.value() + "'");
        }
        LinkedHashMap<String, Object> headerMap = new LinkedHashMap<>();
        for (String header : orderedHeaders) {
            if (headerMap.containsKey(header)) {
                throw new ExcelSheetWriterException("Duplicate header '" + header + "' for sheet '" + sheet.value() + "'");
            }
            headerMap.put(header, null);
        }
        T prototype = newInstance(sheetClass);
        prototype.setDynamicHeaderKeyedCellValueMap(headerMap);
        List<T> prototypeList = Collections.singletonList(prototype);
        context.setSheetData(prototypeList);
        context.setSheetDataObj(prototype);
        writeDynamicDataToHeader(prototypeList, workbookSheet, sheet, context);
        dynamicLayout = dynamicBodyLayout(extractHeaderKeyedCellInfoMap(prototype), workbookSheet, sheet);
        nextRowIndex = dynamicLayout.firstValueRowIndex();
        applySheetStyles(sheet, workbook, workbookSheet, context);
        return this;
    }

    @Override
    public void writeRow(BaseSheet row) {
        ensureWritable();
        if (sheetClass == null) {
            throw new IllegalStateException("Call beginSheet(...) before writeRow(...)");
        }
        if (row == null) {
            return;
        }
        if (!sheetClass.isInstance(row)) {
            throw new ExcelSheetWriterException("Row of type " + row.getClass().getName() + " written to sheet '"
                    + sheet.value() + "' of type " + sheetClass.getName());
        }
        if (nextRowIndex > MAX_ROW_INDEX) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "' exceeds the XLSX limit of "
                    + (MAX_ROW_INDEX + 1) + " rows");
        }
        if (dynamicLayout != null) {
            writeDynamicBodyRow((DynamicHeaderSheet) row, nextRowIndex, dynamicLayout, workbookSheet, sheet, context);
        }
        else {
            writeBodyRow(row, nextRowIndex, fixedLayout, workbookSheet, sheet, context);
        }
        context.setSheetDataObj(null);
        nextRowIndex++;
        rowsWritten++;
        totalRowsWritten++;
    }

    @Override
    public long rowsWritten() {
        return rowsWritten;
    }

    @Override
    public long totalRowsWritten() {
        return totalRowsWritten;
    }

    @Override
    public void finish(OutputStream outputStream) throws IOException {
        ensureWritable();
        if (workbook == null) {
            throw new IllegalStateException("Nothing to finish: no sheet was started");
        }
        finished = true;
        workbook.write(outputStream);
    }

    @Override
    public void finish(Path path) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            finish(outputStream);
        }
    }

    @Override
    public Workbook workbook() {
        return workbook;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (workbook == null) {
            return;
        }
        workbook.dispose();
        if (ownsWorkbook) {
            try {
                workbook.close();
            }
            catch (IOException ignored) {
                // temp files are already disposed; nothing else to release
            }
        }
    }

    private Sheet startSheet(Class<? extends BaseSheet> sheetClass) {
        ensureWritable();
        Sheet sheet = sheetClass.getAnnotation(Sheet.class);
        if (sheet == null) {
            throw new ExcelSheetWriterException(sheetClass.getName() + " is not annotated with @Sheet");
        }
        if (sheet.vertical() || sheet.userDefinedTemplate() || sheet.mergedHeaders() || sheet.sectional()) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "': vertical, userDefinedTemplate, mergedHeaders and sectional sheets are not supported by the streaming writer");
        }
        if (!ExcelFileType.Extension.XLSX.equals(sheet.type())) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "': the streaming writer only produces XLSX");
        }
        if (workbook == null) {
            StreamingWorkbookSupport.Opened opened = StreamingWorkbookSupport.open(sheet, context);
            workbook = opened.workbook;
            ownsWorkbook = context.template() == null;
            context.setWorkbook(workbook);
            context.setContainsExistingWorkbook(true);
            context.setExistingWorkbook(workbook);
            sheetContext.setWorkbook(workbook);
        }
        if (workbook.getSheet(sheet.value()) != null && workbook.getSheet(sheet.value()).getLastRowNum() > -1) {
            throw new ExcelSheetWriterException("Sheet '" + sheet.value() + "' was already written by this writer");
        }
        this.sheetClass = sheetClass;
        this.sheet = sheet;
        this.workbookSheet = StreamingWorkbookSupport.sheet(workbook, sheet);
        this.fixedLayout = null;
        this.dynamicLayout = null;
        this.rowsWritten = 0;
        return sheet;
    }

    private void ensureWritable() {
        if (closed) {
            throw new IllegalStateException("Writer is closed");
        }
        if (finished) {
            throw new IllegalStateException("Writer is already finished");
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException e) {
            throw new ExcelSheetWriterException(clazz.getName() + " needs an accessible no-arg constructor: " + e.getMessage());
        }
    }
}
