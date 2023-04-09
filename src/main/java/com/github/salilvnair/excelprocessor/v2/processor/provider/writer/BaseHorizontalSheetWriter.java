package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;


public abstract class BaseHorizontalSheetWriter extends BaseExcelSheetWriter {

    protected void writeDataToHeader(Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
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
                applyCellStyles(rowCell, cellField);
            }
        }
    }

    protected void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
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
                applyCellStyles(rowCell, cellField);
            }
        }
    }
}
