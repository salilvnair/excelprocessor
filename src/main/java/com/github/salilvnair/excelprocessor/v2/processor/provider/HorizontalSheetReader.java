package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class HorizontalSheetReader extends BaseExcelSheetReader {
    @Override
    public void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        if(!validateWorkbook(context)) {
            return;//change it to exception later on
        }
        Workbook workbook = ExcelSheetReader.extractWorkbook(context);
        if (workbook == null) {
            return;//change it to exception later on
        }

        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(excelSheet.headerColumnAt())  - 1;
        String sheetName = context.sheetName() == null ? excelSheet.value(): context.sheetName();
        Sheet sheet = workbook.getSheet(sheetName);
        int totalRows = sheet.getLastRowNum();
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = new LinkedHashMap<>();
        Row headerRow = sheet.getRow(headerRowIndex);
        Map<Integer, String> headerColumnIndexKeyedHeaderValueMap = new HashMap<>();

        for (int i = headerColumnIndex; i < headerRow.getLastCellNum(); i++) {
            headerColumnIndexKeyedHeaderValueMap.put(i, headerRow.getCell(i).getStringCellValue());
        }
        int valueRowIndex = excelSheet.valueRowBeginsAt()!=-1 ? excelSheet.valueRowBeginsAt(): excelSheet.valueRowAt()!=-1 ? excelSheet.valueRowAt() : headerRowIndex+1;
        totalRows = excelSheet.valueRowEndsAt()!=-1 ? excelSheet.valueRowEndsAt() : totalRows;
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
                Cell cell = row.getCell(c);
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

        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
        Map<String, Field> headerKeyFieldMap = excelHeaders.stream().collect(Collectors.toMap(excelHeader -> {
            ExcelHeader excelHeaderAnn = excelHeader.getAnnotation(ExcelHeader.class);
            return excelHeaderAnn.value();
        }, excelHeader -> excelHeader, (o, n) -> n));
        List<BaseExcelSheet> baseSheetList = new LinkedList<>();
        rowIndexKeyedHeaderKeyCellInfoMap.forEach((key, value) -> {
            try {
                BaseExcelSheet classObject = clazz.asSubclass(BaseExcelSheet.class).newInstance();
                classObject.setRowIndex(key);
                headerKeyFieldMap.forEach((headerKey, field) -> {
                    CellInfo cellInfo = value.get(headerKey);
                    if(cellInfo!=null) {
                        Object fieldValue = TypeConvertor.convert(cellInfo.getValue(), cellInfo.getCellType(), field.getType());
                        ReflectionUtil.setField(classObject, field, fieldValue);
                    }
                });
                baseSheetList.add(classObject);
            }
            catch (Exception ignored) {}
        });
        context.setHeaderColumnIndexKeyedHeaderValueMap(headerColumnIndexKeyedHeaderValueMap);
        context.setHeaderKeyFieldMap(headerKeyFieldMap);
        context.setRowIndexKeyedHeaderKeyCellInfoMap(rowIndexKeyedHeaderKeyCellInfoMap);
        context.setSheetData(Collections.unmodifiableList(baseSheetList));
        context.setExcelSheet(excelSheet);
    }
}
