package com.github.salilvnair.excelprocessor.v2.processor.service;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.MergedCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Salil V Nair
 */
public interface StaticHeaderSheetReader {

    static Map<Cell, Field> headerCellFieldMap(ExcelSheetReaderContext context, Sheet sheet, Set<Field> excelHeaders) {
        return excelHeaders.stream().collect(Collectors.toMap(excelHeader -> excelHeader.getAnnotation(Cell.class), excelHeader -> excelHeader, (o, n) -> n));
    }

    static BaseSheet cellValueResolver(Class<? extends BaseSheet> clazz, Map<String, CellInfo> excelCellInfoMap, int rowOrColumnIndexKey, Map<Cell, Field> headerCellFieldMap) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(sheet.vertical()) {
            classObject.setColumnIndex(rowOrColumnIndexKey);
            classObject.setColumn(ExcelSheetReader.toIndentName(rowOrColumnIndexKey + 1));
        }
        else {
            classObject.setRowIndex(rowOrColumnIndexKey);
            classObject.setRow(rowOrColumnIndexKey+1);
        }
        headerCellFieldMap.forEach((cell, field) -> {
            String headerString = cell.value();
            headerString = ExcelSheetReaderUtil.cleanAndProcessSimilarHeaderString(headerString, clazz, cell);
            CellInfo cellInfo = excelCellInfoMap.get(headerString);
            if(cellInfo!=null) {
                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType(), field);
                ReflectionUtil.setField(classObject, field, fieldValue);
            }
        });
        return classObject;
    }

    static BaseSheet cellValueResolver(Class<? extends BaseSheet> clazz, Map<String, CellInfo> excelCellInfoMap, int rowOrColumnIndexKey, Map<Cell, Field> headerCellFieldMap,  Map<String, CellRangeAddress> mergedHeaderStringKeyedCellRageAddress) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(sheet.vertical()) {
            classObject.setColumnIndex(rowOrColumnIndexKey);
            classObject.setColumn(ExcelSheetReader.toIndentName(rowOrColumnIndexKey + 1));
        }
        else {
            classObject.setRowIndex(rowOrColumnIndexKey);
            classObject.setRow(rowOrColumnIndexKey+1);
        }
        List<Integer> mergedColumnIndices = mergedHeaderStringKeyedCellRageAddress.values()
                                            .stream()
                                            .map(cellRangeAddress -> IntStream.range(cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn()+1)
                                            .boxed().collect(Collectors.toList()))
                                            .flatMap(Collection::stream)
                                            .collect(Collectors.toList());
        headerCellFieldMap.forEach((cell, field) -> {
            String headerString = cell.value();
            headerString = ExcelSheetReaderUtil.cleanAndProcessSimilarHeaderString(headerString, clazz, cell);
            CellInfo cellInfo = excelCellInfoMap.get(headerString);
            if(cellInfo!=null && !mergedColumnIndices.contains(cellInfo.columnIndex())) {
                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType(), field);
                ReflectionUtil.setField(classObject, field, fieldValue);
            }
        });

        Set<Field> mergedCellFields = AnnotationUtil.getAnnotatedFields(clazz, MergedCell.class);
        Map<MergedCell, Field> mergedHeaderCellFieldMap = new HashMap<>();
        mergedCellFields.forEach(mergedCellField -> {
            MergedCell mergedCell = mergedCellField.getAnnotation(MergedCell.class);
            mergedHeaderCellFieldMap.put(mergedCell, mergedCellField);
        });

        for (MergedCell mergedCell: mergedHeaderCellFieldMap.keySet()) {
            Field mergedField = mergedHeaderCellFieldMap.get(mergedCell);
            Class<?> fieldInstanceClass = mergedField.getType();
            Object fieldObject = fieldInstanceClass.newInstance();
            Set<Field> mergedCellObjectCLassFields = AnnotationUtil.getAnnotatedFields(fieldInstanceClass, Cell.class);
            Map<Cell, Field> mergedHeaderFieldCellFieldMap = new HashMap<>();
            mergedCellObjectCLassFields.forEach(cellField -> {
                Cell cell = cellField.getAnnotation(Cell.class);
                mergedHeaderFieldCellFieldMap.put(cell, cellField);
            });
            mergedHeaderFieldCellFieldMap.forEach((cell, field) -> {
                String headerString = cell.value();
                headerString = ExcelSheetReaderUtil.cleanAndProcessSimilarHeaderString(headerString, fieldInstanceClass, cell);
                CellInfo cellInfo = excelCellInfoMap.get(headerString);
                if(cellInfo!=null) {
                    Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType(), field);
                    ReflectionUtil.setField(fieldObject, field, fieldValue);
                }
            });
            ReflectionUtil.setField(classObject, mergedField, fieldObject);
        }
        return classObject;
    }

    static BaseSheet cellValueResolver(Class<?> clazz, int rowOrColumnIndexKey, Map<String, CellInfo> excelCellInfoMap, boolean section)  throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(sheet.vertical()) {
            classObject.setColumnIndex(rowOrColumnIndexKey);
            classObject.setColumn(ExcelSheetReader.toIndentName(rowOrColumnIndexKey + 1));
        }
        else {
            classObject.setRowIndex(rowOrColumnIndexKey);
            classObject.setRow(rowOrColumnIndexKey+1);
        }
        Set<Field> topLevelCellFields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<String, CellInfo> sectionHeaderStringKeyedCellInfoMap = new LinkedHashMap<>();
        if(!topLevelCellFields.isEmpty()) {
            for (Field field: topLevelCellFields) {
                Cell cell = field.getAnnotation(Cell.class);
                String headerString = cell.value();
                headerString = ExcelSheetReaderUtil.cleanAndProcessSimilarHeaderString(headerString, clazz, cell);
                CellInfo cellInfo = excelCellInfoMap.get(headerString);
                if(cellInfo!=null) {
                    Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType(), field);
                    ReflectionUtil.setField(classObject, field, fieldValue);
                }
                if(section) {
                    sectionHeaderStringKeyedCellInfoMap.put(headerString, cellInfo);
                }
            }
            if(section) {
                classObject.cells().putAll(sectionHeaderStringKeyedCellInfoMap);
            }
        }
        Set<Field> sectionFields = AnnotationUtil.getAnnotatedFields(clazz, Section.class);
        if(!sectionFields.isEmpty()) {
            for (Field sectionField: sectionFields) {
                BaseSheet fieldValue = cellValueResolver(sectionField.getType(), rowOrColumnIndexKey, excelCellInfoMap, true);
                classObject.cells().putAll(fieldValue.cells());
                ReflectionUtil.setField(classObject, sectionField, fieldValue);
            }
        }
        return classObject;
    }

    static BaseSheet cellValueResolver(Class<?> clazz, int rowOrColumnIndexKey, Map<String, CellInfo> excelCellInfoMap, boolean section, Set<String> processedHeader)  throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if(sheet.vertical()) {
            classObject.setColumnIndex(rowOrColumnIndexKey);
            classObject.setColumn(ExcelSheetReader.toIndentName(rowOrColumnIndexKey + 1));
        }
        else {
            classObject.setRowIndex(rowOrColumnIndexKey);
            classObject.setRow(rowOrColumnIndexKey+1);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Cell.class) != null) {
                Cell cell = field.getAnnotation(Cell.class);
                String headerString = cell.value();
                String headerStringKey = excelCellInfoMap.keySet().stream().filter(key -> !processedHeader.contains(key) && key.contains(headerString)).findFirst().orElse(null);
                if(headerStringKey != null ) {
                    processedHeader.add(headerStringKey);
                }
                else  {
                    headerStringKey = headerString;
                }
                CellInfo cellInfo = excelCellInfoMap.get(headerStringKey);
                if(cellInfo!=null) {
                    Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType(), field);
                    ReflectionUtil.setField(classObject, field, fieldValue);
                }
            }
            else if (field.getAnnotation(Section.class) != null) {
                BaseSheet fieldValue = cellValueResolver(field.getType(), rowOrColumnIndexKey, excelCellInfoMap, true, processedHeader);
                classObject.cells().putAll(fieldValue.cells());
                ReflectionUtil.setField(classObject, field, fieldValue);
            }
        }
        return classObject;
    }


    static Set<Field> findAllSectionFields(Class<?> clazz) {
        Set<Field> allSectionFields = new HashSet<>();
        Set<Field> sectionFields = AnnotationUtil.getAnnotatedFields(clazz, Section.class);
        if(!sectionFields.isEmpty()) {
            sectionFields.forEach(sectionField -> allSectionFields.addAll(findAllSectionFields(sectionField.getType())));
            allSectionFields.addAll(sectionFields);
        }
        return allSectionFields;
    }

    static Set<Field> findAllCellFields(Class<?> clazz) {
        Set<Field> cellFields = new HashSet<>();
        Set<Field> topLevelCellFields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Set<Field> sectionFields = AnnotationUtil.getAnnotatedFields(clazz, Section.class);
        if(!sectionFields.isEmpty()) {
            sectionFields.forEach(sectionField -> cellFields.addAll(findAllCellFields(sectionField.getType())));
        }
        if(!topLevelCellFields.isEmpty()) {
            cellFields.addAll(topLevelCellFields);
        }
        return cellFields;
    }

    static List<Cell> findAllCells(Class<?> clazz) {
        return findAllCellFields(clazz).stream().map(cellField -> cellField.getAnnotation(Cell.class)).collect(Collectors.toList());
    }

}
