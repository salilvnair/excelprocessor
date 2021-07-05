package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.service.core.ExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.service.provider.ExcelReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.util.List;
import java.util.Map;

public class ExcelProcessorTestSuite {
    public static void main(String[] args) throws Exception {
        ExcelProcessor excelProcessor = new ExcelReader();
        String[] classes = {
                "com.github.salilvnair.excelprocessor.v2.test.sheet.SchoolSheet",
                "com.github.salilvnair.excelprocessor.v2.test.sheet.EmployerSheet",
                "com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet"
        };
        Map<String, List<? extends BaseExcelSheet>> sheetMap = excelProcessor.read(classes);
        System.out.println(sheetMap);
    }
}
