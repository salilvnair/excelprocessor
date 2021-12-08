package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReadException;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.task.ExcelSheetReaderTask;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
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
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Validation Failed");//TODO v2: change it to exception later on
            }
            return;
        }
        Workbook workbook = ExcelSheetReaderUtil.extractWorkbook(context);
        if (workbook == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Workbook Null");//TODO v2: change it to exception later on
            }
            return;
        }



        Sheet sheet = clazz.getAnnotation(Sheet.class);
        context.setSheet(sheet);
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
        List<BaseSheet> baseSheetList = context.concurrentSheetData();
        Map<Integer, String> headerRowIndexKeyedHeaderValueMap = context.headerRowIndexKeyedHeaderValueMap();
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = context.colIndexKeyedHeaderKeyCellInfoMap();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        context.setSheetData(Collections.unmodifiableList(baseSheetList));
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Invalid workbook."); //TODO v2: change to a constant
            }
            return null;
        }
        Workbook workbook = ExcelSheetReaderUtil.extractWorkbook(context);
        if (workbook == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Workbook is null."); //TODO v2: change to a constant
            }
            return null;
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
        if(sheet == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Sheet '"+sheetName + "' is not present in the excel.");
            }
            return null;
        }
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
        totalRows = sheet.headerRowEndsAt()!=-1 ? sheet.headerRowEndsAt() - 1 : totalRows;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt())  - 1;
        String valueColumnAt = !StringUtils.isEmpty(context.valueColumnBeginsAt()) ? context.valueColumnBeginsAt() : sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        int maxColumnC = 0;
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<String> ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.stream(sheet.ignoreHeaderPatterns()).collect(Collectors.toList()) : context.ignoreHeaderPatterns();
        List<Integer> ignoreHeaderRows = context.ignoreHeaderRows().stream().map(r -> r-1).collect(Collectors.toList());
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, String> processedDuplicateHeaderKeyedOriginalHeaderMap = orderedOrUnorderedMap(sheet);
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
            if(ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)) {
                ignoreHeaderRows.add(r);
                continue;
            }
            if(StringUtils.isEmpty(headerString)){
                continue;
            }
            sheetHeaders.add(headerString);
            String processSimilarHeaderString = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, headerColumnIndex, r, headerStringList);
            headerRowIndexKeyedHeaderValueMap.put(r, processSimilarHeaderString);
            processedDuplicateHeaderKeyedOriginalHeaderMap.put(processSimilarHeaderString, headerString);
            headerStringList.add(processSimilarHeaderString);
        }

        int valueColumnBeginsAt = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        int cIndex = valueColumnBeginsAt;
        while(cIndex < maxColumnC) {
            Map<String, CellInfo> headerKeyCellInfoMap = orderedOrUnorderedMap(sheet);
            totalRows = sheet.valueRowEndsAt() > -1 ? sheet.valueRowEndsAt() - 1 : totalRows;
            int valueRowBeginsAt = sheet.valueRowBeginsAt() > - 1 ? sheet.valueRowBeginsAt() -1 : -1;
            int valueRowIndex = valueRowBeginsAt > -1 ? valueRowBeginsAt: sheet.valueRowAt() > -1 ? sheet.valueRowAt() - 1 : headerRowIndex;
            for (int r = valueRowIndex; r <= totalRows; r++) {
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
                    cellInfo.setRow(r+1);
                    cellInfo.setColumnIndex(c);
                    cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                    cellInfo.setHeader(headerString);
                    cellInfo.setOriginalHeader(processedDuplicateHeaderKeyedOriginalHeaderMap.get(headerString));
                    if(cell == null){
                        cellInfo.setValue(null);
                        headerKeyCellInfoMap.put(headerString, cellInfo);
                        continue;
                    }
                    Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                    extractCellPropertiesAndSetCellInfo(workbook, cell, cellInfo);
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
                    catch (Exception e) {
                        if(!context.suppressExceptions()) {
                            throw new ExcelSheetReadException(e);
                        }
                    }
                });
    }

    private void _concurrentRead(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Object headerKeyFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        if(excelInfo == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("ExcelInfo is null."); //TODO v2: change to a constant
            }
            return;
        }
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalColumns = sheetInfo.totalColumns();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalColumns, batchSize);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            ExcelSheetReaderContext taskSheetReaderContext = new ExcelSheetReaderContext();
            taskSheetReaderContext.setSheet(context.sheet());
            taskSheetReaderContext.setSheetName(context.sheetName());
            Integer from = rowList.get(0);
            Integer to = rowList.get(rowList.size() - 1);
            int valueColumnIndex = sheetInfo.valueColumnIndex();
            if(valueColumnIndex > from ) {
                from = valueColumnIndex;
            }
            taskSheetReaderContext.setValueColumnBeginsAt(ExcelSheetReader.toIndentName(from + 1));
            taskSheetReaderContext.setValueColumnEndsAt(ExcelSheetReader.toIndentName(to + 1));
            ExcelSheetReaderTask task = new ExcelSheetReaderTask(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, service, clazz, taskSheetReaderContext, workbook, taskSheetReaderContext.concurrentSheetData(), taskSheetReaderContext.headerRowIndexKeyedHeaderValueMap(), taskSheetReaderContext.colIndexKeyedHeaderKeyCellInfoMap(), headerKeyFieldMap);
            taskCallables.add(task);
        }
        try {
            List<Future<ExcelSheetReaderContext>> futureList = executor.invokeAll(taskCallables);
            int counter = 0;
            for(Future<ExcelSheetReaderContext> futureContext : futureList) {
                ExcelSheetReaderContext readerContext = futureContext.get();
                headerRowIndexKeyedHeaderValueMap.putAll(readerContext.headerRowIndexKeyedHeaderValueMap());
                columnIndexKeyedHeaderKeyCellInfoMap.putAll(readerContext.colIndexKeyedHeaderKeyCellInfoMap());
                baseSheetList.addAll(readerContext.concurrentSheetData());
                counter++;
            }
             if(taskCallables.size() == counter) {
                executor.shutdown();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException(e);
            }
        }
        executor.shutdown();
    }
}
