package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class HorizontalSheetWriter extends BaseExcelSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        Workbook workbook = ExcelSheetWriterUtil.generateWorkbook(sheet);
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.createSheet(sheet.value());
        List<Field> cells = new ArrayList<>(cellFields);
        Row row = null;
        for (int r = 0; r < 1; r++) {
            row = workbookSheet.createRow(r);
            for (int c = 0; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = cell.value();
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
            }
        }
        for (int r = 0; r < sheetData.size(); r++) {
            BaseSheet sheetDataObj = sheetData.get(r);
            row = workbookSheet.createRow(r+1);
            for (int c = 0; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
            }
        }
        context.setWorkbook(workbook);
    }
}
