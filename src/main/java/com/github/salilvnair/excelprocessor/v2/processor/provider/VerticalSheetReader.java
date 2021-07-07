package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class VerticalSheetReader extends BaseExcelSheetReader {

    @Override
    public void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
        if (workbook == null) {
            return;//change it to exception later on
        }

        Sheet excelSheet = clazz.getAnnotation(Sheet.class);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        int totalRows = sheet.getLastRowNum();
        totalRows = excelSheet.valueRowEndsAt()!=-1 ? excelSheet.valueRowEndsAt() - 1 : totalRows;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;

        Map<Integer, String> headerRowIndexKeyedHeaderValueMap = new HashMap<>();

        //headerRowIndex = 2, headerColumnIndex = B (1)
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
            if(!StringUtils.isEmpty(excelSheet.valueColumnEndsAt())) {
                maxColumnC = ExcelSheetReader.toIndentNumber(excelSheet.valueColumnEndsAt()) - 1;
            }
            for (int c = headerColumnIndex ; c < headerColumnIndex+1; c++) {
                org.apache.poi.ss.usermodel.Cell headerCell = row.getCell(headerColumnIndex);
                if(headerCell == null){
                    continue;
                }
                headerRowIndexKeyedHeaderValueMap.put(r, headerCell.getStringCellValue());
            }
        }
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = new LinkedHashMap<>();
        int cIndex = headerColumnIndex + 1;
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
            colIndexKeyedHeaderKeyCellInfoMap.put(cIndex, headerKeyCellInfoMap);
            cIndex++;
        }


        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, Field> headerKeyFieldMap = excelHeaders.stream().collect(Collectors.toMap(excelHeader -> {
            Cell cellAnn = excelHeader.getAnnotation(Cell.class);
            return cellAnn.value();
        }, excelHeader -> excelHeader, (o, n) -> n));
        List<BaseExcelSheet> baseSheetList = new ArrayList<>();
        colIndexKeyedHeaderKeyCellInfoMap.forEach((key, value) -> {
            try {
                BaseExcelSheet classObject = clazz.getConstructor().newInstance();
                classObject.setColumnIndex(key);
                classObject.setColumn(ExcelSheetReader.toIndentName(key+1));
                headerKeyFieldMap.forEach((headerKey, field) -> {
                    CellInfo cellInfo = value.get(headerKey);
                    if(cellInfo!=null) {
                        Object fieldValue = TypeConvertor.convert(cellInfo.getValue(), cellInfo.getCellType(),  field.getType());
                        ReflectionUtil.setField(classObject, field, fieldValue);
                    }
                });
                baseSheetList.add(classObject);
            }
            catch (Exception ignored) {

            }
        });
        context.setHeaderRowIndexKeyedHeaderValueMap(headerRowIndexKeyedHeaderValueMap);
        context.setHeaderKeyFieldMap(headerKeyFieldMap);
        context.setColIndexKeyedHeaderKeyCellInfoMap(colIndexKeyedHeaderKeyCellInfoMap);
        context.setSheetData(Collections.unmodifiableList(baseSheetList));
        context.setSheet(excelSheet);
    }
}
