package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.Field;
import java.util.*;


public abstract class BaseHorizontalSheetWriter extends BaseExcelSheetWriter {

    protected void writeDataToHeader(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        Row row;
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        context.setSheetData(sheetData);
        if(context.template() == null) {
            row = workbookSheet.createRow(headerRowIndex);
            for (int c = 0; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cell = cellField.getAnnotation(Cell.class);
                Object fieldValue = cell.value();
                int createColumnIndex = c + headerColumnIndex;
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(createColumnIndex);
                writeDataToHeaderCell(sheet, cell, rowCell, fieldValue, context);
                applyHeaderCellStyles(sheet, cell, rowCell, cellField, fieldValue, context);
            }
        }
    }

    protected void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()!=-1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        context.setSheetData(sheetData);
        //if the template !=null then need to read header String and prepare
        // a map of HeaderString, Cell annotated field
        // and create columnLoop using above key set
        for (int r = 0; r < sheetData.size(); r++) {
            BaseSheet sheetDataObj = sheetData.get(r);
            context.setSheetDataObj(sheetDataObj);
            int createRowIndex = r + valueRowIndex;
            Row row = workbookSheet.createRow(createRowIndex);
            for (int c = 0; c < cellFields.size(); c++) {
                Field cellField = cells.get(c);
                Cell cellInfo = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                int createColumnIndex = c + headerColumnIndex;
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(createColumnIndex);
                writeDataToCell(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                applyDataCellStyles(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
        }
    }

    protected void writeDynamicDataToHeader(Map<String, Object> headerKeyedCellValueMap, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        Row row;
        Set<String> headerKeys = CollectionUtils.isNotEmpty(context.getOrderedHeaders()) ?  context.getOrderedHeaders(): headerKeyedCellValueMap.keySet();
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        row = workbookSheet.createRow(headerRowIndex);
        for (int c = 0; c < headerKeys.size(); c++) {
            String headerKey = new ArrayList<>(headerKeys).get(c);
            Map<String, String> dynamicHeaderDisplayNames = context.getDynamicHeaderDisplayNames();
            String headerDisplayName = headerKey;
            if(CollectionUtils.isNotEmpty(Collections.singleton(dynamicHeaderDisplayNames))) {
                headerDisplayName = dynamicHeaderDisplayNames.getOrDefault(headerKey, headerKey);
            }
            int createColumnIndex = c + headerColumnIndex;
            org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(createColumnIndex);
            convertAndSetCellValue(rowCell, headerDisplayName);
            applyDynamicHeaderCellStyles(sheet, headerKey, rowCell, context);
        }
    }

    protected void writeDynamicDataToBody(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()!=-1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        for (int r = 0; r < sheetData.size(); r++) {
            BaseSheet sheetDataObj = sheetData.get(r);
            if(sheetDataObj == null) {
                continue;
            }
            context.setSheetDataObj(sheetDataObj);
            DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetDataObj;
            Map<String, Object> headerKeyedCellValueMap = dynamicHeaderSheet.dynamicHeaderKeyedCellValueMap();
            if(headerKeyedCellValueMap == null || headerKeyedCellValueMap.isEmpty()) {
                continue;
            }
            Set<String> headers = headerKeyedCellValueMap.keySet();
            int createRowIndex = r + valueRowIndex;
            Row row = workbookSheet.createRow(createRowIndex);
            for (int c = 0; c < headers.size(); c++) {
                String header = new ArrayList<>(headers).get(c);
                Object fieldValue = headerKeyedCellValueMap.get(header);
                int createColumnIndex = c + headerColumnIndex;
                org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(createColumnIndex);
                convertAndSetCellValue(rowCell, fieldValue);
                applyDynamicHeaderDataCellStyles(sheet, header, rowCell, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
        }
    }

}
