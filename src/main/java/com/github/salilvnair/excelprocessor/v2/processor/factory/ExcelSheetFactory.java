package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.processor.provider.BaseExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.provider.HorizontalSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.provider.MultiOrientedSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.provider.VerticalSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

/**
 * @author Salil V Nair
 */
public class ExcelSheetFactory {
    private ExcelSheetFactory(){}


    public static BaseExcelSheetReader generateReader(Sheet sheet) {
        if(sheet.isVertical()) {
            return new VerticalSheetReader();
        }
        else {
            return new HorizontalSheetReader();
        }
    }

    public static BaseExcelSheetReader generateReader(Class<? extends BaseExcelSheet> clazz) {
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        if(multiOrientedSheet!=null) {
            return new MultiOrientedSheetReader();
        }
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        return sheet != null ? generateReader(sheet) : null;
    }

}
