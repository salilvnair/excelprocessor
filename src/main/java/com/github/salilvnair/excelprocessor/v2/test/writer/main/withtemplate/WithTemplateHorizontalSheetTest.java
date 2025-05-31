package com.github.salilvnair.excelprocessor.v2.test.writer.main.withtemplate;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoAndUserInfoMultiOrientedWithTemplateHorizontalSheetMock;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoNoTemplateHorizontalSheetMock;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoWithTemplateHorizontalSheetMock;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;

public class WithTemplateHorizontalSheetTest {

    public static void main(String[] args) throws Exception {
        String filePath = "/Users/salilvnair/workspace/git/salilvnair/excelprocessor/src/main/resources/excel/generated";
        String templateFilePath = "/Users/salilvnair/workspace/git/salilvnair/excelprocessor/src/main/resources/excel/template/TemplateHorizontalSheetTest.xlsx";
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                .builder()
                .filePath(filePath)
                .template(new File(templateFilePath))
                .fileName("WithTemplateHorizontalSheetTest.xlsx")
                .build();
        Workbook workbook = writer.workbook(CompanyInfoWithTemplateHorizontalSheetMock.generateHorizontalSheets(), excelSheetContext);
        ExcelSheetWriterUtil.write(workbook, excelSheetContext.fileName(), excelSheetContext.filePath());
    }
}
