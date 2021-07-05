package com.github.salilvnair.excelprocessor.v2.service.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v1.test.ExcelProcessorTestSuite;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.service.core.ExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelReader implements ExcelProcessor {

    private InputStream resourceStream(String folder, String fileName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return classLoader.getResourceAsStream(folder+"/"+fileName);
    }

    @Override
    public final Map<String, List<? extends BaseExcelSheet>> read(String[] fullyQualifiedClassNames) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        for (String clazzName : fullyQualifiedClassNames) {
            Class<? extends BaseExcelSheet> clazz = Class.forName(clazzName).asSubclass(BaseExcelSheet.class);
            ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
            List<? extends BaseExcelSheet> sheetData = read(clazz);
            excelSheets.put(excelSheet.value(), sheetData);
        }
        return excelSheets;
    }

    @Override
    public Map<String, List<? extends BaseExcelSheet>> read(Class<? extends BaseExcelSheet>[] classes) throws Exception {
        Map<String, List<? extends BaseExcelSheet>> excelSheets = new HashMap<>();
        for (Class<? extends BaseExcelSheet> clazz : classes) {
            ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
            List<? extends BaseExcelSheet> sheetData = read(clazz);
            excelSheets.put(excelSheet.value(), sheetData);
        }
        return excelSheets;
    }

    @Override
    public final <T extends BaseExcelSheet> List<T> read(Class<T> clazz) throws Exception {
        InputStream inputS = resourceStream(ExcelProcessorTestSuite.TEST_EXCEL_FOLDER, "ExcelProcessorTest.xls");
        Workbook workbook = findWorkbook(inputS, "ExcelProcessorTest.xls");
        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        int headerRowIndex = excelSheet.headerRowAt() - 1;
        Sheet sheet = workbook.getSheet(excelSheet.value());
        int pnR = sheet.getPhysicalNumberOfRows();
        Map<Integer, Map<String, CellInfo>> rowMap = new LinkedHashMap<>();
        Row headerRow = sheet.getRow(headerRowIndex);
        Map<Integer, String> indexKeyMap = new HashMap<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            indexKeyMap.put(i, headerRow.getCell(i).getStringCellValue());
        }

        for (int r = 0; r < pnR; r++) {
            if(r == headerRowIndex) {
                continue;
            }
            Map<String, CellInfo> headerValueMap = new HashMap<>();
            Row row = sheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            for (int c = 0; c < pnC; c++) {
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(r);
                cellInfo.setColumnIndex(c);
                String headerKey = indexKeyMap.get(c);
                Cell cell = row.getCell(c);
                if(cell == null){
                    continue;
                }
                Object cellValue = null;
                int cellType = cell.getCellType();
                switch (cellType) {
                    case Cell.CELL_TYPE_STRING:
                        cellValue = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        cellValue = cell.getNumericCellValue();
                }
                cellInfo.setValue(cellValue);
                headerValueMap.put(headerKey, cellInfo);
            }
            rowMap.put(r, headerValueMap);
        }

        Set<Field> excelHeaders = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
        Map<String, Field> headerFieldMap = excelHeaders.stream().collect(Collectors.toMap(excelHeader -> {
            ExcelHeader excelHeaderAnn = excelHeader.getAnnotation(ExcelHeader.class);
            return excelHeaderAnn.value();
        }, excelHeader -> excelHeader, (o, n) -> n));
        List<T> sheetData = new ArrayList<>();
        rowMap.forEach((key, value) -> {
            try {
                T classObject = clazz.getConstructor().newInstance();
                headerFieldMap.forEach((headerKey, field) -> {
                    CellInfo cellInfo = value.get(headerKey);
                    ReflectionUtil.setField(classObject, field, cellInfo.getValue());
                });
                sheetData.add(classObject);
            }
            catch (Exception ignored) {}
        });
        return sheetData;
    }

    private Workbook findWorkbook(InputStream inputStream, String excelFilePath)
            throws Exception {
        Workbook workbook;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        }
        else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        }
        else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }


}
