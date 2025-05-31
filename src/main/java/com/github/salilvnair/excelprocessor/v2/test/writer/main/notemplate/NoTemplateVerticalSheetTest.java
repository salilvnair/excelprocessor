package com.github.salilvnair.excelprocessor.v2.test.writer.main.notemplate;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoNoTemplateVerticalSheetMock;
import org.apache.poi.ss.usermodel.Workbook;

public class NoTemplateVerticalSheetTest {

    public static void main(String[] args) throws Exception {
        String filePath = "/Users/salilvnair/workspace/experiments";
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                .builder()
                .filePath(filePath)
                .fileName("VerticalSheetTest.xlsx")
                .build();
        Workbook workbook = writer.workbook(CompanyInfoNoTemplateVerticalSheetMock.generateVerticalSheets(), excelSheetContext);
        ExcelSheetWriterUtil.write(workbook, excelSheetContext.fileName(), excelSheetContext.filePath());
    }
}
