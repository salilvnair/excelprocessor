package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public interface SheetReaderConcurrentService {
    default void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowOrColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Map<Cell, Field> headerCellFieldMap) {}
    default void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowOrColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Field dynamicHeaderField) {}
    @SuppressWarnings("unchecked")
    default void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerRowOrColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Object object) {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        if (sheet.dynamicHeaders()) {
            Field field = (Field) object;
            read(clazz, context, workbook, baseSheetList, headerRowOrColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, field);
        }
        else {
            Map<Cell, Field> headerCellFieldMap = (Map<Cell, Field>) object;
            read(clazz, context, workbook, baseSheetList, headerRowOrColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMap);
        }
    }
}
