package com.github.salilvnair.excelprocessor.v2.processor.service;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.FieldInfo;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.FieldType;

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

    static BaseSheet dynamicCellValueResolver(Class<? extends BaseSheet> clazz, List<String> headerStringList, Map<String, CellInfo> excelCellInfoMap, int rowIndexKey, Field headerDynamicCellField, ExcelSheetReaderContext context) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        classObject.setRowIndex(rowIndexKey);
        classObject.setRow(rowIndexKey+1);
        Map<String, Object> dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        headerStringList.forEach(headerString -> {
            if(!ExcelSheetReaderUtil.oneOfTheIgnoreHeaders(headerString, clazz)) {
                CellInfo cellInfo = excelCellInfoMap.get(headerString);
                if(cellInfo!=null) {
                    Object fieldValue = resolveFieldValue(context, cellInfo);
                    dynamicHeaderKeyedCellValueMap.put(headerString, fieldValue);
                }
            }
        });
        ReflectionUtil.setField(classObject, headerDynamicCellField, dynamicHeaderKeyedCellValueMap);
        return classObject;
    }

    static Object resolveFieldValue(ExcelSheetReaderContext context, CellInfo cellInfo) {
        Map<String, FieldInfo> headerFieldInfoMap = context.headerFieldInfoMap();
        String header = cellInfo.originalHeader();
        Object value = cellInfo.value();
        if(headerFieldInfoMap!=null && headerFieldInfoMap.containsKey(header)) {
            FieldInfo fieldInfo = headerFieldInfoMap.get(header);
            FieldType fieldType = FieldType.type(fieldInfo.getType());
            value = TypeConvertor.convert(value, cellInfo.cellType(), fieldType.type(), fieldInfo);
        }
        return value;
    }
}
