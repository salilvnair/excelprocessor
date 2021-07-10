package com.github.salilvnair.excelprocessor.v2.processor.service;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public interface StaticHeaderSheetReader {



    static Map<Cell, Field> headerCellFieldMap(ExcelSheetReaderContext context, Sheet sheet, Set<Field> excelHeaders) {
        return excelHeaders.stream().collect(Collectors.toMap(excelHeader -> excelHeader.getAnnotation(Cell.class), excelHeader -> excelHeader, (o, n) -> n));
    }

    static BaseSheet cellValueResolver(Class<? extends BaseSheet> clazz, Map<String, CellInfo> excelCellInfoMap, int key, Map<Cell, Field> headerCellFieldMap) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        classObject.setRowIndex(key);
        classObject.setRow(key+1);
        headerCellFieldMap.forEach((cell, field) -> {
            String headerString = cell.value();
            headerString = ExcelSheetReaderUtil.cleanAndProcessSimilarHeaderString(headerString, clazz, cell);
            if(!ExcelSheetReaderUtil.oneOfTheIgnoreHeaders(headerString, clazz)) {
                CellInfo cellInfo = excelCellInfoMap.get(headerString);
                if(cellInfo!=null) {
                    Object fieldValue = TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), field.getType());
                    ReflectionUtil.setField(classObject, field, fieldValue);
                }
            }
        });
        return classObject;
    }
}
