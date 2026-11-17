package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * List based streaming (SXSSF) writer for fixed-header horizontal sheets. Only
 * {@link Sheet#streamingRowWindowSize()} rows are kept in memory by POI; the input list itself is
 * still held by the caller. For inputs that should not be materialized use
 * {@code ExcelSheetWriterFactory.openStreaming(...)}.
 */
public class StreamingHorizontalSheetWriter extends BaseHorizontalSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        StreamingWorkbookSupport.Opened opened = StreamingWorkbookSupport.open(sheet, context);
        try {
            SXSSFSheet sxssfSheet = StreamingWorkbookSupport.sheet(opened.workbook, sheet);
            List<Field> cells = new ArrayList<>(cellFields);
            writeDataToHeader(sheetData, cellFields, sxssfSheet, cells, sheet, context);
            writeDataToBody(sheetData, cellFields, sxssfSheet, cells, sheet, context);
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
