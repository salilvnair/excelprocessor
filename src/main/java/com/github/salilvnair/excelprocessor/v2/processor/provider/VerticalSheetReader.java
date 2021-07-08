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
import org.apache.commons.lang3.StringUtils;
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
public class VerticalSheetReader extends BaseExcelSheetReader {
    private final boolean concurrent;
    public VerticalSheetReader(boolean concurrent) {
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



        Map<Integer, String> headerRowIndexKeyedHeaderValueMap = new HashMap<>();
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = new LinkedHashMap<>();
        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, Field> headerKeyFieldMap = excelHeaders.stream().collect(Collectors.toMap(excelHeader -> {
            Cell cellAnn = excelHeader.getAnnotation(Cell.class);
            return cellAnn.value();
        }, excelHeader -> excelHeader, (o, n) -> n));
        List<BaseExcelSheet> baseSheetList = new ArrayList<>();
        if(concurrent) {
            _concurrentRead(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
        }
        else {
            _read(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, colIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
        }
        context.setHeaderRowIndexKeyedHeaderValueMap(headerRowIndexKeyedHeaderValueMap);
        context.setHeaderKeyFieldMap(headerKeyFieldMap);
        context.setColIndexKeyedHeaderKeyCellInfoMap(colIndexKeyedHeaderKeyCellInfoMap);
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
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        SheetInfo sheetInfo = new SheetInfo();
        int totalRows = sheet.getLastRowNum();
        sheetInfo.setTotalRows(totalRows);
        sheetInfo.setName(sheetName);
        Row row = sheet.getRow(headerRowIndex);
        if(row !=null) {
            int totalColumns = row.getLastCellNum();
            sheetInfo.setTotalColumns(totalColumns);
        }
        excelInfo.sheets().add(sheetInfo);
        return excelInfo;
    }

    protected void _read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseExcelSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Map<String, Field> headerKeyFieldMap) {
        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        context.setSheet(excelSheet);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        int totalRows = sheet.getLastRowNum();
        totalRows = excelSheet.valueRowEndsAt()!=-1 ? excelSheet.valueRowEndsAt() - 1 : totalRows;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;
        String valueColumnAt = !StringUtils.isEmpty(context.valueColumnBeginsAt()) ? context.valueColumnBeginsAt() : excelSheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        int maxColumnC = 0;
        for (int r = headerRowIndex; r <= totalRows; r++) {
            Row row = sheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            if(maxColumnC < pnC ) {
                maxColumnC = pnC;
            }
            String valueColumnEndsAt = context.valueColumnEndsAt() != null ? context.valueColumnEndsAt() : excelSheet.valueColumnEndsAt();
            if(!StringUtils.isEmpty(valueColumnEndsAt)) {
                maxColumnC = ExcelSheetReader.toIndentNumber(valueColumnEndsAt);
            }
            for (int c = headerColumnIndex ; c < headerColumnIndex+1; c++) {
                org.apache.poi.ss.usermodel.Cell headerCell = row.getCell(headerColumnIndex);
                if(headerCell == null){
                    continue;
                }
                headerRowIndexKeyedHeaderValueMap.put(r, headerCell.getStringCellValue());
            }
        }

        int valueColumnBeginsAt = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        int cIndex = valueColumnBeginsAt;
        while(cIndex < maxColumnC) {
            Map<String, CellInfo> headerKeyCellInfoMap = new HashMap<>();
            for (int r = headerRowIndex; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if(row == null){
                    continue;
                }
                String headerKey = headerRowIndexKeyedHeaderValueMap.get(r);
                for (int c = cIndex ; c < cIndex + 1; c++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                    CellInfo cellInfo = new CellInfo();
                    cellInfo.setRowIndex(r);
                    cellInfo.setColumnIndex(c);
                    if(cell == null){
                        cellInfo.setValue(null);
                        headerKeyCellInfoMap.put(headerKey, cellInfo);
                        continue;
                    }
                    Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                    cellInfo.setValue(cellValue);
                    headerKeyCellInfoMap.put(headerKey, cellInfo);
                }
            }
            columnIndexKeyedHeaderKeyCellInfoMap.put(cIndex, headerKeyCellInfoMap);
            cIndex++;
        }

        columnIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueColumnBeginsAt)
                .forEach(entry -> {
                    try {
                        int key = entry.getKey();
                        Map<String, CellInfo> value = entry.getValue();
                        BaseExcelSheet classObject = clazz.getConstructor().newInstance();
                        classObject.setColumnIndex(key);
                        classObject.setColumn(ExcelSheetReader.toIndentName(key+1));
                        headerKeyFieldMap.forEach((headerKey, field) -> {
                            CellInfo cellInfo = value.get(headerKey);
                            if(cellInfo!=null) {
                                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(),  field.getType());
                                ReflectionUtil.setField(classObject, field, fieldValue);
                            }
                        });
                        baseSheetList.add(classObject);
                    }
                    catch (Exception ignored) {

                    }
                });
    }

    private void _concurrentRead(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseExcelSheet> baseSheetList,  Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Map<String, Field> headerKeyFieldMap) {
        ExcelInfo excelInfo = excelInfo(clazz, context);
        SheetInfo sheetInfo = excelInfo.sheets().get(0);
        int totalColumns = sheetInfo.totalColumns();
        Stream<List<Integer>> rowListStream = ConcurrentUtil.split(totalColumns, 1000);
        List<List<Integer>> rowBatchList = rowListStream.collect(Collectors.toList());
        List<Callable<ExcelSheetReaderContext>> taskCallables = new ArrayList<>();
        List<Future<ExcelSheetReaderContext>> futureList = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        ExcelSheetReaderTaskService service = new ExcelSheetReaderTaskService();
        for (List<Integer> rowList : rowBatchList) {
            Integer from = rowList.get(0);
            Integer to = rowList.get(rowList.size() - 1);
            context.setValueColumnBeginsAt(ExcelSheetReader.toIndentName(from + 1));
            context.setValueColumnEndsAt(ExcelSheetReader.toIndentName(to + 1));
            service.toContext(TaskType.READ_MULTIPLE_ROWS_OR_COLUMNS.name(), null, clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, columnIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
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
