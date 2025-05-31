package com.github.salilvnair.excelprocessor.v2.test.writer.main.notemplate;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.writer.mock.CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class NoTemplateMultiOrientedTest4 {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/salilvnair/workspace/git/salilvnair/excelprocessor/src/main/resources/excel/generated";
        ExcelSheetWriter writer = ExcelSheetWriterFactory.generate();
        ExcelSheetContext excelSheetContext = ExcelSheetContext
                .builder()
                .filePath(filePath)
                .fileName("NoTemplateMultiOrientedTest4.xlsx")
                .build();
        List<List<? extends BaseSheet>> multiOrientedSheets = List.of(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedCompanyInfoVerticalSheets(), CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock.generateMultiOrientedUserInfoVerticalSheets());
        Workbook workbook = writer.workbook(excelSheetContext, multiOrientedSheets);
        ExcelSheetWriterUtil.write(workbook, excelSheetContext.fileName(), excelSheetContext.filePath());
    }
}
