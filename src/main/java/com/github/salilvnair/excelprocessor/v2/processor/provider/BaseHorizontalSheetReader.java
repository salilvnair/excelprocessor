package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Salil V Nair
 */
public abstract class BaseHorizontalSheetReader extends BaseExcelSheetReader {
    private final boolean concurrent;
    private final int batchSize;
    public BaseHorizontalSheetReader(boolean concurrent, int batchSize) {
        this.concurrent = concurrent;
        this.batchSize = batchSize;
    }
    @Override
    public void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReaderUtil.extractWorkbook(context);
        if (workbook == null) {
            return;//change it to exception later on
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Object headerCellFieldMapOrDynamicCellField = null;
        if(!sheet.dynamicHeaders()) {
            Map<Cell, Field> cellFieldMap = StaticHeaderSheetReader.headerCellFieldMap(context, sheet, excelHeaders);
            headerCellFieldMapOrDynamicCellField = cellFieldMap;
            context.setHeaderCellFieldMap(cellFieldMap);
        }
        else {
            headerCellFieldMapOrDynamicCellField = DynamicHeaderSheetReader.dynamicCellField(clazz);
        }
        Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = orderedOrUnorderedMap(sheet);
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = orderedOrUnorderedMap(sheet);
        List<BaseSheet> baseSheetList = new ArrayList<>();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }

        context.setHeaderColumnIndexKeyedHeaderValueMap(headerColumnIndexKeyedHeaderValueMap);
        context.setRowIndexKeyedHeaderKeyCellInfoMap(rowIndexKeyedHeaderKeyCellInfoMap);
        context.setSheetData(Collections.unmodifiableList(baseSheetList));
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return null;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReaderUtil.extractWorkbook(context);
        if (workbook == null) {
            return null;//change it to exception later on
        }
        ExcelInfo excelInfo = new ExcelInfo();
        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        int valueRowBeginsAt = excelSheet.valueRowBeginsAt();
        int valueRowIndex = valueRowBeginsAt!=-1 ? valueRowBeginsAt: excelSheet.valueRowAt()!=-1 ? excelSheet.valueRowAt() : headerRowIndex+1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        SheetInfo sheetInfo = new SheetInfo();
        int totalRows = sheet.getLastRowNum();
        sheetInfo.setTotalRows(totalRows);
        sheetInfo.setName(sheetName);
        sheetInfo.setValueRowIndex(valueRowIndex);
        Row row = sheet.getRow(0);
        if(row !=null) {
            int totalColumns = row.getLastCellNum();
            sheetInfo.setTotalColumns(totalColumns);
        }
        excelInfo.sheets().add(sheetInfo);
        return excelInfo;
    }

    @SuppressWarnings("unchecked")
    protected void _read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Object headerCellFieldMapOrDynamicCellField) {
        Map<Cell, Field> headerCellFieldMap = null;
        Field dynamicCellField = null;
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        context.setSheet(sheet);
        if(sheet.dynamicHeaders()) {
            dynamicCellField = (Field) headerCellFieldMapOrDynamicCellField;
        }
        else {
            headerCellFieldMap = (Map<Cell, Field>) headerCellFieldMapOrDynamicCellField;
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt())  - 1;
        String sheetName = context.sheetName() == null ? sheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.getSheet(sheetName);
        int totalRows = workbookSheet.getLastRowNum();
        Row headerRow = workbookSheet.getRow(headerRowIndex);
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<Integer> ignoreHeaderColumns = context.ignoreHeaderColumns().stream().map(col -> ExcelSheetReader.toIndentNumber(col) -1 ).collect(Collectors.toList());
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        List<String> classCellHeaders = sheetCells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).collect(Collectors.toList());
        for (int c = headerColumnIndex; c < headerRow.getLastCellNum(); c++) {
            String headerString = headerRow.getCell(c).getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            if(!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders()) {
                ignoreHeaderColumns.add(c);
            }
            if(ignoreHeaders.contains(headerString)) {
                ignoreHeaderColumns.add(c);
            }
            sheetHeaders.add(headerString);
            headerString = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, c, headerRowIndex, headerStringList);
            headerColumnIndexKeyedHeaderValueMap.put(c, headerString);
            headerStringList.add(headerString);
        }
        int valueRowBeginsAt = context.valueRowBeginsAt()!=-1 ? context.valueRowBeginsAt() : sheet.valueRowBeginsAt();
        int valueRowIndex = valueRowBeginsAt!=-1 ? valueRowBeginsAt: sheet.valueRowAt()!=-1 ? sheet.valueRowAt() : headerRowIndex+1;
        int valueEndsAt = context.valueRowEndsAt()!=-1 ? context.valueRowEndsAt() : sheet.valueRowBeginsAt();
        totalRows = valueEndsAt!=-1 ? valueEndsAt : totalRows;
        for (int r = valueRowIndex ; r <= totalRows; r++) {
            Map<String, CellInfo> headerKeyCellInfoMap = new HashMap<>();
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            for (int c = headerColumnIndex; c < pnC; c++) {
                if(ignoreHeaderColumns.contains(c)) {
                    continue;
                }
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(r);
                cellInfo.setColumnIndex(c);
                String headerString = headerColumnIndexKeyedHeaderValueMap.get(c);
                if(StringUtils.isEmpty(headerString)){
                    continue;
                }
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                if(cell == null){
                    cellInfo.setValue(null);
                    headerKeyCellInfoMap.put(headerString, cellInfo);
                    continue;
                }
                Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                cellInfo.setValue(cellValue);
                headerKeyCellInfoMap.put(headerString, cellInfo);
            }
            rowIndexKeyedHeaderKeyCellInfoMap.put(r, headerKeyCellInfoMap);
        }

        Field finalDynamicCellField = dynamicCellField;
        Map<Cell, Field> finalHeaderCellFieldMap = headerCellFieldMap;
        rowIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueRowBeginsAt)
                .forEach(entry -> {
                    try {
                        int key = entry.getKey();
                        Map<String, CellInfo> value = entry.getValue();
                        BaseSheet classObject = null;
                        if(sheet.dynamicHeaders()) {
                            classObject = DynamicHeaderSheetReader.dynamicCellValueResolver(clazz, headerStringList, value, key, finalDynamicCellField);
                        }
                        else {
                            classObject = StaticHeaderSheetReader.cellValueResolver(clazz, value, key, finalHeaderCellFieldMap);
                        }
                        classObject.setSheetHeaders(sheetHeaders);
                        classObject.setCells(value);
                        baseSheetList.add(classObject);
                    }
                    catch (Exception ignored) {}
                });
    }

    private void _concurrentRead(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Object headerFieldOrFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalRows = sheetInfo.totalRows();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalRows, batchSize);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            Integer from = rowList.get(0);
            int valueRowIndex = sheetInfo.valueRowIndex();
            if(valueRowIndex > from ) {
                from = valueRowIndex;
            }
            else {
                from = from + 1;
            }
            Integer to = rowList.get(rowList.size() - 1);
            context.setValueRowBeginsAt(from);
            context.setValueRowEndsAt(to+1);
            service.toContext(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerFieldOrFieldMap);
        }
        try {
            executor.invokeAll(taskCallables);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
}
