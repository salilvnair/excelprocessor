package com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic;

import com.github.salilvnair.excelprocessor.util.MapGenerator;
import com.github.salilvnair.excelprocessor.util.StopWatch;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.model.AllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task.DynamicSheetTask;
import com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task.DynamicSheetValidatorTask;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.*;

public class DynamicSheetTestSuite {
    public static void main(String[] args) throws Exception {
        write();
//        read();
    }

    public static List<? extends BaseSheet> read() throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate();
        ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
        String filePath = "/Users/salilvnair/Workspace/Personal/github/salilvnair/excelprocessor/src/main/resources/excel/DynamicallyGeneratedExcelProcessorTest.xlsx";
        builder.excelFile(new File(filePath));
        StopWatch.start();
        AllowedValuesInfo allowedValuesInfo = new AllowedValuesInfo().toBuilder().showValuesInMessage(true).allowNull(true).value(new String[]{"RJ", "KL"}).build();
        CellValidationInfo cellValidationInfo = new CellValidationInfo().toBuilder().required(true).allowedValuesInfo(allowedValuesInfo).build();
        Map<String, CellValidationInfo> cellValidationInfoMap = MapGenerator.mutable().generate("State", cellValidationInfo);

        CellValidationInfo noOfStudentsValidation = new CellValidationInfo().toBuilder().customTask("someCustomTask").build();
        cellValidationInfoMap.put("# of students", noOfStudentsValidation);
        //Map<String, FieldInfo> headerFieldInfo = MapGenerator.immutable().generate("# of students", new FieldInfo().toBuilder().type(FieldType.LONG.typeStringValue()).build());
        builder.headerKeyedCellValidationInfo(cellValidationInfoMap);
        //builder.headerFieldInfo(headerFieldInfo);
        builder.taskValidatorBean(new DynamicSheetValidatorTask());
        ExcelSheetContext sheetContext = builder.build();
        List<DynamicCollegeSheet> sheetData = reader.read(DynamicCollegeSheet.class, sheetContext);
        for (DynamicCollegeSheet collegeSheet : sheetData) {
            System.out.println(collegeSheet.extract());
            System.out.println(collegeSheet.extractCells());
        }
        List<CellValidationMessage> cellValidationMessages = reader.validate(sheetData, sheetContext);
        System.out.println(cellValidationMessages);
        return sheetData;
    }

    public static void write() throws Exception {
        String path = "/Users/salilvnair/Workspace/personal";
//        File template = new File(path+ "/"+"ExcelProcessorTestTemplate.xlsx");
        Map<String, HeaderCellStyleInfo> headerCellStyleInfoMap = new HashMap<>();
        HeaderCellStyleInfo headerCellStyleInfo = new HeaderCellStyleInfo();
        headerCellStyleInfo.setCustomTextStyle(true);
        headerCellStyleInfo.getTextStyleInfo().setColor(IndexedColors.RED);
        headerCellStyleInfoMap.put("university", headerCellStyleInfo);

        Map<String, DataCellStyleInfo> dataCellStyleInfoMap = new HashMap<>();
        DataCellStyleInfo dataCellStyleInfo = new DataCellStyleInfo();
        dataCellStyleInfo.setCustomTextStyle(true);
        dataCellStyleInfo.getTextStyleInfo().setColor(IndexedColors.GREY_50_PERCENT);
        dataCellStyleInfoMap.put("university", dataCellStyleInfo);
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                                                .builder()
                                                .filePath(path)
                                                .fileName("DynamicallyGeneratedExcelProcessorTest.xlsx")
//                                                .styleTemplate(template)
//                                                .template(template)
                                                .dynamicHeaderCellStyleInfo(headerCellStyleInfoMap)
                                                .dynamicHeaderDataCellStyleInfo(dataCellStyleInfoMap)
                                                .taskBean(new DynamicSheetTask())
                                                .taskMetadata(dataCellStyleInfoMap, "Test1", "Test2")
                                                .build();
//        Map<String, String> dynamicHeaderDisplayNames = prepareDynamicHeaderDisplayNames();
//        excelSheetContext.setDynamicHeaderDisplayNames(dynamicHeaderDisplayNames);
        List<DynamicCollegeSheet> dynamicCollegeSheets = prepareDynamicCollegeSheet();
        Map<String, List<? extends BaseSheet>> excelData = new HashMap<>();
        excelData.put("College", dynamicCollegeSheets);

        List<DynamicSchoolSheet> dynamicSchoolSheets = prepareDynamicSchoolSheet();
        excelData.put("School", dynamicSchoolSheets);
        sheetWriter(excelData, excelSheetContext);
    }

    private static void sheetWriter(Map<String, List<? extends BaseSheet>> excelData, ExcelSheetContext context) throws Exception {
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        Workbook workbook = writer.workbook(excelData, context);
        ExcelSheetWriterUtil.write(workbook, context.fileName(), context.filePath());
    }

    private static Map<String, String> prepareDynamicHeaderDisplayNames() {
        return MapGenerator
                .immutable()
                .generate("name", "Name",
                        "university", "University",
                        "state", "State",
                        "noOfStudents", "# of students");
    }

    private static List<DynamicCollegeSheet> prepareDynamicCollegeSheet() {
        List<DynamicCollegeSheet> dynamicCollegeSheets = new ArrayList<>();
        DynamicCollegeSheet dynamicCollegeSheet = new DynamicCollegeSheet();
        LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellValueMap.put("name", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents", 5000);
        dynamicHeaderKeyedCellValueMap.put("name1", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university1", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state1", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents1", 5000);
        dynamicHeaderKeyedCellValueMap.put("name2", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university2", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state2", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents2", 5000);
        dynamicHeaderKeyedCellValueMap.put("name3", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university3", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state3", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents3", 5000);
        dynamicCollegeSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicCollegeSheets.add(dynamicCollegeSheet);

        dynamicCollegeSheet = new DynamicCollegeSheet();
        dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellValueMap.put("name", null);
        dynamicHeaderKeyedCellValueMap.put("university", "MIT");
        dynamicHeaderKeyedCellValueMap.put("state", null);
        dynamicHeaderKeyedCellValueMap.put("noOfStudents", 10000);
        dynamicHeaderKeyedCellValueMap.put("name1", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university1", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state1", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents1", 5000);
        dynamicHeaderKeyedCellValueMap.put("name2", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university2", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state2", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents2", 5000);
        dynamicHeaderKeyedCellValueMap.put("name3", "Salil");
        dynamicHeaderKeyedCellValueMap.put("university3", "VMU");
        dynamicHeaderKeyedCellValueMap.put("state3", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents3", 5000);
        dynamicCollegeSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicCollegeSheets.add(dynamicCollegeSheet);


        for (int i = 0; i < 20000; i++) {
            dynamicCollegeSheet = new DynamicCollegeSheet();
            dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
            dynamicHeaderKeyedCellValueMap.put("name", "test"+i);
            dynamicHeaderKeyedCellValueMap.put("university", "MIT"+i);
            dynamicHeaderKeyedCellValueMap.put("state", null);
            dynamicHeaderKeyedCellValueMap.put("noOfStudents", i+10000);
            dynamicHeaderKeyedCellValueMap.put("name1", "test"+i);
            dynamicHeaderKeyedCellValueMap.put("university1", "MIT"+i);
            dynamicHeaderKeyedCellValueMap.put("state1", null);
            dynamicHeaderKeyedCellValueMap.put("noOfStudents1", i+10000);
            dynamicHeaderKeyedCellValueMap.put("name2", "test"+i);
            dynamicHeaderKeyedCellValueMap.put("university2", "MIT"+i);
            dynamicHeaderKeyedCellValueMap.put("state2", null);
            dynamicHeaderKeyedCellValueMap.put("noOfStudents2", i+10000);
            dynamicHeaderKeyedCellValueMap.put("name33", "test"+i);
            dynamicHeaderKeyedCellValueMap.put("university3", "MIT"+i);
            dynamicHeaderKeyedCellValueMap.put("state3", null);
            dynamicHeaderKeyedCellValueMap.put("noOfStudents3", i+10000);
            dynamicCollegeSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
            dynamicCollegeSheets.add(dynamicCollegeSheet);
        }
        return dynamicCollegeSheets;
    }

    private static List<DynamicSchoolSheet> prepareDynamicSchoolSheet() {
        List<DynamicSchoolSheet> dynamicSchoolSheets = new ArrayList<>();
        DynamicSchoolSheet dynamicSchoolSheet = new DynamicSchoolSheet();
        LinkedHashMap<String, Object> dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellValueMap.put("name", "Salil");
        dynamicHeaderKeyedCellValueMap.put("board", "CBSE");
        dynamicHeaderKeyedCellValueMap.put("state", "KA");
        dynamicHeaderKeyedCellValueMap.put("noOfStudents", 5000);
        dynamicSchoolSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicSchoolSheets.add(dynamicSchoolSheet);

        dynamicSchoolSheet = new DynamicSchoolSheet();
        dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellValueMap.put("name", null);
        dynamicHeaderKeyedCellValueMap.put("board", "ICSE");
        dynamicHeaderKeyedCellValueMap.put("state", null);
        dynamicHeaderKeyedCellValueMap.put("noOfStudents", 10000);
        dynamicSchoolSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicSchoolSheets.add(dynamicSchoolSheet);
        return dynamicSchoolSheets;
    }

}
