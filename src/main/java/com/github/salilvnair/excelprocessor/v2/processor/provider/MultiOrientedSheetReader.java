package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.util.*;

/**
 * @author Salil V Nair
 */
public class MultiOrientedSheetReader extends  BaseExcelSheetReader {
    @Override
    public void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        if(!clazz.isAnnotationPresent(MultiOrientedSheet.class)) {
            return;//add error later
        }
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        context.setSheetName(multiOrientedSheet.name());
        Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap = new HashMap<>();
        for (Class<? extends BaseExcelSheet> sheetClass : multiOrientedSheet.value()) {
            BaseExcelSheetReader excelSheetReader = ExcelSheetFactory.generateReader(sheetClass);
            if (excelSheetReader != null) {
                excelSheetReader.read(sheetClass, context);
            }

            List<? extends BaseExcelSheet> sheetData = context.getSheetData();
            ExcelSheet excelSheet = context.getExcelSheet();
            multiOrientedSheetMap.put(excelSheet.value(), sheetData);
        }
        context.setMultiOrientedSheetMap(multiOrientedSheetMap);
    }
}
