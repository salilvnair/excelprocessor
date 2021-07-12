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
public class BaseVerticalSheetReader extends BaseExcelSheetReader {
    private final boolean concurrent;
    private final int batchSize;
    public BaseVerticalSheetReader(boolean concurrent, int batchSize) {
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
        Map<Integer, String> headerRowIndexKeyedHeaderValueMap = orderedOrUnorderedMap(sheet);
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = orderedOrUnorderedMap(sheet);
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
        List<BaseSheet> baseSheetList = new ArrayList<>();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        context.setHeaderRowIndexKeyedHeaderValueMap(headerRowIndexKeyedHeaderValueMap);
        context.setColIndexKeyedHeaderKeyCellInfoMap(colIndexKeyedHeaderKeyCellInfoMap);
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
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;
        String valueColumnAt = excelSheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        SheetInfo sheetInfo = new SheetInfo();
        int totalRows = sheet.getLastRowNum();
        sheetInfo.setTotalRows(totalRows);
        sheetInfo.setName(sheetName);
        sheetInfo.setValueColumnIndex(valueColumnIndex);
        Row row = sheet.getRow(headerRowIndex);
        if(row !=null) {
            int totalColumns = row.getLastCellNum();
            sheetInfo.setTotalColumns(totalColumns);
        }
        excelInfo.sheets().add(sheetInfo);
        return excelInfo;
    }

    @SuppressWarnings("unchecked")
    protected void _read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Object headerCellFieldMapOrDynamicCellField) {
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
        String sheetName = context.sheetName() == null ? sheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.getSheet(sheetName);
        int totalRows = workbookSheet.getLastRowNum();
        totalRows = sheet.valueRowEndsAt()!=-1 ? sheet.valueRowEndsAt() - 1 : totalRows;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt())  - 1;
        String valueColumnAt = !StringUtils.isEmpty(context.valueColumnBeginsAt()) ? context.valueColumnBeginsAt() : sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        int maxColumnC = 0;
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<Integer> ignoreHeaderRows = context.ignoreHeaderRows().stream().map(r -> r+1).collect(Collectors.toList());
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        List<String> classCellHeaders = sheetCells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).collect(Collectors.toList());
        for (int r = headerRowIndex; r <= totalRows; r++) {
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            if(maxColumnC < pnC ) {
                maxColumnC = pnC;
            }
            String valueColumnEndsAt = context.valueColumnEndsAt() != null ? context.valueColumnEndsAt() : sheet.valueColumnEndsAt();
            if(!StringUtils.isEmpty(valueColumnEndsAt)) {
                maxColumnC = ExcelSheetReader.toIndentNumber(valueColumnEndsAt);
            }
            org.apache.poi.ss.usermodel.Cell headerCell = row.getCell(headerColumnIndex);
            if(headerCell == null){
                continue;
            }
            String headerString = headerCell.getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            if(!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders()) {
                ignoreHeaderRows.add(r);
                continue;
            }
            if(ignoreHeaders.contains(headerString)) {
                ignoreHeaderRows.add(r);
                continue;
            }
            sheetHeaders.add(headerString);
            headerString = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, headerColumnIndex, r, headerStringList);
            headerRowIndexKeyedHeaderValueMap.put(r, headerString);
            headerStringList.add(headerString);
        }

        int valueColumnBeginsAt = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        int cIndex = valueColumnBeginsAt;
        while(cIndex < maxColumnC) {
            Map<String, CellInfo> headerKeyCellInfoMap = orderedOrUnorderedMap(sheet);
            for (int r = headerRowIndex; r <= totalRows; r++) {
                if (ignoreHeaderRows.contains(r)) {
                    continue;
                }
                Row row = workbookSheet.getRow(r);
                if(row == null){
                    continue;
                }
                String headerString = headerRowIndexKeyedHeaderValueMap.get(r);
                if(StringUtils.isEmpty(headerString)){
                    continue;
                }
                for (int c = cIndex ; c < cIndex + 1; c++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                    CellInfo cellInfo = new CellInfo();
                    cellInfo.setRowIndex(r);
                    cellInfo.setColumnIndex(c);
                    if(cell == null){
                        cellInfo.setValue(null);
                        headerKeyCellInfoMap.put(headerString, cellInfo);
                        continue;
                    }
                    Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                    cellInfo.setValue(cellValue);
                    headerKeyCellInfoMap.put(headerString, cellInfo);
                }
            }
            columnIndexKeyedHeaderKeyCellInfoMap.put(cIndex, headerKeyCellInfoMap);
            cIndex++;
        }

        Field finalDynamicCellField = dynamicCellField;
        Map<Cell, Field> finalHeaderCellFieldMap = headerCellFieldMap;
        columnIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueColumnBeginsAt)
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
                    catch (Exception ignored) {

                    }
                });
    }

    private void _concurrentRead(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Object headerKeyFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalColumns = sheetInfo.totalColumns();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalColumns, batchSize);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            Integer from = rowList.get(0);
            Integer to = rowList.get(rowList.size() - 1);
            int valueColumnIndex = sheetInfo.valueColumnIndex();
            if(valueColumnIndex > from ) {
                from = valueColumnIndex;
            }
            context.setValueColumnBeginsAt(ExcelSheetReader.toIndentName(from + 1));
            context.setValueColumnEndsAt(ExcelSheetReader.toIndentName(to + 1));
            service.toContext(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, columnIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
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
