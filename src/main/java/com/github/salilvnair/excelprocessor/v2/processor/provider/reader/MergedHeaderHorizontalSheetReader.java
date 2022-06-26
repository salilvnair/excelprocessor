package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.MergedCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReadException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class MergedHeaderHorizontalSheetReader extends HorizontalSheetReader {
    public MergedHeaderHorizontalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void _read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Object headerCellFieldMapOrDynamicCellField) {
        Map<Cell, Field> headerCellFieldMap = null;
        Field dynamicCellField = null;
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(!sheet.mergedHeaders() || sheet.mergedHeaderRowAt()==-1) {
            super._read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
            return;
        }
        context.setSheet(sheet);
        if(sheet.dynamicHeaders()) {
            dynamicCellField = (Field) headerCellFieldMapOrDynamicCellField;
        }
        else {
            headerCellFieldMap = (Map<Cell, Field>) headerCellFieldMapOrDynamicCellField;
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        int mergedHeaderRowIndex = sheet.mergedHeaderRowAt() - 1;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt())  - 1;
        String sheetName = context.sheetName() == null ? sheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.getSheet(sheetName);
        int totalRows = workbookSheet.getLastRowNum();
        Row headerRow = workbookSheet.getRow(headerRowIndex);
        Row mergedHeaderRow = workbookSheet.getRow(mergedHeaderRowIndex);
        Map<String, CellRangeAddress> mergedHeaderStringKeyedCellRageAddress = new HashMap<>();
        for (int c = headerColumnIndex; c < mergedHeaderRow.getLastCellNum(); c++) {
            if(mergedHeaderRow.getCell(c) == null) {
                continue;
            }
            String headerString = mergedHeaderRow.getCell(c).getStringCellValue();
            if(StringUtils.isEmpty(headerString)){
                continue;
            }
            CellRangeAddress mergedRegionForCell = findMergedRegionForCell(mergedHeaderRow.getCell(c));
            if(mergedRegionForCell == null) {
                continue;
            }
            mergedHeaderStringKeyedCellRageAddress.put(headerString, mergedRegionForCell);
        }
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<String> ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.stream(sheet.ignoreHeaderPatterns()).collect(Collectors.toList()) : context.ignoreHeaderPatterns();
        List<Integer> ignoreHeaderColumns = context.ignoreHeaderColumns().stream().map(col -> ExcelSheetReader.toIndentNumber(col) -1 ).collect(Collectors.toList());
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, String> processedDuplicateHeaderKeyedOriginalHeaderMap = orderedOrUnorderedMap(sheet);
        List<String> classCellHeaders = sheetCells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).collect(Collectors.toList());
        Set<Field> mergedCells = AnnotationUtil.getAnnotatedFields(clazz, MergedCell.class);
        mergedCells.forEach(mergedCellField -> {
            Class<?> mergedCellFieldType = mergedCellField.getType();
            Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(mergedCellFieldType, Cell.class);
            cellFields.forEach(cellField -> classCellHeaders.add(cellField.getAnnotation(Cell.class).value()));
        });
        for (int c = headerColumnIndex; c < headerRow.getLastCellNum(); c++) {
            String headerString = headerRow.getCell(c).getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            if(!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders()) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            if(ignoreHeaders.contains(headerString)) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            if(ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)) {
                ignoreHeaderColumns.add(c);
                continue;
            }
            sheetHeaders.add(headerString);
            String processSimilarHeaderString = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, c, headerRowIndex, headerStringList);
            headerColumnIndexKeyedHeaderValueMap.put(c, processSimilarHeaderString);
            processedDuplicateHeaderKeyedOriginalHeaderMap.put(processSimilarHeaderString, headerString);
            headerStringList.add(processSimilarHeaderString);
        }
        int valueRowBeginsAt = context.valueRowBeginsAt() > -1 ? context.valueRowBeginsAt() - 1 : sheet.valueRowBeginsAt() > - 1 ? sheet.valueRowBeginsAt() -1 : -1;
        int valueRowIndex = valueRowBeginsAt > -1 ? valueRowBeginsAt: sheet.valueRowAt() > -1 ? sheet.valueRowAt() - 1 : headerRowIndex+1;
        int valueEndsAt = context.valueRowEndsAt() > -1 ? context.valueRowEndsAt() - 1 : sheet.valueRowEndsAt() > - 1 ? sheet.valueRowEndsAt() - 1 : -1;
        totalRows = valueEndsAt > -1 ? valueEndsAt : totalRows;
        for (int r = valueRowIndex ; r <= totalRows; r++) {
            Map<String, CellInfo> headerKeyCellInfoMap = new HashMap<>();
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            for (int c = headerColumnIndex; c < pnC; c++) {
                if(ignoreHeaderColumns.contains(c)) {
                    continue;
                }
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(r);
                cellInfo.setRow(r+1);
                cellInfo.setColumnIndex(c);
                cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                String headerString = headerColumnIndexKeyedHeaderValueMap.get(c);
                if(StringUtils.isEmpty(headerString)){
                    continue;
                }
                cellInfo.setHeader(headerString);
                cellInfo.setOriginalHeader(processedDuplicateHeaderKeyedOriginalHeaderMap.get(headerString));
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                if(cell == null){
                    cellInfo.setValue(null);
                    headerKeyCellInfoMap.put(headerString, cellInfo);
                    continue;
                }
                Object cellValue = extractValueBasedOnCellType(workbook, cell, cellInfo);
                extractCellPropertiesAndSetCellInfo(workbook, cell, cellInfo);
                cellInfo.setValue(cellValue);
                headerKeyCellInfoMap.put(headerString, cellInfo);
            }
            rowIndexKeyedHeaderKeyCellInfoMap.put(r, headerKeyCellInfoMap);
        }

        Map<Cell, Field> finalHeaderCellFieldMap = headerCellFieldMap;
        rowIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueRowBeginsAt)
                .forEach(entry -> {
                    try {
                        int rowIndexKey = entry.getKey();
                        Map<String, CellInfo> excelCellInfoMap = entry.getValue();
                        BaseSheet classObject;
                        classObject = StaticHeaderSheetReader.cellValueResolver(clazz, excelCellInfoMap, rowIndexKey, finalHeaderCellFieldMap, mergedHeaderStringKeyedCellRageAddress);
                        classObject.setSheetHeaders(sheetHeaders);
                        classObject.setCells(excelCellInfoMap);
                        baseSheetList.add(classObject);
                    }
                    catch (Exception e) {
                        if(!context.suppressExceptions()) {
                            throw new ExcelSheetReadException(e);
                        }
                    }
                });
    }

    public CellRangeAddress findMergedRegionForCell(org.apache.poi.ss.usermodel.Cell cell) {
        org.apache.poi.ss.usermodel.Sheet sheet = cell.getRow().getSheet();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                // This region contains the cell in question
                return mergedRegion;
            }
        }
        // Not in any
        return null;
    }
}
