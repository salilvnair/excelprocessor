package com.github.salilvnair.excelprocessor.v2.processor.service;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public interface DynamicHeaderSheetReader {
    static Field dynamicCellField(Class<? extends BaseSheet> clazz) {
        Set<Field> headers = AnnotationUtil.getAnnotatedFields(clazz, DynamicCell.class);
        boolean hasUserDefinedDynamicCell = !headers.isEmpty() && headers.size() > 1;
        List<Field> fields = headers
                .stream()
                .filter(field -> hasUserDefinedDynamicCell && field.getAnnotation(DynamicCell.class).priority() > -1 || field.getAnnotation(DynamicCell.class).priority() == -1)
                .collect(Collectors.toList());
        return fields.get(0);
    }

    static BaseSheet dynamicCellValueResolver(Class<? extends BaseSheet> clazz, List<String> headerStringList, Map<String, CellInfo> excelCellInfoMap, int key, Field headerDynamicCellField) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        classObject.setRowIndex(key);
        classObject.setRow(key+1);
        Map<String, Object> dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        headerStringList.forEach(headerString -> {
            if(!ExcelSheetReaderUtil.oneOfTheIgnoreHeaders(headerString, clazz)) {
                CellInfo cellInfo = excelCellInfoMap.get(headerString);
                if(cellInfo!=null) {
                    Object fieldValue = cellInfo.value();
                    dynamicHeaderKeyedCellValueMap.put(headerString, fieldValue);
                }
            }
        });
        ReflectionUtil.setField(classObject, headerDynamicCellField, dynamicHeaderKeyedCellValueMap);
        return classObject;
    }
}
