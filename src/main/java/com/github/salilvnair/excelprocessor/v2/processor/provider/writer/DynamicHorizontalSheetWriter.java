package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.util.List;
import java.util.Map;


public class DynamicHorizontalSheetWriter extends BaseHorizontalSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext writerContext) {
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Sheet sheet = dynamicHeaderSheet.getClass().getAnnotation(Sheet.class);
        writerContext.setSheetData(sheetData);
        writerContext.setSheetDataObj(dynamicHeaderSheet);
        Map<String, Object> headerKeyedCellValueMap = dynamicHeaderSheet.dynamicHeaderKeyedCellValueMap();

        Workbook workbook = writerContext.containsExistingWorkbook() ? writerContext.existingWorkbook() : writerContext.template() == null ? ExcelSheetWriterUtil.generateWorkbook(sheet) : writerContext.template();
        org.apache.poi.ss.usermodel.Sheet workbookSheet =  writerContext.template() == null ? workbook.createSheet(sheet.value()): workbook.getSheet(sheet.value());

        writeDynamicDataToHeader(headerKeyedCellValueMap, workbookSheet, sheet, writerContext);
        writeDynamicDataToBody(sheetData, workbookSheet, sheet, writerContext);
        applySheetStyles(sheet, workbook, workbookSheet, writerContext);
        writerContext.setWorkbook(workbook);
    }
}
