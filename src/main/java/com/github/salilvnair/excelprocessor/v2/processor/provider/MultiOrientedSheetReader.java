package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
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
        Map<String, List<? extends BaseExcelSheet>> multiOrientedSheetMap = new HashMap<>();
        for (Class<? extends BaseExcelSheet> sheetClass : multiOrientedSheet.value()) {
            ExcelSheetReaderContext multiOrientedReaderContext = copyReaderContextAndGenerateMultiOrientedReaderContext(multiOrientedSheet, clazz, context);

            BaseExcelSheetReader excelSheetReader = ExcelSheetFactory.generateReader(sheetClass);
            if (excelSheetReader != null) {
                excelSheetReader.read(sheetClass, multiOrientedReaderContext);
            }

            List<? extends BaseExcelSheet> sheetData = multiOrientedReaderContext.getSheetData();
            Sheet sheet = multiOrientedReaderContext.sheet();
            context.multiOrientedReaderContexts().put(sheet.value(), multiOrientedReaderContext);
            multiOrientedSheetMap.put(sheet.value(), sheetData);
        }
        context.setMultiOrientedSheetMap(multiOrientedSheetMap);
    }

    private ExcelSheetReaderContext copyReaderContextAndGenerateMultiOrientedReaderContext(MultiOrientedSheet multiOrientedSheet, Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context) {
        ExcelSheetReaderContext multiOrientedReaderContext = new ExcelSheetReaderContext();
        multiOrientedReaderContext.setSheetName(multiOrientedSheet.name());
        multiOrientedReaderContext.setFileName(context.getFileName());
        multiOrientedReaderContext.setWorkbook(context.getWorkbook());
        multiOrientedReaderContext.setExtractMultiOrientedMap(context.extractMultiOrientedMap());
        return multiOrientedReaderContext;
    }
}
