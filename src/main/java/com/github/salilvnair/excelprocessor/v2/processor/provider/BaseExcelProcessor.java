package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.constant.SheetProcessingCommonConstant;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Salil V Nair
 */
abstract class BaseExcelProcessor {

    protected static <T> List<T> typedList(List<?> untypedList, Class<T> itemClass) {
        List<T> list = new ArrayList<T>();
        for (Object item : untypedList) {
            list.add(itemClass.cast(item));
        }
        return list;
    }

    protected boolean validateWorkbook(ExcelSheetReaderContext context) {
        return context != null && (context.getWorkbook() != null || context.getExcelFileInputStream() != null);
    }

    protected <K, V> Map<K, V> orderedOrUnorderedMap(Sheet excelSheet) {
        Map<K, V> headerKeyCellInfoMap;
        if(excelSheet.dynamicHeaders()) {
            headerKeyCellInfoMap = new LinkedHashMap<>();
        }
        else {
            headerKeyCellInfoMap = new HashMap<>();
        }
        return headerKeyCellInfoMap;
    }

    protected <T> List<T> orderedOrUnorderedList(Sheet excelSheet) {
        List<T> list;
        if(excelSheet.dynamicHeaders()) {
            list = new LinkedList<>();
        }
        else {
            list = new ArrayList<>();
        }
        return list;
    }
}
