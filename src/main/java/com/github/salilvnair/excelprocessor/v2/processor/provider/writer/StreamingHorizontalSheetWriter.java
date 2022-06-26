package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class StreamingHorizontalSheetWriter extends BaseExcelSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        XSSFWorkbook workbook = (XSSFWorkbook) (context.template() == null ? ExcelSheetWriterUtil.generateWorkbook(sheet) : context.template());
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        sxssfWorkbook.setCompressTempFiles(true);
        SXSSFSheet sxssfSheet = (SXSSFSheet) sxssfWorkbook.getSheet(sheet.value());
        sxssfSheet.setRandomAccessWindowSize(100);
        List<Field> cells = new ArrayList<>(cellFields);
        writeDataToHeader(cellFields, sxssfSheet, cells, sheet, context);
        writeDataToBody(sheetData, cellFields, sxssfSheet, cells, sheet, context);
        context.setWorkbook(sxssfWorkbook);
        context.setStreamingWorkbook(true);
    }

    private void writeDataToHeader(Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        Row row;
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        if(context.template() == null) {
            row = workbookSheet.createRow(headerRowIndex);
            for (int c = headerColumnIndex; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = cell.value();
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
            }
        }
    }

    private void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()!=-1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        for (int r = 0; r < sheetData.size(); r++) {
            BaseSheet sheetDataObj = sheetData.get(r);
            int createRowIndex = r + valueRowIndex;
            Row row = workbookSheet.createRow(createRowIndex);
            for (int c = headerColumnIndex; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
            }
        }
    }
}
