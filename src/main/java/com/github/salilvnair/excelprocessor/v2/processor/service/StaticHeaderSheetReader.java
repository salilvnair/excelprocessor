package com.github.salilvnair.excelprocessor.v2.processor.service;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.MergedCell;
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
                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
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
                Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
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
                    Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
                    ReflectionUtil.setField(fieldObject, field, fieldValue);
                }
            });
            ReflectionUtil.setField(classObject, mergedField, fieldObject);
        }
        return classObject;
    }
}
