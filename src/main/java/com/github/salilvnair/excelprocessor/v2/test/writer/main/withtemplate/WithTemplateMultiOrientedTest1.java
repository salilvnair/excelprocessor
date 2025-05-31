package com.github.salilvnair.excelprocessor.v2.test.writer.main.withtemplate;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoAndUserInfoMultiOrientedWithTemplateHorizontalSheetMock;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.List;

public class WithTemplateMultiOrientedTest1 {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/salilvnair/workspace/experiments";
        String templateFilePath = "/Users/salilvnair/workspace/experiments/NoTemplateMultiOrientedTemplate.xlsx";
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                                                .builder()
                                                .filePath(filePath)
                                                .template(new File(templateFilePath))
                                                .fileName("WithTemplateMultiOrientedTest1.xlsx")
                                                .build();
        List<List<? extends BaseSheet>> multiOrientedSheets = CompanyInfoAndUserInfoMultiOrientedWithTemplateHorizontalSheetMock.generateMultiOrientedHorizontalSheets();
        Workbook workbook = writer.workbook(excelSheetContext, multiOrientedSheets);
        ExcelSheetWriterUtil.write(workbook, excelSheetContext.fileName(), excelSheetContext.filePath());
    }
}
