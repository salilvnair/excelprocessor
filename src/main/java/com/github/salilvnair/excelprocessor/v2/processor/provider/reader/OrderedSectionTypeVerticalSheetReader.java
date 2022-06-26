package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ListGenerator;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.SectionHint;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReadException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.model.SectionRangeAddress;
import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Salil V Nair
 */
public class OrderedSectionTypeVerticalSheetReader extends BaseVerticalSheetReader {
    public OrderedSectionTypeVerticalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }

    @Override
    protected void _read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> columnIndexKeyedHeaderKeyCellInfoMap, Object headerCellFieldMapOrDynamicCellField) {
        Field dynamicCellField = null;
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(!sheet.sectional()) {
            super._read(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, columnIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMapOrDynamicCellField);
            return;
        }
        context.setSheet(sheet);
        if(sheet.dynamicHeaders()) {
            dynamicCellField = (Field) headerCellFieldMapOrDynamicCellField;
        }
        int headerRowIndex = sheet.headerRowAt() - 1;
        String sheetName = context.sheetName() == null ? sheet.value(): context.sheetName();
        org.apache.poi.ss.usermodel.Sheet workbookSheet = workbook.getSheet(sheetName);
        int totalRows = workbookSheet.getLastRowNum();
        totalRows = sheet.valueRowEndsAt()!=-1 ? sheet.valueRowEndsAt() - 1 : totalRows;
        int headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt())  - 1;
        String valueColumnAt = !StringUtils.isEmpty(context.valueColumnBeginsAt()) ? context.valueColumnBeginsAt() : sheet.valueColumnAt();
        int valueColumnIndex = ExcelSheetReader.toIndentNumber(valueColumnAt)  - 1;
        int maxColumnC = 0;
        List<String> headerStringList = orderedOrUnorderedList(sheet);
        List<String> sheetHeaders = orderedOrUnorderedList(sheet);
        List<String> ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.stream(sheet.ignoreHeaders()).collect(Collectors.toList()) : context.ignoreHeaders();
        List<String> ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.stream(sheet.ignoreHeaderPatterns()).collect(Collectors.toList()) : context.ignoreHeaderPatterns();
        List<Integer> ignoreHeaderRows = context.ignoreHeaderRows().stream().map(r -> r-1).collect(Collectors.toList());
        Map<String, String> processedDuplicateHeaderKeyedOriginalHeaderMap = orderedOrUnorderedMap(sheet);
        Set<Field> sheetCells = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        List<String> annotatedHeaders = sheetCells.stream().map(cellField -> cellField.getAnnotation(Cell.class).value()).collect(Collectors.toList());
        extractSectionAnnotatedHeaders(clazz, annotatedHeaders);
        Set<Field> sectionTypeCells = StaticHeaderSheetReader.findAllSectionFields(clazz);
        Set<String> annotatedSectionBeginningEndingTexts = new HashSet<>();
        sectionTypeCells.forEach(sectionTypeCell -> {
            Section sectionTypeCellAnnotation = sectionTypeCell.getAnnotation(Section.class);
            annotatedSectionBeginningEndingTexts.add(sectionTypeCellAnnotation.beginningText());
            annotatedSectionBeginningEndingTexts.add(sectionTypeCellAnnotation.endingText());
        });
        Map<String, Integer> sectionRangeMap = new HashMap<>();
        SectionHint[] sectionHints = sheet.sectionHints();
        List<String> sectionStrings = sectionHints.length > 0 ? Arrays
                                                                .stream(sectionHints)
                                                                .map(sectionHint -> ListGenerator
                                                                                        .immutable()
                                                                                        .generate(sectionHint.beginningTextLike(), sectionHint.endingTextLike()))
                                                                .flatMap(List::stream)
                                                                .collect(Collectors.toList()) : Collections.emptyList();
        for (int r = headerRowIndex; r <= totalRows; r++) {
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            int pnC = row.getLastCellNum();
            if(maxColumnC < pnC ) {
                maxColumnC = pnC;
            }
            String valueColumnEndsAt = context.valueColumnEndsAt() != null ? context.valueColumnEndsAt() : sheet.valueColumnEndsAt();
            if(!StringUtils.isEmpty(valueColumnEndsAt)) {
                maxColumnC = ExcelSheetReader.toIndentNumber(valueColumnEndsAt);
            }
            org.apache.poi.ss.usermodel.Cell headerCell = row.getCell(headerColumnIndex);
            if(headerCell == null){
                continue;
            }
            String headerString = headerCell.getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            if(!annotatedSectionBeginningEndingTexts.contains(headerString)) {
                //these are the section headers which is not present in pojo mapping
                if(sectionStrings.stream().anyMatch(headerString::contains)) {
                    sectionRangeMap.put(headerString, r);
                }
            }

            if(!annotatedHeaders.contains(headerString) && !sheet.dynamicHeaders()) {
                ignoreHeaderRows.add(r);
                continue;
            }
            if(ignoreHeaders.contains(headerString)) {
                ignoreHeaderRows.add(r);
                continue;
            }
            if(ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)) {
                ignoreHeaderRows.add(r);
                continue;
            }
            String processSimilarHeaderString = ExcelSheetReaderUtil.processSimilarHeaderString(sheetHeaders, sheet, headerString, headerColumnIndex, r);
            sheetHeaders.add(headerString);
            headerRowIndexKeyedHeaderValueMap.put(r, processSimilarHeaderString);
            processedDuplicateHeaderKeyedOriginalHeaderMap.put(processSimilarHeaderString, headerString);
            headerStringList.add(processSimilarHeaderString);

        }

        //skip all unknown header range
        processUnknownSectionsIfAny(sheet, ignoreHeaderRows, sectionRangeMap);

        int valueColumnBeginsAt = valueColumnIndex!= -1 ? valueColumnIndex : headerColumnIndex + 1;
        int cIndex = valueColumnBeginsAt;
        while(cIndex < maxColumnC) {
            Map<String, CellInfo> headerKeyCellInfoMap = orderedOrUnorderedMap(sheet);
            for (int r = headerRowIndex; r <= totalRows; r++) {
                if (ignoreHeaderRows.contains(r)) {
                    continue;
                }
                Row row = workbookSheet.getRow(r);
                if(row == null){
                    continue;
                }
                String headerString = headerRowIndexKeyedHeaderValueMap.get(r);
                if(StringUtils.isEmpty(headerString)){
                    continue;
                }
                for (int c = cIndex ; c < cIndex + 1; c++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                    CellInfo cellInfo = new CellInfo();
                    cellInfo.setRowIndex(r);
                    cellInfo.setRow(r+1);
                    cellInfo.setColumnIndex(c);
                    cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                    cellInfo.setHeader(headerString);
                    cellInfo.setOriginalHeader(processedDuplicateHeaderKeyedOriginalHeaderMap.get(headerString));
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
            }
            columnIndexKeyedHeaderKeyCellInfoMap.put(cIndex, headerKeyCellInfoMap);
            cIndex++;
        }

        Field finalDynamicCellField = dynamicCellField;
        columnIndexKeyedHeaderKeyCellInfoMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= valueColumnBeginsAt)
                .forEach(entry -> {
                    try {
                        int key = entry.getKey();
                        Map<String, CellInfo> value = entry.getValue();
                        BaseSheet classObject = null;
                        if(sheet.dynamicHeaders()) {
                            classObject = DynamicHeaderSheetReader.dynamicCellValueResolver(clazz, headerStringList, value, key, finalDynamicCellField);
                        }
                        else {
                            Set<String> processedHeader = new HashSet<>();
                            classObject = StaticHeaderSheetReader.cellValueResolver(clazz, key, value, false, processedHeader);
                        }
                        classObject.setSheetHeaders(sheetHeaders);
                        classObject.setCells(value);
                        baseSheetList.add(classObject);
                    }
                    catch (Exception e) {
                        if(!context.suppressExceptions()) {
                            throw new ExcelSheetReadException(e);
                        }
                    }
                });
    }

    private void processUnknownSectionsIfAny(Sheet sheet, List<Integer> ignoreHeaderRows, Map<String, Integer> sectionRangeMap) {
        SectionHint[] sectionHints = sheet.sectionHints();
        if(sectionHints.length > 0) {
            for(SectionHint sectionHint: sectionHints) {
                sectionRangeMap.forEach((key, value) -> {
                    int rb = value;
                    int re = -1;
                    if (key.contains(sectionHint.beginningTextLike())) {
                        String newKey = key.replace(sectionHint.beginningTextLike(), sectionHint.endingTextLike());
                        if(!sectionRangeMap.containsKey(newKey) && sectionHint.findClosestMatch()) {
                            newKey = com.github.salilvnair.excelprocessor.v2.helper.StringUtils.findClosestMatch(sectionRangeMap.keySet(), newKey);
                        }
                        re = sectionRangeMap.get(newKey);
                    }
                    if(re > -1) {
                        IntStream.range(rb, (re+1)).boxed().forEach(ignoreHeaderRows::add);
                    }
                });
            }
        }
    }


    private List<SectionRangeAddress> findSectionRangeAddresses(int headerRowIndex, int totalRows, org.apache.poi.ss.usermodel.Sheet workbookSheet, Class<?> clazz, int headerColumnIndex) {
        Map<String, Field> sectionBeginningTextKeyedFieldMap = new HashMap<>();
        Map<String, Field> sectionEndingTextKeyedFieldMap = new HashMap<>();
        Set<Field> sectionFields = populateSectionTextKeyedFieldMap(sectionEndingTextKeyedFieldMap, sectionBeginningTextKeyedFieldMap, clazz);
        return extractSectionRangeAddresses(clazz, sectionFields, headerRowIndex, totalRows, workbookSheet, headerColumnIndex, sectionBeginningTextKeyedFieldMap, sectionEndingTextKeyedFieldMap);
    }

    private  Set<Field> populateSectionTextKeyedFieldMap(Map<String, Field> sectionEndingTextKeyedFieldMap, Map<String, Field> sectionBeginningTextKeyedFieldMap, Class<?> clazz) {
        Set<Field> sectionFields = StaticHeaderSheetReader.findAllSectionFields(clazz);
        sectionBeginningTextKeyedFieldMap.putAll(sectionFields.stream().collect(Collectors.toMap(field -> field.getAnnotation(Section.class).beginningText(), field -> field)));
        sectionEndingTextKeyedFieldMap.putAll(sectionFields.stream().collect(Collectors.toMap(field -> field.getAnnotation(Section.class).endingText(), field -> field)));
        return sectionFields;
    }

    private List<SectionRangeAddress> extractSectionRangeAddresses(Class<?> clazz, Set<Field> sectionFields, int headerRowIndex, int totalRows, org.apache.poi.ss.usermodel.Sheet workbookSheet, int headerColumnIndex, Map<String, Field> sectionBeginningTextKeyedFieldMap, Map<String, Field> sectionEndingTextKeyedFieldMap) {
        Map<String, SectionRangeAddress> sectionTextKeyedSectionRangeAddressMap = new HashMap<>();
        for (int r = headerRowIndex; r <= totalRows; r++) {
            SectionRangeAddress sectionRangeAddress = new SectionRangeAddress();
            Row row = workbookSheet.getRow(r);
            if(row == null){
                continue;
            }
            org.apache.poi.ss.usermodel.Cell headerCell = row.getCell(headerColumnIndex);
            if(headerCell == null){
                continue;
            }
            String headerString = headerCell.getStringCellValue();
            headerString = ExcelSheetReaderUtil.cleanHeaderString(headerString);
            //t -- e
            if(sectionBeginningTextKeyedFieldMap.containsKey(headerString)) {
                //this index is begining of the section range
                sectionRangeAddress.setSectionBeginningText(headerString);
                sectionRangeAddress.setSectionBeginningRowIndex(r);
                sectionRangeAddress.setSectionFirstColIndex(headerColumnIndex);
                sectionBeginningTextKeyedFieldMap.remove(headerString);
                sectionTextKeyedSectionRangeAddressMap.put(headerString, sectionRangeAddress);
            }
            else if(sectionEndingTextKeyedFieldMap.containsKey(headerString)) {
                //this index is ending of the section range
                sectionRangeAddress.setSectionEndingText(headerString);
                sectionRangeAddress.setSectionEndingRowIndex(r);
                sectionRangeAddress.setSectionLastColIndex(headerColumnIndex);
                sectionBeginningTextKeyedFieldMap.remove(headerString);
                sectionTextKeyedSectionRangeAddressMap.put(headerString, sectionRangeAddress);
            }
        }
        return extractSections(sectionFields, sectionTextKeyedSectionRangeAddressMap, clazz);
    }

    private List<SectionRangeAddress> extractSections(Set<Field> sectionFields, Map<String, SectionRangeAddress> sectionTextKeyedSectionRangeAddressMap, Class<?> clazz) {
        return sectionFields
                .stream()
                .map(sectionField -> {
                    Section sectionFieldAnnotation = sectionField.getAnnotation(Section.class);
                    SectionRangeAddress sectionRangeAddress1 = sectionTextKeyedSectionRangeAddressMap.get(sectionFieldAnnotation.beginningText());
                    SectionRangeAddress sectionRangeAddress2 = sectionTextKeyedSectionRangeAddressMap.get(sectionFieldAnnotation.endingText());
                    sectionRangeAddress1.setSectionEndingText(sectionRangeAddress2.sectionEndingText());
                    sectionRangeAddress1.setSectionLastColIndex(sectionRangeAddress2.sectionLastColIndex());
                    sectionRangeAddress1.setSectionEndingRowIndex(sectionRangeAddress2.sectionEndingRowIndex());
                    return sectionRangeAddress1;
                })
                .collect(Collectors.toList());
    }

    private void extractSectionAnnotatedHeaders(Class<? extends BaseSheet> clazz, List<String> classCellHeaders) {
        Set<Field> sectionTypeCells = AnnotationUtil.getAnnotatedFields(clazz, Section.class);
        resolveSectionAnnotatedFields(classCellHeaders, sectionTypeCells);
    }

    private void resolveSectionAnnotatedFields(List<String> classCellHeaders, Set<Field> sectionTypeCells) {
        sectionTypeCells.forEach(sectionTypeCellField -> {
            Class<?> mergedCellFieldType = sectionTypeCellField.getType();
            Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(mergedCellFieldType, Cell.class);
            cellFields.forEach(cellField -> classCellHeaders.add(cellField.getAnnotation(Cell.class).value()));
            Set<Field> sectionFields = AnnotationUtil.getAnnotatedFields(mergedCellFieldType, Section.class);
            if(!sectionFields.isEmpty()) {
                resolveSectionAnnotatedFields(classCellHeaders, sectionFields);
            }
        });
    }
}
