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
        context.setSheet(sheet);
        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Object headerCellFieldMapOrDynamicCellField;
        if(!sheet.dynamicHeaders()) {
            Map<Cell, Field> cellFieldMap = StaticHeaderSheetReader.headerCellFieldMap(context, sheet, excelHeaders);
            headerCellFieldMapOrDynamicCellField = cellFieldMap;
            context.setHeaderCellFieldMap(cellFieldMap);
        }
        else {
            headerCellFieldMapOrDynamicCellField = DynamicHeaderSheetReader.dynamicCellField(clazz);
        }
        Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = context.headerColumnIndexKeyedHeaderValueMap();
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = context.rowIndexKeyedHeaderKeyCellInfoMap();
        List<BaseSheet> baseSheetList = context.concurrentSheetData();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
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
        int valueRowBeginsAt = excelSheet.valueRowBeginsAt();
        int valueRowIndex = valueRowBeginsAt!=-1 ? valueRowBeginsAt: excelSheet.valueRowAt()!=-1 ? excelSheet.valueRowAt() : headerRowIndex+1;
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
        if(workbookSheet == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("Sheet '"+sheetName + "' is not present in the excel.");
            }
            return;
        }
        int totalRows = workbookSheet.getLastRowNum();
        Row headerRow = workbookSheet.getRow(headerRowIndex);
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        Map<String, String> processedDuplicateHeaderKeyedOriginalHeaderMap = orderedOrUnorderedMap(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<String> ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.stream(sheet.ignoreHeaderPatterns()).collect(Collectors.toList()) : context.ignoreHeaderPatterns();
        List<Integer> ignoreHeaderColumns = context.ignoreHeaderColumns().stream().map(col -> ExcelSheetReader.toIndentNumber(col) -1 ).collect(Collectors.toList());
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        List<String> classCellHeaders = sheetCells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).collect(Collectors.toList());
        int lastCellNum = StringUtils.isNotEmpty(sheet.headerColumnEndsAt()) ? ExcelSheetReader.toIndentNumber(sheet.headerColumnEndsAt()) - 1 : headerRow.getLastCellNum();
        for (int c = headerColumnIndex; c < lastCellNum; c++) {
            String headerString = headerRow.getCell(c).getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            if(!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders()) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            if(ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            if(ignoreHeaders.contains(headerString)) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            sheetHeaders.add(headerString);
            String processSimilarHeaderString = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, c, headerRowIndex, headerStringList);
            headerColumnIndexKeyedHeaderValueMap.put(c, processSimilarHeaderString);
            processedDuplicateHeaderKeyedOriginalHeaderMap.put(processSimilarHeaderString, headerString);
            headerStringList.add(processSimilarHeaderString);
        }
        int valueRowBeginsAt = context.valueRowBeginsAt() > -1 ? context.valueRowBeginsAt() - 1 : sheet.valueRowBeginsAt() > - 1 ? sheet.valueRowBeginsAt() -1 : -1;
        int valueRowIndex = valueRowBeginsAt > -1 ? valueRowBeginsAt: sheet.valueRowAt()!=-1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int valueEndsAt = context.valueRowEndsAt() > -1 ? context.valueRowEndsAt() - 1 : sheet.valueRowEndsAt() > - 1 ? sheet.valueRowEndsAt() - 1 : -1;
        totalRows = valueEndsAt!=-1 ? valueEndsAt : totalRows;
        for (int r = valueRowIndex ; r <= totalRows; r++) {
            Map<String, CellInfo> headerKeyCellInfoMap = orderedOrUnorderedMap(sheet);
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            for (int c = headerColumnIndex; c < lastCellNum; c++) {
                if(ignoreHeaderColumns.contains(c)) {
                    continue;
                }
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(r);
                cellInfo.setRow(r+1);
                cellInfo.setColumnIndex(c);
                cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                String headerString = headerColumnIndexKeyedHeaderValueMap.get(c);
                if(StringUtils.isEmpty(headerString)){
                    continue;
                }
                cellInfo.setHeader(headerString);
                cellInfo.setOriginalHeader(processedDuplicateHeaderKeyedOriginalHeaderMap.get(headerString));
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
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
                        int  rowIndexKey = entry.getKey();
                        Map<String, CellInfo> excelCellInfoMap = entry.getValue();
                        BaseSheet classObject;
                        if(sheet.dynamicHeaders()) {
                            classObject = DynamicHeaderSheetReader.dynamicCellValueResolver(clazz, headerStringList, excelCellInfoMap, rowIndexKey, finalDynamicCellField);
                        }
                        else {
                            classObject = StaticHeaderSheetReader.cellValueResolver(clazz, excelCellInfoMap,  rowIndexKey, finalHeaderCellFieldMap);
                        }
                        classObject.setSheetHeaders(sheetHeaders);
                        classObject.setCells(excelCellInfoMap);
                        baseSheetList.add(classObject);
                    }
                    catch (Exception e) {
                        if(!context.suppressExceptions()) {
                            throw new ExcelSheetReadException(e);
                        }
                    }
                });
    }

    private void _concurrentRead(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Object headerFieldOrFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        if(excelInfo == null) {
            if(!context.suppressExceptions()) {
                throw new ExcelSheetReadException("ExcelInfo is null."); //TODO v2: change to a constant
            }
            return;
        }
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalRows = sheetInfo.totalRows();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalRows, batchSize);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            ExcelSheetReaderContext taskSheetReaderContext = new ExcelSheetReaderContext();
            taskSheetReaderContext.setSheet(context.sheet());
            taskSheetReaderContext.setSheetName(context.sheetName());
            Integer from = rowList.get(0);
            int valueRowIndex = sheetInfo.valueRowIndex();
            if(valueRowIndex > from ) {
                from = valueRowIndex;
            }
            else {
                from = from + 1;
            }
            Integer to = rowList.get(rowList.size() - 1);
            taskSheetReaderContext.setValueRowBeginsAt(from);
            taskSheetReaderContext.setValueRowEndsAt(to+1);
            ExcelSheetReaderTask task = new ExcelSheetReaderTask(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, service, clazz, taskSheetReaderContext, workbook, taskSheetReaderContext.concurrentSheetData(), taskSheetReaderContext.headerColumnIndexKeyedHeaderValueMap(), taskSheetReaderContext.rowIndexKeyedHeaderKeyCellInfoMap(), headerFieldOrFieldMap);
            taskCallables.add(task);
        }
        try {
            List<Future<ExcelSheetReaderContext>> futureList = executor.invokeAll(taskCallables);
            int counter = 0;
            for(Future<ExcelSheetReaderContext> futureContext : futureList) {
                ExcelSheetReaderContext readerContext = futureContext.get();
                headerColumnIndexKeyedHeaderValueMap.putAll(readerContext.headerColumnIndexKeyedHeaderValueMap());
                rowIndexKeyedHeaderKeyCellInfoMap.putAll(readerContext.rowIndexKeyedHeaderKeyCellInfoMap());
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
    }
}
