package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.processor.provider.*;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
public class ExcelSheetFactory {
    private ExcelSheetFactory(){}


    public static BaseExcelSheetReader generateReader(Sheet sheet, boolean concurrent, int batchSize) {
        if(sheet.vertical()) {
            if(sheet.dynamicHeaders()) {
                return new DynamicHeaderVerticalSheetReader(concurrent, batchSize);
            }
            return new VerticalSheetReader(concurrent, batchSize);
        }
        else {
            if(sheet.dynamicHeaders()) {
                return new DynamicHeaderHorizontalSheetReader(concurrent, batchSize);
            }
            return new HorizontalSheetReader(concurrent, batchSize);
        }
    }

    public static BaseExcelSheetReader generateReader(Class<? extends BaseSheet> clazz, boolean concurrent) {
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        if(multiOrientedSheet!=null) {
            return new MultiOrientedSheetReader(concurrent, 100);
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        return sheet != null ? generateReader(sheet, concurrent, 100) : null;
    }

    public static BaseExcelSheetReader generateReader(Class<? extends BaseSheet> clazz, boolean concurrent, int batchSize) {
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        if(multiOrientedSheet!=null) {
            return new MultiOrientedSheetReader(concurrent, batchSize);
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        return sheet != null ? generateReader(sheet, concurrent, batchSize) : null;
    }

}
