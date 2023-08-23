package com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic;

import com.github.salilvnair.excelprocessor.util.MapGenerator;
import com.github.salilvnair.excelprocessor.util.StopWatch;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.model.AllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task.DynamicSheetTask;
import com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic.task.DynamicSheetValidatorTask;

import java.io.File;
import java.util.*;

public class DynamicSheetTestSuite {
    public static void main(String[] args) throws Exception {
//        write();
        read();
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
        String path = "/Users/salilvnair/Workspace/Personal/github/salilvnair/excelprocessor/src/main/resources/excel";
        File template = new File(path+ "/"+"ExcelProcessorTestTemplate.xlsx");
        Map<String, HeaderCellStyleInfo> headerCellStyleInfoMap = new HashMap<>();
        HeaderCellStyleInfo headerCellStyleInfo = new HeaderCellStyleInfo();
        headerCellStyleInfo.getStyleTemplateCellInfo().setRow(1);
        headerCellStyleInfo.getStyleTemplateCellInfo().setColumn("B");
        headerCellStyleInfoMap.put("university", headerCellStyleInfo);
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                                                .builder()
                                                .filePath(path)
                                                .fileName("DynamicallyGeneratedExcelProcessorTest.xlsx")
                                                .styleTemplate(template)
                                                .dynamicHeaderCellStyleInfo(headerCellStyleInfoMap)
                                                .taskBean(new DynamicSheetTask())
                                                .taskMetadata(headerCellStyleInfoMap, "Test1", "Test2")
                                                .build();
        Map<String, String> dynamicHeaderDisplayNames = prepareDynamicHeaderDisplayNames();
        excelSheetContext.setDynamicHeaderDisplayNames(dynamicHeaderDisplayNames);
        List<DynamicCollegeSheet> dynamicCollegeSheets = prepareDynamicCollegeSheet();
        sheetWriter(dynamicCollegeSheets, excelSheetContext);
    }

    private static void sheetWriter(List<? extends BaseSheet> sheetData, ExcelSheetContext context) throws Exception {
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        writer.write(sheetData, context);
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
        dynamicCollegeSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicCollegeSheets.add(dynamicCollegeSheet);

        dynamicCollegeSheet = new DynamicCollegeSheet();
        dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellValueMap.put("name", null);
        dynamicHeaderKeyedCellValueMap.put("university", "MIT");
        dynamicHeaderKeyedCellValueMap.put("state", null);
        dynamicHeaderKeyedCellValueMap.put("noOfStudents", 10000);
        dynamicCollegeSheet.setDynamicHeaderKeyedCellValueMap(dynamicHeaderKeyedCellValueMap);
        dynamicCollegeSheets.add(dynamicCollegeSheet);
        return dynamicCollegeSheets;
    }

}
