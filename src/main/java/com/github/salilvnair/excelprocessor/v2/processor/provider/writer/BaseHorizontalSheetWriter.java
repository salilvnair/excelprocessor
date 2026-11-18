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
            context.evaluateIfFormula(rowCell);
        }
        SheetFeatures.headerRow(workbookSheet.getRow(headerRowIndex), sheet);
    }

    /**
     * Column layout of the body of a sheet: where value rows start, the value column range and the
     * column-index keyed model ({@link Field} for fixed sheets, {@link CellInfo} for dynamic ones).
     * Computing it once allows rows to be written one at a time.
     */
    protected static final class BodyLayout<T> {
        final int firstValueRowIndex;
        final int firstColumnIndex;
        final int lastColumnIndex;
        final Map<Integer, T> columns;

        BodyLayout(int firstValueRowIndex, int firstColumnIndex, int lastColumnIndex, Map<Integer, T> columns) {
            this.firstValueRowIndex = firstValueRowIndex;
            this.firstColumnIndex = firstColumnIndex;
            this.lastColumnIndex = lastColumnIndex;
            this.columns = columns;
        }

        int firstValueRowIndex() {
            return firstValueRowIndex;
        }
    }

    protected void writeDataToBody(List<? extends BaseSheet> sheetData, Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        context.setSheetData(sheetData);
        BodyLayout<Field> layout = bodyLayout(cellFields, workbookSheet, cells, sheet, context);
        int valueRowIndex = layout.firstValueRowIndex;
        for (BaseSheet sheetDataObj : sheetData) {
            writeBodyRow(sheetDataObj, valueRowIndex, layout, workbookSheet, sheet, context);
            valueRowIndex++;
        }
    }

    protected BodyLayout<Field> bodyLayout(Set<Field> cellFields, org.apache.poi.ss.usermodel.Sheet workbookSheet, List<Field> cells, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;
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
        context.writtenHeaders().clear();
        headerColumnIndexCellFieldMap.forEach((c, field) -> context.writtenHeaders().put(c, field.getAnnotation(Cell.class).value()));
        return new BodyLayout<>(valueRowIndex, valueColumnBeginsAtIndex, valueColumnEndsAtIndex, headerColumnIndexCellFieldMap);
    }

    /** Writes one fixed-header row at rowIndex; nothing about previous rows is retained. */
    protected void writeBodyRow(BaseSheet sheetDataObj, int rowIndex, BodyLayout<Field> layout, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        context.setSheetDataObj(sheetDataObj);
        Row row = workbookSheet.getRow(rowIndex) == null ? workbookSheet.createRow(rowIndex) : workbookSheet.getRow(rowIndex);
        for (int c = layout.firstColumnIndex; c <= layout.lastColumnIndex; c++) {
            Field cellField = layout.columns.get(c);
            if (cellField == null) {
                continue;
            }
            Cell cellInfo = cellField.getAnnotation(Cell.class);
            Object fieldValue = ReflectionUtil.getFieldValue(sheetDataObj, cellField);
            org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
            writeDataToCell(sheet, cellInfo, rowCell, cellField, fieldValue, context);
            applyDataCellStyles(sheet, cellInfo, rowCell, cellField, fieldValue, context);
            context.evaluateIfFormula(rowCell);
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
            context.writtenHeaders().put(c, headerValue);
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
                context.evaluateIfFormula(rowCell);
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
            context.evaluateIfFormula(rowCell);
        }
        SheetFeatures.headerRow(workbookSheet.getRow(headerRowIndex), sheet);
    }

    protected void writeDynamicDataToBody(List<? extends BaseSheet> sheetData, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        if(CollectionUtils.isEmpty(sheetData)) {
            return;
        }
        Map<String, CellInfo> firstRowCells = extractHeaderKeyedCellInfoMap((DynamicHeaderSheet) sheetData.get(0));
        BodyLayout<CellInfo> layout = dynamicBodyLayout(firstRowCells, workbookSheet, sheet, context);
        int valueRowIndex = layout.firstValueRowIndex;
        for (BaseSheet sheetDataObj : sheetData) {
            writeDynamicBodyRow((DynamicHeaderSheet) sheetDataObj, valueRowIndex, layout, workbookSheet, sheet, context);
            valueRowIndex++;
        }
    }

    protected BodyLayout<CellInfo> dynamicBodyLayout(Map<String, CellInfo> headerKeyedCellInfoMap, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        int headerRowIndex = sheet.headerRowAt() - 1;
        int valueRowIndex = sheet.valueRowAt()> -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int headerColumnIndex = ExcelSheetWriter.toIndentNumber(sheet.headerColumnAt())  - 1;

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
        context.writtenHeaders().clear();
        headerColumnIndexCellInfoMap.forEach((c, cellInfo) -> context.writtenHeaders().put(c, cellInfo.getOriginalHeader()));
        return new BodyLayout<>(valueRowIndex, valueColumnBeginsAtIndex, valueColumnEndsAtIndex, headerColumnIndexCellInfoMap);
    }

    /**
     * Writes one dynamic-header row at rowIndex. A header missing from the row is written as an
     * empty (but styled) cell; nothing about previous rows is retained.
     */
    protected void writeDynamicBodyRow(DynamicHeaderSheet dynamicHeaderSheet, int rowIndex, BodyLayout<CellInfo> layout, org.apache.poi.ss.usermodel.Sheet workbookSheet, Sheet sheet, ExcelSheetWriterContext context) {
        context.setSheetDataObj(dynamicHeaderSheet);
        Map<String, CellInfo> headerKeyedCellInfoMap = extractHeaderKeyedCellInfoMap(dynamicHeaderSheet);
        Row row = workbookSheet.getRow(rowIndex) == null ? workbookSheet.createRow(rowIndex) : workbookSheet.getRow(rowIndex);
        for (int c = layout.firstColumnIndex; c <= layout.lastColumnIndex; c++) {
            CellInfo dynamicallyGeneratedCellInfo = layout.columns.get(c);
            if (dynamicallyGeneratedCellInfo == null) {
                continue;
            }
            String headerKey = dynamicallyGeneratedCellInfo.getOriginalHeader();
            CellInfo userProvidedCellInfo = headerKeyedCellInfoMap == null ? null : headerKeyedCellInfoMap.get(headerKey);
            if (userProvidedCellInfo == null && headerKeyedCellInfoMap != null) {
                String columnHeaderKey = dynamicallyGeneratedCellInfo.getHeader();
                userProvidedCellInfo = headerKeyedCellInfoMap.get(columnHeaderKey);
                if (userProvidedCellInfo != null) {
                    headerKey = columnHeaderKey;
                }
            }
            Object fieldValue = userProvidedCellInfo == null ? null : userProvidedCellInfo.value();
            org.apache.poi.ss.usermodel.Cell rowCell = row.getCell(c) == null ? row.createCell(c) : row.getCell(c);
            convertAndSetCellValue(rowCell, fieldValue);
            applyDynamicHeaderDataCellStyles(sheet, headerKey, rowCell, fieldValue, context);
            context.evaluateIfFormula(rowCell);
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
            context.writtenHeaders().put(c, originalHeader);
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
                context.evaluateIfFormula(rowCell);
            }
            valueRowIndex++;
        }
    }

}
