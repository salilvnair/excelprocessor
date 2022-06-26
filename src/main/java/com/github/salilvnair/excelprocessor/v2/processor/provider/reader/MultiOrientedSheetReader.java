package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetFactory;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class MultiOrientedSheetReader extends  BaseExcelSheetReader {
    private final boolean concurrent;
    private final int batchSize;
    public MultiOrientedSheetReader(boolean concurrent, int batchSize) {
        this.concurrent = concurrent;
        this.batchSize = batchSize;
    }
    @Override
    public void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!clazz.isAnnotationPresent(MultiOrientedSheet.class)) {
            return;//add error later
        }
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        Map<String, List<? extends BaseSheet>> multiOrientedSheetMap = new HashMap<>();
        for (Class<? extends BaseSheet> sheetClass : multiOrientedSheet.value()) {
            ExcelSheetReaderContext multiOrientedReaderContext = copyReaderContextAndGenerateMultiOrientedReaderContext(multiOrientedSheet, clazz, context);

            BaseExcelSheetReader excelSheetReader = ExcelSheetFactory.generateReader(sheetClass, concurrent, batchSize);
            if (excelSheetReader != null) {
                excelSheetReader.read(sheetClass, multiOrientedReaderContext);
            }

            List<? extends BaseSheet> sheetData = multiOrientedReaderContext.getSheetData();
            Sheet sheet = multiOrientedReaderContext.sheet();
            context.multiOrientedReaderContexts().put(sheet.value(), multiOrientedReaderContext);
            multiOrientedSheetMap.put(sheet.value(), sheetData);
        }
        context.setMultiOrientedSheetMap(multiOrientedSheetMap);
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        if(!clazz.isAnnotationPresent(MultiOrientedSheet.class)) {
            return null;//add error later
        }
        MultiOrientedSheet multiOrientedSheet = clazz.getAnnotation(MultiOrientedSheet.class);
        ExcelInfo excelInfo = new ExcelInfo();
        for (Class<? extends BaseSheet> sheetClass : multiOrientedSheet.value()) {
            ExcelSheetReaderContext multiOrientedReaderContext = copyReaderContextAndGenerateMultiOrientedReaderContext(multiOrientedSheet, clazz, context);

            BaseExcelSheetReader excelSheetReader = ExcelSheetFactory.generateReader(sheetClass, concurrent, batchSize);
            if (excelSheetReader != null) {
                ExcelInfo excelInfoItr = excelSheetReader.excelInfo(sheetClass, multiOrientedReaderContext);
                if(excelInfoItr == null) {
                    if(!context.suppressExceptions()) {
                        throw new ExcelSheetReaderException("ExcelInfo is null."); //TODO v2: change to a constant
                    }
                }
                else {
                    excelInfo.sheets().addAll(excelInfoItr.sheets());
                }
            }
        }
        return excelInfo;
    }

    private ExcelSheetReaderContext copyReaderContextAndGenerateMultiOrientedReaderContext(MultiOrientedSheet multiOrientedSheet, Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        ExcelSheetReaderContext multiOrientedReaderContext = new ExcelSheetReaderContext();
        multiOrientedReaderContext.setSheetName(multiOrientedSheet.name());
        multiOrientedReaderContext.setIgnoreHeaders(context.ignoreHeaders());
        multiOrientedReaderContext.setIgnoreHeaderRows(context.ignoreHeaderRows());
        multiOrientedReaderContext.setIgnoreHeaderColumns(context.ignoreHeaderColumns());
        multiOrientedReaderContext.setFileName(context.getFileName());
        multiOrientedReaderContext.setWorkbook(context.getWorkbook());
        multiOrientedReaderContext.setExtractMultiOrientedMap(context.extractMultiOrientedMap());
        multiOrientedReaderContext.setSuppressExceptions(context.suppressExceptions());
        return multiOrientedReaderContext;
    }
}
