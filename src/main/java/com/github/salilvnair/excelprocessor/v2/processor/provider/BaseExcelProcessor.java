package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;

import java.util.ArrayList;
import java.util.List;

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
}
