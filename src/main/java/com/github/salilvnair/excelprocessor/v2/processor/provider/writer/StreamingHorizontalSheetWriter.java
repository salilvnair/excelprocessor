package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class StreamingHorizontalSheetWriter extends BaseHorizontalSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        XSSFWorkbook workbook = (XSSFWorkbook) (context.template() == null ? ExcelSheetWriterUtil.generateWorkbook(sheet) : context.template());
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        sxssfWorkbook.setCompressTempFiles(true);
        SXSSFSheet sxssfSheet = context.template() == null ? sxssfWorkbook.createSheet(sheet.value()): sxssfWorkbook.getSheet(sheet.value());
        sxssfSheet.setRandomAccessWindowSize(100);
        List<Field> cells = new ArrayList<>(cellFields);
        writeDataToHeader(cellFields, sxssfSheet, cells, sheet, context);
        writeDataToBody(sheetData, cellFields, sxssfSheet, cells, sheet, context);
        context.setWorkbook(sxssfWorkbook);
        context.setStreamingWorkbook(true);
    }
}
