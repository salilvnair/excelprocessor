package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
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
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Salil V Nair
 */
public class HorizontalSheetReader extends BaseExcelSheetReader {
    private final boolean concurrent;
    public HorizontalSheetReader(boolean concurrent) {
        this.concurrent = concurrent;
    }
    @Override
    public void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
        if (workbook == null) {
            return;//change it to exception later on
        }

        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, Field> headerKeyFieldMap = excelHeaders.stream().collect(Collectors.toMap(excelHeader -> {
            Cell cellAnn = excelHeader.getAnnotation(Cell.class);
            return cellAnn.value();
        }, excelHeader -> excelHeader, (o, n) -> n));

        Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = new HashMap<>();
        List<BaseExcelSheet> baseSheetList = new ArrayList<>();
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = new HashMap<>();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
        }

        context.setHeaderColumnIndexKeyedHeaderValueMap(headerColumnIndexKeyedHeaderValueMap);
        context.setHeaderKeyFieldMap(headerKeyFieldMap);
        context.setRowIndexKeyedHeaderKeyCellInfoMap(rowIndexKeyedHeaderKeyCellInfoMap);
        context.setSheetData(Collections.unmodifiableList(baseSheetList));
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return null;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
        if (workbook == null) {
            return null;//change it to exception later on
        }
        ExcelInfo excelInfo = new ExcelInfo();
        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        SheetInfo sheetInfo = new SheetInfo();
        int totalRows = sheet.getLastRowNum();
        sheetInfo.setTotalRows(totalRows);
        sheetInfo.setName(sheetName);
        Row row = sheet.getRow(0);
        if(row !=null) {
            int totalColumns = row.getLastCellNum();
            sheetInfo.setTotalColumns(totalColumns);
        }
        excelInfo.sheets().add(sheetInfo);
        return excelInfo;
    }

    protected void _read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseExcelSheet> baseSheetList,  Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Map<String, Field> headerKeyFieldMap) {
        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        context.setSheet(excelSheet);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        int totalRows = sheet.getLastRowNum();
        Row headerRow = sheet.getRow(headerRowIndex);

        for (int i = headerColumnIndex; i < headerRow.getLastCellNum(); i++) {
            headerColumnIndexKeyedHeaderValueMap.put(i, headerRow.getCell(i).getStringCellValue());
        }
        int valueRowBeginsAt = context.valueRowBeginsAt()!=-1 ? context.valueRowBeginsAt() : excelSheet.valueRowBeginsAt();
        int valueEndsAt = context.valueRowEndsAt()!=-1 ? context.valueRowEndsAt() : excelSheet.valueRowBeginsAt();
        int valueRowIndex = valueRowBeginsAt!=-1 ? valueRowBeginsAt: excelSheet.valueRowAt()!=-1 ? excelSheet.valueRowAt() : headerRowIndex+1;
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
                String headerKey = headerColumnIndexKeyedHeaderValueMap.get(c);
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                if(cell == null){
                    cellInfo.setValue(null);
                    headerKeyCellInfoMap.put(headerKey, cellInfo);
                    continue;
                }
                Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                cellInfo.setValue(cellValue);
                headerKeyCellInfoMap.put(headerKey, cellInfo);
            }
            rowIndexKeyedHeaderKeyCellInfoMap.put(r, headerKeyCellInfoMap);
        }
        rowIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueRowBeginsAt)
                .forEach(entry -> {
                    try {
                        int key = entry.getKey();
                        Map<String, CellInfo> value = entry.getValue();
                        BaseExcelSheet classObject = clazz.asSubclass(BaseExcelSheet.class).newInstance();
                        classObject.setRowIndex(key);
                        classObject.setRow(key+1);
                        headerKeyFieldMap.forEach((headerKey, field) -> {
                            CellInfo cellInfo = value.get(headerKey);
                            if(cellInfo!=null) {
                                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
                                ReflectionUtil.setField(classObject, field, fieldValue);
                            }
                        });
                        baseSheetList.add(classObject);
                    }
                    catch (Exception ignored) {}
                });
    }

    private void _concurrentRead(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseExcelSheet> baseSheetList,  Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Map<String, Field> headerKeyFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalRows = sheetInfo.totalRows();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalRows, 100);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        List<Future<ExcelSheetReaderContext>> futureList = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            Integer from = rowList.get(0);
            Integer to = rowList.get(rowList.size() - 1);
            context.setValueRowBeginsAt(from+1);
            context.setValueRowEndsAt(to+1);
            service.toContext(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
        }
        try {
            futureList = executor.invokeAll(taskCallables);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(futureList!=null) {
            System.out.println("task done");
        }
        executor.shutdown();
    }
}
