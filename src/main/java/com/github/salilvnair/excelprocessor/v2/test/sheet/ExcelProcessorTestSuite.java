package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.util.StopWatch;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.generator.service.ExcelSheetClassGenerator;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExcelProcessorTestSuite {
    public static void main(String[] args) throws Exception {
        List<? extends BaseSheet> sheetData = sheetReader();
        //sheetWriter(sheetData, new ExcelSheetContext());
        //generateClassTemplate();
    }

    private static void sheetWriter(List<? extends BaseSheet> sheetData, ExcelSheetContext context) throws Exception {
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        writer.write(sheetData, context);
    }

    private static void generateClassTemplate() throws Exception {
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        builder.fileName("ExcelProcessorTest1.xlsx");
        InputStream inputS = ExcelSheetReaderUtil.resourceStream(com.github.salilvnair.excelprocessor.v1.test.ExcelProcessorTestSuite.TEST_EXCEL_FOLDER, "ExcelProcessorTest1.xlsx");
        Workbook workbook = ExcelSheetReaderUtil.generateWorkbook(inputS, "ExcelProcessorTest1.xlsx");
        builder.workbook(workbook);
        ExcelSheetContext sheetContext = builder.build();
        SheetInfo.SheetInfoBuilder sheetInfoBuilder = SheetInfo.builder();
        sheetInfoBuilder
                .name("SectionSheet")
                .vertical()
                .headerRowAt(1)
                .ignoreHeaderPatterns(
                    "Section"
                )
                .headerColumnAt("B");
        System.out.println(ExcelSheetClassGenerator.generate(sheetContext, sheetInfoBuilder.build()));
    }

    private static List<? extends BaseSheet> sheetReader() throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate(true);
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        builder.fileName("ExcelProcessorTest1.xlsx");
        InputStream inputS = ExcelSheetReaderUtil.resourceStream(com.github.salilvnair.excelprocessor.v1.test.ExcelProcessorTestSuite.TEST_EXCEL_FOLDER, "ExcelProcessorTest1.xlsx");
        Workbook workbook = ExcelSheetReaderUtil.generateWorkbook(inputS, "ExcelProcessorTest1.xlsx");
        builder.workbook(workbook);
        StopWatch.start();
        ExcelSheetContext sheetContext = builder.build();
        List<SectionSheet> sheetData = reader.read(SectionSheet.class, sheetContext);
        System.out.println(sheetData.size());
        System.out.println(sheetData.get(0).rowForegroundRgb());
        System.out.println(sheetData.get(1).rowForegroundRgb());
        System.out.println(Arrays.toString(sheetData.get(0).foregroundRgb()));
        System.out.println(Arrays.toString(sheetData.get(1).foregroundRgb()));
        List<CellValidationMessage> validationMessages = reader.validate(sheetData, sheetContext);
        System.out.println("validationMessages:"+validationMessages.size());
        System.out.println("excelprocessor v2 took " + StopWatch.elapsed(TimeUnit.MILLISECONDS) + " millisecond(s)");
        return sheetData;
    }
}
