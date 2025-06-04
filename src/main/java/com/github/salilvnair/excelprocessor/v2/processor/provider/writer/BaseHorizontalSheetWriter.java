package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.HeaderCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.BaseExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
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
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        List<String> headerList = cells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).toList();
        headerColumnEndsAtIndex = headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + cellFields.size() - 1);
        Map<String, Field> headerKeyFieldMap = new HashMap<>();
        for (Field cellField : cells) {
            Cell cell = cellField.getAnnotation(Cell.class);
            String headerKey = cell.value();
            headerKeyFieldMap.put(headerKey, cellField);
        }
        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            Row row = workbookSheet.getRow(headerRowIndex) == null ? workbookSheet.createRow(headerRowIndex) : workbookSheet.getRow(headerRowIndex);
            String originalHeader = headerList.get(c - headerColumnIndex);
            org.apache.poi.ss.usermodel.Cell rowCell = row.createCell(c);
            Field field = headerKeyFieldMap.get(originalHeader);
            Cell cell = field.getAnnotation(Cell.class);
            writeDataToHeaderCell(sheet, cell, rowCell, originalHeader, context);
            applyHeaderCellStyles(sheet, cell, rowCell, field, originalHeader, context);
            FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateFormulaCell(rowCell);
        }
    }

    protected void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        context.setSheetData(sheetData);
        //if the template !=null then need to read header String and prepare
        // a map of HeaderString, Cell annotated field
        // and create columnLoop using above key set

        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        headerColumnEndsAtIndex = headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + cellFields.size() - 1);
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex> -1 ? valueColumnIndex : headerColumnIndex;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex> -1 ? valueColumnBeginsAtIndex : valueColumnIndex;
        String valueColumnEndsAt = sheet.valueColumnEndsAt();
        int valueColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnEndsAt)  - 1;
        valueColumnEndsAtIndex = valueColumnEndsAtIndex> -1 ? valueColumnEndsAtIndex : headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + cellFields.size() - 1);

        Map<Integer, Field> headerColumnIndexCellFieldMap = new HashMap<>();

        Map<String, Field> headerKeyFieldMap = new HashMap<>();
        for (Field cellField : cells) {
            Cell cell = cellField.getAnnotation(Cell.class);
            String headerKey = cell.value();
            if(StringUtils.isNotEmpty(cell.column())) {
                headerKey = headerKey + "_" + cell.column();
            }
            headerKeyFieldMap.put(headerKey, cellField);
        }
        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            Row row = workbookSheet.getRow(headerRowIndex) == null ? workbookSheet.createRow(headerRowIndex) : workbookSheet.getRow(headerRowIndex);
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbookSheet.getWorkbook(), cell, cellInfo);
            String headerValue = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            Field field = headerKeyFieldMap.get(headerValue);
            if(field == null) {
                String column = ExcelSheetReader.toIndentName(c+1);
                field = headerKeyFieldMap.get(headerValue + "_" + column);
            }
            if(field == null) {
                continue;
            }
            headerColumnIndexCellFieldMap.put(c, field);
        }
        for (BaseSheet sheetDataObj : sheetData) {
            context.setSheetDataObj(sheetDataObj);
            Row row = workbookSheet.getRow(valueRowIndex) == null ? workbookSheet.createRow(valueRowIndex) : workbookSheet.getRow(valueRowIndex);
            for (int c = valueColumnBeginsAtIndex; c <= valueColumnEndsAtIndex; c++) {
                Field cellField = headerColumnIndexCellFieldMap.get(c);
                if (cellField == null) {
                    continue;
                }
                Cell cellInfo = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
                writeDataToCell(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                applyDataCellStyles(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
            valueRowIndex++;
        }
    }

    protected void writeMultiPositionalHeadersUserDefinedTemplateDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        Map<Integer, Set<Field>> headerCellFields = new HashMap<>();
        for (Field cellField : cellFields) {
            Cell cell = cellField.getAnnotation(Cell.class);
            int rowIndex = cell.row() != -1 ? cell.row() - 1 : headerRowIndex;
            if(headerCellFields.containsKey(rowIndex)) {
                headerCellFields.get(rowIndex).add(cellField);
            }
            else {
                Set<Field> fieldList = new LinkedHashSet<>();
                fieldList.add(cellField);
                headerCellFields.put(rowIndex, fieldList);
            }
        }
        for (Map.Entry<Integer, Set<Field>> entry : headerCellFields.entrySet()) {
            int rowIndex = entry.getKey();
            Set<Field> cellFieldsAtRow = entry.getValue();
            writeUserDefinedTemplateDataToBody(rowIndex, sheetData, cellFieldsAtRow, workbookSheet, sheet, context);
        }
    }

    protected void writeUserDefinedTemplateDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        writeUserDefinedTemplateDataToBody(headerRowIndex, sheetData, cellFields, workbookSheet, sheet, context);
    }

    protected void writeUserDefinedTemplateDataToBody(int headerRowIndex, List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        context.setSheetData(sheetData);
        //if the template !=null then need to read header String and prepare
        // a map of HeaderString, Cell annotated field
        // and create columnLoop using above key set

        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex> -1 ? valueColumnIndex : headerColumnIndex;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex> -1 ? valueColumnBeginsAtIndex : valueColumnIndex;
        String valueColumnEndsAt = sheet.valueColumnEndsAt();
        int valueColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnEndsAt)  - 1;
        headerColumnEndsAtIndex = headerColumnEndsAtIndex> -1 ? headerColumnEndsAtIndex : (headerColumnIndex + cellFields.size() - 1);
        valueColumnEndsAtIndex = valueColumnEndsAtIndex > -1 ? valueColumnEndsAtIndex : headerColumnEndsAtIndex;

        Map<Integer, Field> headerColumnIndexCellFieldMap = new HashMap<>();

        Map<String, Field> headerKeyFieldMap = new HashMap<>();
        for (Field cellField : cellFields) {
            Cell cell = cellField.getAnnotation(Cell.class);
            headerKeyFieldMap.put(cell.value(), cellField);
        }
        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            org.apache.poi.ss.usermodel.Cell cell = workbookSheet.getRow(headerRowIndex).getCell(c);
            if (cell == null) {
                continue;
            }
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbookSheet.getWorkbook(), cell, cellInfo);
            String headerValue = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            Field field = headerKeyFieldMap.get(headerValue);
            if(field == null) {
                continue;
            }
            headerColumnIndexCellFieldMap.put(c, field);
        }
        for (BaseSheet sheetDataObj : sheetData) {
            context.setSheetDataObj(sheetDataObj);
            Row row = workbookSheet.getRow(valueRowIndex) == null ? workbookSheet.createRow(valueRowIndex) : workbookSheet.getRow(valueRowIndex);
            for (int c = valueColumnBeginsAtIndex; c <= valueColumnEndsAtIndex; c++) {
                Field cellField = headerColumnIndexCellFieldMap.get(c);
                if (cellField == null) {
                    continue;
                }
                Cell cellInfo = cellField.getAnnotation(Cell.class);
                Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
                writeDataToCell(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                applyDataCellStyles(sheet, cellInfo, rowCell, cellField, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
            valueRowIndex++;
        }
    }

    protected void writeDynamicDataToHeader(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
        Set<String> headers = headerKeyedCellInfoMap.keySet();
        List<String> headerList = new ArrayList<>(headers);
        headerColumnEndsAtIndex = headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + headerKeyedCellInfoMap.size() - 1);
        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            Row row = workbookSheet.getRow(headerRowIndex) == null ? workbookSheet.createRow(headerRowIndex) : workbookSheet.getRow(headerRowIndex);
            String originalHeader = headerList.get(c - headerColumnIndex);
            org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
            convertAndSetCellValue(rowCell, originalHeader);
            addHeaderAndDataCellStyleFromCellInfoIntoWriterContextIfAvailable(headerKeyedCellInfoMap, context);
            applyDynamicHeaderCellStyles(sheet, originalHeader, rowCell, context);
            FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateFormulaCell(rowCell);
        }
    }

    protected void writeDynamicDataToBody(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;

        if(CollectionUtils.isEmpty(sheetData)) {
            return;
        }

        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);

        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex> -1 ? valueColumnIndex : headerColumnIndex;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex> -1 ? valueColumnBeginsAtIndex : valueColumnIndex;
        String valueColumnEndsAt = sheet.valueColumnEndsAt();
        int valueColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnEndsAt)  - 1;
        valueColumnEndsAtIndex = valueColumnEndsAtIndex> -1 ? valueColumnEndsAtIndex : headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + headerKeyedCellInfoMap.size() - 1);
        Map<Integer, CellInfo> headerColumnIndexCellInfoMap = new HashMap<>();
        headerColumnEndsAtIndex = headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + headerKeyedCellInfoMap.size() - 1);
        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            Row row = workbookSheet.getRow(headerRowIndex) == null ? workbookSheet.createRow(headerRowIndex) : workbookSheet.getRow(headerRowIndex);
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbookSheet.getWorkbook(), cell, cellInfo);
            cellInfo.setRowIndex(headerRowIndex);
            cellInfo.setColumnIndex(c);
            String originalHeader = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            String column = ExcelSheetReader.toIndentName(c +1);
            String header = originalHeader + "_" + column;
            cellInfo.setHeader(header);
            cellInfo.setOriginalHeader(originalHeader);
            headerColumnIndexCellInfoMap.put(c, cellInfo);
        }

        for (BaseSheet sheetDataObj : sheetData) {
            context.setSheetDataObj(sheetDataObj);
            dynamicHeaderSheet = (DynamicHeaderSheet) sheetDataObj;
            headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
            Row row = workbookSheet.getRow(valueRowIndex) == null ? workbookSheet.createRow(valueRowIndex) : workbookSheet.getRow(valueRowIndex);
            for (int c = valueColumnBeginsAtIndex; c <= valueColumnEndsAtIndex; c++) {
                CellInfo dynamicallyGeneratedCellInfo = headerColumnIndexCellInfoMap.get(c);
                String headerKey = dynamicallyGeneratedCellInfo.getOriginalHeader();
                CellInfo userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                if (userProvidedCellInfo == null) {
                    headerKey = dynamicallyGeneratedCellInfo.getHeader();
                    userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                }
                Object fieldValue = userProvidedCellInfo.value();
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
                convertAndSetCellValue(rowCell, fieldValue);
                applyDynamicHeaderDataCellStyles(sheet, headerKey, rowCell, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
            valueRowIndex++;
        }
    }

    private void generateHeaderCellStyleFromCellInfoAndApplyDynamicHeaderCellStyles(Map<String, CellInfo> headerKeyedCellInfoMap, Sheet sheet, String header, org.apache.poi.ss.usermodel.Cell cell, ExcelSheetWriterContext context) {
        Map<String, HeaderCellStyleInfo> dynamicHeaderCellStyleInfo = new HashMap<>();
        headerKeyedCellInfoMap.forEach((key, cellInfo) -> {
            HeaderCellStyleInfo headerCellStyleInfo = cellInfo.getHeaderCellStyleInfo();
            if (headerCellStyleInfo != null) {
                dynamicHeaderCellStyleInfo.put(key, headerCellStyleInfo);
            }
        });
        if(!dynamicHeaderCellStyleInfo.isEmpty()) {
            context.setDynamicHeaderCellStyleInfo(dynamicHeaderCellStyleInfo);
        }
        applyDynamicHeaderCellStyles(sheet, header, cell, context);
    }

    protected void writeMultiPositionalHeadersUserDefinedTemplateDataToBody(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        Map<Integer, Map<String, CellInfo>> rowIndexHeaderKeyedCellInfoMap = new HashMap<>();
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
        for (String headerKey : headerKeyedCellInfoMap.keySet()) {
            CellInfo cellInfo = headerKeyedCellInfoMap.get(headerKey);
            int rowIndex = cellInfo.row() != -1 ? cellInfo.row() - 1 : headerRowIndex;
            if(rowIndexHeaderKeyedCellInfoMap.containsKey(rowIndex)) {
                rowIndexHeaderKeyedCellInfoMap.get(rowIndex).put(headerKey, cellInfo);
            }
            else {
                Map<String, CellInfo> headerCellInfoMap = new LinkedHashMap<>();
                headerCellInfoMap.put(headerKey, cellInfo);
                rowIndexHeaderKeyedCellInfoMap.put(rowIndex, headerCellInfoMap);
            }
        }
        for (Map.Entry<Integer, Map<String, CellInfo>> entry : rowIndexHeaderKeyedCellInfoMap.entrySet()) {
            int rowIndex = entry.getKey();
            Map<String, CellInfo> eachHeaderKeyedCellInfoMap = entry.getValue();
            writeUserDefinedTemplateDynamicDataToBody(rowIndex, eachHeaderKeyedCellInfoMap, sheetData, workbookSheet, sheet, context);
        }
    }

    protected void writeUserDefinedTemplateDynamicDataToBody(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData.get(0);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
        writeUserDefinedTemplateDynamicDataToBody(headerRowIndex, headerKeyedCellInfoMap, sheetData, workbookSheet, sheet, context);
    }

    protected void writeUserDefinedTemplateDynamicDataToBody(int headerRowIndex, Map<String, CellInfo> headerKeyedCellInfoMap, List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;

        if(CollectionUtils.isEmpty(sheetData)) {
            return;
        }

        addHeaderAndDataCellStyleFromCellInfoIntoWriterContextIfAvailable(headerKeyedCellInfoMap, context);
        String headerColumnEndsAt = sheet.headerColumnEndsAt();
        int headerColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(headerColumnEndsAt)  - 1;
        String valueColumnAt = sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        valueColumnIndex = valueColumnIndex> -1 ? valueColumnIndex : headerColumnIndex;
        String valueColumnBeginsAt = sheet.valueColumnBeginsAt();
        int valueColumnBeginsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnBeginsAt)  - 1;
        valueColumnBeginsAtIndex = valueColumnBeginsAtIndex> -1 ? valueColumnBeginsAtIndex : valueColumnIndex;
        String valueColumnEndsAt = sheet.valueColumnEndsAt();
        int valueColumnEndsAtIndex = ExcelSheetReader.toIndentNumber(valueColumnEndsAt)  - 1;
        valueColumnEndsAtIndex = valueColumnEndsAtIndex> -1 ? valueColumnEndsAtIndex : headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + headerKeyedCellInfoMap.size() - 1);;
        headerColumnEndsAtIndex = headerColumnEndsAtIndex > -1 ? headerColumnEndsAtIndex : (headerColumnIndex + headerKeyedCellInfoMap.size() - 1);
        Map<Integer, CellInfo> headerColumnIndexCellInfoMap = new HashMap<>();

        for (int c = headerColumnIndex; c <= headerColumnEndsAtIndex; c++) {
            org.apache.poi.ss.usermodel.Cell cell = workbookSheet.getRow(headerRowIndex).getCell(c);
            if (cell == null) {
                continue;
            }
            CellInfo cellInfo = new CellInfo();
            Object value = BaseExcelSheetReader.extractValueBasedOnCellType(workbookSheet.getWorkbook(), cell, cellInfo);
            cellInfo.setRowIndex(headerRowIndex);
            cellInfo.setColumnIndex(c);
            String originalHeader = ExcelSheetReaderUtil.cleanHeaderString(value+"");
            String column = ExcelSheetReader.toIndentName(c +1);
            String header = originalHeader + "_" + column;
            cellInfo.setHeader(header);
            cellInfo.setOriginalHeader(originalHeader);
            headerColumnIndexCellInfoMap.put(c, cellInfo);
        }

        for (BaseSheet sheetDataObj : sheetData) {
            context.setSheetDataObj(sheetDataObj);
            DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetDataObj;
            headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
            Row row = workbookSheet.getRow(valueRowIndex) == null ? workbookSheet.createRow(valueRowIndex) : workbookSheet.getRow(valueRowIndex);
            for (int c = valueColumnBeginsAtIndex; c <= valueColumnEndsAtIndex; c++) {
                CellInfo dynamicallyGeneratedCellInfo = headerColumnIndexCellInfoMap.get(c);
                if (dynamicallyGeneratedCellInfo == null) {
                    continue;
                }
                String headerKey = dynamicallyGeneratedCellInfo.getOriginalHeader();
                CellInfo userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                if (userProvidedCellInfo == null) {
                    headerKey = dynamicallyGeneratedCellInfo.getHeader();
                    userProvidedCellInfo = headerKeyedCellInfoMap.get(headerKey);
                }
                if (userProvidedCellInfo == null) {
                    continue;
                }
                Object fieldValue = userProvidedCellInfo.value();
                org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
                convertAndSetCellValue(rowCell, fieldValue);
                applyDynamicHeaderDataCellStyles(sheet, headerKey, rowCell, fieldValue, context);
                FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateFormulaCell(rowCell);
            }
            valueRowIndex++;
        }
    }

}
