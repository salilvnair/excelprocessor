package com.github.salilvnair.excelprocessor.v2.test.archived.sheet;

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
import com.github.salilvnair.excelprocessor.v2.model.SheetInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExcelProcessorTestSuite {
    public static final String TEST_EXCEL_FOLDER = "excel";
    public static final String IMAGES_EXCEL_FOLDER = "images";
    public static void main(String[] args) throws Exception {

    }

    private static void pictureSheetWriter(List<? extends BaseSheet> sheetData, ExcelSheetContext excelSheetContext) throws Exception {
        InputStream inputS = ExcelSheetReaderUtil.resourceStream(IMAGES_EXCEL_FOLDER, "e.png");
        byte[] image1 = IOUtils.toByteArray(inputS);
        CollegeSheet collegeSheet = (CollegeSheet) sheetData.get(0);
        List<Byte[]> imageBytes = new ArrayList<>();
        imageBytes.add(ArrayUtils.toObject(image1));
        inputS = ExcelSheetReaderUtil.resourceStream(IMAGES_EXCEL_FOLDER, "box.png");
        byte[] image2 = IOUtils.toByteArray(inputS);
        imageBytes.add(ArrayUtils.toObject(image2));
        collegeSheet.setImages(imageBytes);
        collegeSheet.setImage(ArrayUtils.toObject(image1));
        sheetWriter(sheetData, excelSheetContext);
    }

    private static List<? extends BaseSheet> prepareCollegeSheet() {
        List<CollegeSheet> sheetData = new ArrayList<>();
        CollegeSheet collegeSheet = new CollegeSheet();
//        collegeSheet.setName("SRM");
        collegeSheet.setState("Chennai");
//        collegeSheet.setUniversity("Anna University");
//        collegeSheet.setNoOfStudents(5000L);
        collegeSheet.setUniversityHomepageURL("https://www.salilvnair.com, https://www.google.com");
        sheetData.add(collegeSheet);
        return sheetData;
    }

    private static void sheetWriter(List<? extends BaseSheet> sheetData, ExcelSheetContext context) throws Exception {
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        writer.write(sheetData, context);
    }

    private static void generateClassTemplate(String fileName) throws Exception {
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        builder.excelFile(new File("/Users/salilvnair/workspace/dbv/data.xlsx"));
        ExcelSheetContext sheetContext = builder.build();
        SheetInfo.SheetInfoBuilder sheetInfoBuilder = SheetInfo.builder();
        sheetInfoBuilder
                .name("Sheet1")
                .headerRowAt(2)
                .headerColumnAt("B");
        System.out.println(ExcelSheetClassGenerator.generate(sheetContext, sheetInfoBuilder.build()));
    }

    private static List<? extends BaseSheet> sheetReader() throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate();
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        builder.fileName("ExcelProcessorTest1.xlsx");
        InputStream inputS = ExcelSheetReaderUtil.resourceStream(TEST_EXCEL_FOLDER, "ExcelProcessorTest1.xlsx");
        Workbook workbook = ExcelSheetReaderUtil.generateWorkbook(inputS, "ExcelProcessorTest1.xlsx");
        builder.workbook(workbook);
        StopWatch watch = StopWatch.start();
        ExcelSheetContext sheetContext = builder.build();
        List<SectionSheet> sheetData = reader.read(SectionSheet.class, sheetContext);
        System.out.println(sheetData.size());
        System.out.println(sheetData.get(0).rowForegroundRgb());
        System.out.println(sheetData.get(1).rowForegroundRgb());
        System.out.println(Arrays.toString(sheetData.get(0).foregroundRgb()));
        System.out.println(Arrays.toString(sheetData.get(1).foregroundRgb()));
        List<CellValidationMessage> validationMessages = reader.validate(sheetData, sheetContext);
        System.out.println("validationMessages:"+validationMessages.size());
        System.out.println("excelprocessor v2 took " + StopWatch.elapsed(watch, TimeUnit.MILLISECONDS) + " millisecond(s)");
        return sheetData;
    }
}
