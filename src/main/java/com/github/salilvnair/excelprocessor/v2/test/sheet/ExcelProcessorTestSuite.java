package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.util.StopWatch;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExcelProcessorTestSuite {
    public static void main(String[] args) throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate(true);
        String[] classes = {
//               "com.github.salilvnair.excelprocessor.v2.test.sheet.CountryStateInfoSheet",
               "com.github.salilvnair.excelprocessor.v2.test.sheet.EmployerSheet"
        };
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        builder.fileName("ExcelProcessorTest1.xls");
        InputStream inputS = ExcelSheetReader.resourceStream(com.github.salilvnair.excelprocessor.v1.test.ExcelProcessorTestSuite.TEST_EXCEL_FOLDER, "ExcelProcessorTest1.xlsx");
        Workbook workbook = ExcelSheetReader.generateWorkbook(inputS, "ExcelProcessorTest1.xlsx");
        builder.workbook(workbook);
        //List<CountryStateInfoSheet> countryStateInfoSheets = excelProcessor.read(CountryStateInfoSheet.class, context);
        StopWatch.start();
        ExcelSheetContext sheetContext = builder.build();
//        reader.readAndValidate(classes, sheetContext);
        reader.readAndValidate(classes, sheetContext);
        System.out.println("That took " + StopWatch.elapsed(TimeUnit.MILLISECONDS) + " millisecond(s)");
        //Map<String, List<CellValidationMessage>> excelValidationMessages = reader.validate(sheetMap, context);
        sheetContext.excelSheets().get("Employer").forEach(emp -> {
            EmployerSheet employer = (EmployerSheet)emp;
            System.out.println(employer.dynamicHeaderKeyedCellValueMap());
        });

    }
}
