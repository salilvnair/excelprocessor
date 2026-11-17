package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.List;

/**
 * List based streaming (SXSSF) writer for dynamic-header horizontal sheets, selected for
 * {@code @Sheet(dynamicHeaders = true, streamingWorkbook = true)} XLSX sheets without a
 * user-defined template. Headers are taken from the first row, as in {@link DynamicHorizontalSheetWriter}.
 */
public class StreamingDynamicHorizontalSheetWriter extends BaseHorizontalSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Sheet sheet = dynamicHeaderSheet.getClass().getAnnotation(Sheet.class);
        context.setSheetData(sheetData);
        context.setSheetDataObj(dynamicHeaderSheet);
        StreamingWorkbookSupport.Opened opened = StreamingWorkbookSupport.open(sheet, context);
        try {
            SXSSFSheet sxssfSheet = StreamingWorkbookSupport.sheet(opened.workbook, sheet);
            writeDynamicDataToHeader(sheetData, sxssfSheet, sheet, context);
            writeDynamicDataToBody(sheetData, sxssfSheet, sheet, context);
            applySheetStyles(sheet, opened.workbook, sxssfSheet, context);
        }
        catch (RuntimeException e) {
            if (opened.created) {
                opened.workbook.dispose();
            }
            throw e;
        }
        context.setWorkbook(opened.workbook);
        context.setStreamingWorkbook(true);
    }
}
