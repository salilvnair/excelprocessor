package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.helper.StopWatch;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ExcelProcessorTestSuite {
    public static void main(String[] args) throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate();
        String[] classes = {
                "com.github.salilvnair.excelprocessor.v2.test.sheet.CountryStateInfoSheet"
        };
        ExcelSheetContext context = new ExcelSheetContext();
        context.setFileName("ExcelProcessorTest1.xls");
        InputStream inputS = ExcelSheetReader.resourceStream(com.github.salilvnair.excelprocessor.v1.test.ExcelProcessorTestSuite.TEST_EXCEL_FOLDER, "ExcelProcessorTest1.xlsx");
        Workbook workbook = ExcelSheetReader.generateWorkbook(inputS, "ExcelProcessorTest1.xlsx");
        context.setWorkbook(workbook);
        //List<CountryStateInfoSheet> countryStateInfoSheets = excelProcessor.read(CountryStateInfoSheet.class, context);
        StopWatch.start();
        Map<String, List<? extends BaseExcelSheet>> sheetMap = reader.read(classes, context);
        System.out.println("That took " + StopWatch.stop() + " milliseconds");
        //Map<String, List<ValidationMessage>> excelValidationMessages = reader.validate(sheetMap, context);
        //System.out.println(excelValidationMessages);
    }
}
