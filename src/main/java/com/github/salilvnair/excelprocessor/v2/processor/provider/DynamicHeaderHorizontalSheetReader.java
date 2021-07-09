package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DynamicCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ConcurrentUtil;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.service.ExcelSheetReaderTaskService;
import com.github.salilvnair.excelprocessor.v2.processor.concurrent.type.TaskType;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderHorizontalSheetReader extends BaseHorizontalSheetReader {
    public DynamicHeaderHorizontalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }

    @Override
    protected Field dynamicCellField(Class<? extends BaseSheet> clazz) {
        Set<Field> headers = AnnotationUtil.getAnnotatedFields(clazz, DynamicCell.class);
        boolean hasUserDefinedDynamicCell = !headers.isEmpty() && headers.size() > 1;
        List<Field> fields = headers
                .stream()
                .filter(field -> hasUserDefinedDynamicCell && field.getAnnotation(DynamicCell.class).priority() > -1 || field.getAnnotation(DynamicCell.class).priority() == -1)
                .collect(Collectors.toList());
        return fields.get(0);
    }

    @Override
    protected BaseSheet dynamicCellValueResolver(Class<? extends BaseSheet> clazz, List<String> headerStringList, Map<String, CellInfo> excelCellInfoMap, int key, Field headerDynamicCellField) throws InstantiationException, IllegalAccessException {
        BaseSheet classObject = clazz.asSubclass(BaseSheet.class).newInstance();
        classObject.setRowIndex(key);
        classObject.setRow(key+1);
        Map<String, Object> dynamicHeaderKeyedCellValueMap = new LinkedHashMap<>();
        headerStringList.forEach(headerString -> {
            CellInfo cellInfo = excelCellInfoMap.get(headerString);
            if(cellInfo!=null) {
                Object fieldValue = cellInfo.value();
                dynamicHeaderKeyedCellValueMap.put(headerString, fieldValue);
            }
        });
        ReflectionUtil.setField(classObject, headerDynamicCellField, dynamicHeaderKeyedCellValueMap);
        return classObject;
    }
}
