package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
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
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
        if (workbook == null) {
            return;//change it to exception later on
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Object headerCellFieldMapOrDynamicCellField = null;
        if(!sheet.dynamicHeaders()) {
            Map<Cell, Field> cellFieldMap = headerCellFieldMap(context, sheet, excelHeaders);
            headerCellFieldMapOrDynamicCellField = cellFieldMap;
            context.setHeaderCellFieldMap(cellFieldMap);
        }
        else {
            headerCellFieldMapOrDynamicCellField = dynamicCellField(clazz);
        }
        Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = new HashMap<>();
        List<BaseSheet> baseSheetList = new ArrayList<>();
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = new HashMap<>();
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

    protected Map<Cell, Field> headerCellFieldMap(ExcelSheetReaderContext context, Sheet sheet, Set<Field> excelHeaders) {
        return Collections.emptyMap();
    }

    protected Field dynamicCellField(Class<? extends BaseSheet> clazz) {
        return null;
    }

    protected BaseSheet dynamicCellValueResolver(Class<? extends BaseSheet> clazz, List<String> headerStringList, Map<String, CellInfo> excelCellInfoMap, int key, Field headerDynamicCellField) throws InstantiationException, IllegalAccessException {
        return null;
    }

    protected BaseSheet cellValueResolver(Class<? extends BaseSheet> clazz, Map<String, CellInfo> excelCellInfoMap, int key, Map<Cell, Field> headerCellFieldMap) throws InstantiationException, IllegalAccessException {
        return null;
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return null;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
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
        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        context.setSheet(excelSheet);
        if(excelSheet.dynamicHeaders()) {
            dynamicCellField = (Field) headerCellFieldMapOrDynamicCellField;
        }
        else {
            headerCellFieldMap = (Map<Cell, Field>) headerCellFieldMapOrDynamicCellField;
        }
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        int totalRows = sheet.getLastRowNum();
        Row headerRow = sheet.getRow(headerRowIndex);
        List<String> headerStringList = new LinkedList<>();
        for (int i = headerColumnIndex; i < headerRow.getLastCellNum(); i++) {
            String headerString = headerRow.getCell(i).getStringCellValue();
            headerString = cleanAndProcessSimilarHeaderString(headerString, clazz, i, headerRowIndex, headerStringList);
            headerColumnIndexKeyedHeaderValueMap.put(i, headerString);
            headerStringList.add(headerString);
        }
        int valueRowBeginsAt = context.valueRowBeginsAt()!=-1 ? context.valueRowBeginsAt() : excelSheet.valueRowBeginsAt();
        int valueRowIndex = valueRowBeginsAt!=-1 ? valueRowBeginsAt: excelSheet.valueRowAt()!=-1 ? excelSheet.valueRowAt() : headerRowIndex+1;
        int valueEndsAt = context.valueRowEndsAt()!=-1 ? context.valueRowEndsAt() : excelSheet.valueRowBeginsAt();
        totalRows = valueEndsAt!=-1 ? valueEndsAt : totalRows;
        for (int r = valueRowIndex ; r <= totalRows; r++) {
            Map<String, CellInfo> headerKeyCellInfoMap = new HashMap<>();
            Row row = sheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            for (int c = headerColumnIndex; c < pnC; c++) {
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(r);
                cellInfo.setColumnIndex(c);
                String headerString = headerColumnIndexKeyedHeaderValueMap.get(c);
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
                        if(excelSheet.dynamicHeaders()) {
                            classObject = dynamicCellValueResolver(clazz, headerStringList, value, key, finalDynamicCellField);
                        }
                        else {
                            classObject = cellValueResolver(clazz, value, key, finalHeaderCellFieldMap);
                        }
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
