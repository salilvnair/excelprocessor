package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.*;
import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.*;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;

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
            else if(sheet.sectional() && sheet.ordered()) {
                return new OrderedSectionTypeVerticalSheetReader(concurrent, batchSize);
            }
            else if(sheet.sectional()) {
                return new SectionTypeVerticalSheetReader(concurrent, batchSize);
            }
            return new VerticalSheetReader(concurrent, batchSize);
        }
        else {
            if(sheet.dynamicHeaders()) {
                return new DynamicHeaderHorizontalSheetReader(concurrent, batchSize);
            }
            else if(sheet.mergedHeaders()) {
                return new MergedHeaderHorizontalSheetReader(concurrent, batchSize);
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

    public static BaseExcelSheetWriter generateWriter(Class<? extends BaseSheet> clazz) {
        Sheet sheet = clazz.getAnnotation(Sheet.class);
        return sheet != null ? generateWriter(sheet) : null;
    }

    public static BaseExcelSheetWriter generateWriter(Sheet sheet) {
        if(sheet.vertical()) {
            return new VerticalSheetWriter();
        }
        else if(sheet.dynamicHeaders()) {
            return new DynamicHorizontalSheetWriter();
        }
        else if (ExcelFileType.Extension.XLSX.equals(sheet.type()) && sheet.streamingWorkbook()) {
            return new StreamingHorizontalSheetWriter();
        }
        else {
            return new HorizontalSheetWriter();
        }
    }

}
