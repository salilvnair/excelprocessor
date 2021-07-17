package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class ExcelSheetWriterImpl implements ExcelSheetWriter {
    private ExcelSheetWriterImpl() {}
    public static ExcelSheetWriterImpl init() {
        return new ExcelSheetWriterImpl();
    }
    @Override
    public <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext) throws Exception {
        VerticalSheetWriter writer = new VerticalSheetWriter();
        ExcelSheetWriterContext context = new ExcelSheetWriterContext();
        writer.write(sheetData, context);
        Workbook workbook = context.workbook();
        ExcelSheetWriterUtil.write(workbook, "Test.xls", "/Users/salilvnair/workspace/kichuz/experiments");
    }
}
