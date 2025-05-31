package com.github.salilvnair.excelprocessor.v2.test.writer.main.notemplate;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoNoTemplateHorizontalSheetMock;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.UserInfoNoTemplateHorizontalSheetMock;
import org.apache.poi.ss.usermodel.Workbook;

public class NoTemplateHorizontalSheetNumberFormatTest {

    public static void main(String[] args) throws Exception {
        String filePath = "/Users/salilvnair/workspace/git/salilvnair/excelprocessor/src/main/resources/excel/generated";
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                .builder()
                .filePath(filePath)
                .fileName("NoTemplateHorizontalSheetNumberFormatTest.xlsx")
                .build();
        Workbook workbook = writer.workbook(UserInfoNoTemplateHorizontalSheetMock.generateHorizontalDynamicSheets(), excelSheetContext);
        ExcelSheetWriterUtil.write(workbook, excelSheetContext.fileName(), excelSheetContext.filePath());
    }
}
