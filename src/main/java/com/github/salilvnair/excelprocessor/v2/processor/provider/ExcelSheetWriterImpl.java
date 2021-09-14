package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class ExcelSheetWriterImpl implements ExcelSheetWriter {
    private ExcelSheetWriterImpl() {}
    public static ExcelSheetWriterImpl init() {
        return new ExcelSheetWriterImpl();
    }
    @Override
    public <T extends BaseSheet> void write(List<T> sheetData, ExcelSheetContext sheetContext) throws Exception {
        Workbook workbook = workbook(sheetData, sheetContext);
        if(workbook != null) {
            ExcelSheetWriterUtil.write(workbook, sheetContext.fileName(), sheetContext.filePath());
        }
    }

    @Override
    public <T extends BaseSheet> Workbook workbook(List<T> sheetData, ExcelSheetContext sheetContext) {
        if(CollectionUtils.isEmpty(sheetData)) {
            return null;
        }
        BaseExcelSheetWriter writer = ExcelSheetFactory.generateWriter(sheetData.get(0).getClass());
        ExcelSheetWriterContext context = new ExcelSheetWriterContext();
        if(writer != null) {
            writer.write(sheetData, context);
        }
        return context.workbook();
    }
}
