package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class VerticalSheetWriter extends BaseExcelSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        Workbook workbook = ExcelSheetWriterUtil.generateWorkbook(sheet);
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.createSheet(sheet.value());
        List<Field> cells = new ArrayList<>(cellFields);
        Row row = null;
        for (int r = 0; r < cellFields.size(); r++) {
            row = workbookSheet.createRow(r);
            Field cellField = cells.get(r);
            for (int c = 0; c < 1; c++) {
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = cell.value();
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
                applyCellStyles(rowCell, cellField);
            }
            for (int c = 0; c < sheetData.size(); c++) {
                BaseSheet sheetDataObj = sheetData.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c+1);
                writeDataToCell(rowCell, fieldValue);
                applyCellStyles(rowCell, cellField);
            }
        }
        context.setWorkbook(workbook);
    }
}
