package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetWriterUtil;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.BaseExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;


public class VerticalSheetWriter extends BaseExcelSheetWriter {

    @Override
    void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext writerContext) {
        BaseSheet baseSheet = sheetData.get(0);
        Sheet sheet = baseSheet.getClass().getAnnotation(Sheet.class);
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(baseSheet.getClass(), Cell.class);
        Workbook workbook = writerContext.containsExistingWorkbook() ? writerContext.existingWorkbook() : writerContext.template() == null ? ExcelSheetWriterUtil.generateWorkbook(sheet) : writerContext.template();
        org.apache.poi.ss.usermodel.Sheet workbookSheet =  writerContext.template() == null ? workbook.createSheet(sheet.value()): workbook.getSheet(sheet.value());
        if(workbookSheet == null) {
            workbookSheet = workbook.createSheet(sheet.value());
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerRowEndsIndex = sheet.headerRowEndsAt();
        headerRowEndsIndex = headerRowEndsIndex != -1 ? headerRowEndsIndex : (headerRowIndex + cellFields.size() - 1);
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex!= -1 ? valueColumnBeginsAtIndex : valueColumnIndex;

        List<Field> cells = new ArrayList<>(cellFields);
        writerContext.setSheetData(sheetData);


        List<String> headerList = cells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).toList();

        Map<String, Field> headerKeyFieldMap = new HashMap<>();

        for (Field cellField : cells) {
            Cell cell = cellField.getAnnotation(Cell.class);
            headerKeyFieldMap.put(cell.value(), cellField);
        }

        for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
            Row row = workbookSheet.createRow(r);
            String originalHeader = headerList.get(r - headerRowIndex);
            org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(headerColumnIndex) == null ? row.createCell(headerColumnIndex) : row.getCell(headerColumnIndex);
            Field field = headerKeyFieldMap.get(originalHeader);
            Cell cell = field.getAnnotation(Cell.class);
            writeDataToHeaderCell(sheet, cell, rowCell, originalHeader, writerContext);
            applyHeaderCellStyles(sheet, cell, rowCell, field, originalHeader, writerContext);
            FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateFormulaCell(rowCell);
        }

        Map<Integer, Field> headerRowIndexCellFieldMap = new HashMap<>();

        for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
            Row row = workbookSheet.getRow(r) == null ? workbookSheet.createRow(r) : workbookSheet.getRow(r);
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(headerColumnIndex) == null ? row.createCell(headerColumnIndex) : row.getCell(headerColumnIndex);
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbook, cell, cellInfo);
            String headerValue = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            Field field = headerKeyFieldMap.get(headerValue);
            Cell annotationCell = field.getAnnotation(Cell.class);
            writeDataToHeaderCell(sheet, annotationCell, cell, headerValue, writerContext);
            headerRowIndexCellFieldMap.put(r, field);
        }

        int columnIndex = valueColumnBeginsAtIndex;
        for (BaseSheet sheetDataObj : sheetData) {
            writerContext.setSheetDataObj(sheetDataObj);
            for (int r = headerRowIndex; r <= headerRowEndsIndex; r++) {
                Row row = workbookSheet.getRow(r) == null ? workbookSheet.createRow(r) : workbookSheet.getRow(r);
                Field cellField = headerRowIndexCellFieldMap.get(r);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(columnIndex) == null ? row.createCell(columnIndex) : row.getCell(columnIndex);
                writeDataToCell(sheet, cell, rowCell, cellField, fieldValue, writerContext);
            }
            columnIndex++;
        }
        applySheetStyles(sheet, workbook, workbookSheet, writerContext);
        writerContext.setWorkbook(workbook);
    }
}
