package com.github.salilvnair.excelprocessor.v2.processor.provider.core;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import java.util.*;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.BaseExcelSheetContext;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelProcessor {

    protected static <T> List<T> typedList(List<?> untypedList, Class<T> itemClass) {
        List<T> list = new ArrayList<T>();
        for (Object item : untypedList) {
            list.add(itemClass.cast(item));
        }
        return list;
    }

    protected boolean validateWorkbook(BaseExcelSheetContext context) {
        return context != null && (context.getWorkbook() != null || context.getExcelFileInputStream() != null);
    }

    public static <K, V> Map<K, V> orderedOrUnorderedMap(Sheet excelSheet) {
        Map<K, V> headerKeyCellInfoMap;
        if(excelSheet.dynamicHeaders() || excelSheet.sectional()) {
            headerKeyCellInfoMap = new LinkedHashMap<>();
        }
        else {
            headerKeyCellInfoMap = new HashMap<>();
        }
        return headerKeyCellInfoMap;
    }

    public static <T> List<T> orderedOrUnorderedList(Sheet excelSheet) {
        List<T> list;
        if(excelSheet.dynamicHeaders()) {
            list = new LinkedList<>();
        }
        else {
            list = new ArrayList<>();
        }
        return list;
    }

    public static <T> Set<T> orderedOrUnorderedSet(Sheet excelSheet) {
        Set<T> set;
        if(excelSheet.dynamicHeaders()) {
            set = new LinkedHashSet<>();
        }
        else {
            set = new HashSet<>();
        }
        return set;
    }
    
    /**
     * One formatter per thread (DataFormatter is not thread-safe and costly to build per row).
     * Formula cells are judged by their cached result, so template rows whose formulas evaluate to
     * blank count as empty. POI 6 does this by default; setting it explicitly gives the same
     * result when an application runs the library on POI 5.
     */
    private static final ThreadLocal<DataFormatter> EMPTY_CHECK_FORMATTER = ThreadLocal.withInitial(() -> {
        DataFormatter formatter = new DataFormatter();
        formatter.setUseCachedValuesForFormulaCells(true);
        return formatter;
    });

    public static boolean rowIsEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = EMPTY_CHECK_FORMATTER.get();
        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }
    
    public static boolean rowIsEmpty(Row row, int headerColumnIndex, int lastCellNum) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = EMPTY_CHECK_FORMATTER.get();
        if (row != null) {
            for (int c = headerColumnIndex; c < lastCellNum; c++) {
                if (dataFormatter.formatCellValue(row.getCell(c)).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }
    
}
