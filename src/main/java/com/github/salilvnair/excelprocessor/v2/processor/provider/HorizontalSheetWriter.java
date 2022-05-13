package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
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
        writeDataToHeader(cellFields, workbookSheet, cells, sheet, context);
        writeDataToBody(sheetData, cellFields, workbookSheet, cells, sheet, context);
        context.setWorkbook(workbook);
    }

    private void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        Row row;
        int valueRowIndex = sheet.valueRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        int valueColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.valueColumnAt())  - 1;
        valueColumnIndex = valueColumnIndex != -1 ? valueColumnIndex : headerColumnIndex + 1;
        for (int r = valueRowIndex; r < sheetData.size(); r++) {
            BaseSheet sheetDataObj = sheetData.get(r);
            row = workbookSheet.createRow(r+1);
            for (int c = valueColumnIndex; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
                writeDataToCell(rowCell, fieldValue);
            }
        }
    }

    private void writeDataToHeader(Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        Row row;
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        if(context.template() != null) {
            row = context.template().getSheet(sheet.value()).getRow(headerRowIndex);
        }
        else {
            row = workbookSheet.createRow(headerRowIndex);
        }

        for (int c = headerColumnIndex; c < cellFields.size(); c++) {
            Field cellField = cells.get(c);
            Cell cell = cellField.getAnnotation(Cell.class);
            Object fieldValue = cell.value();
            org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
            writeDataToCell(rowCell, fieldValue);
        }
    }
}
