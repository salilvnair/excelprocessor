package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
public interface ExcelSheetReaderConcurrentService {

    ExcelSheetReaderContext read(ExcelSheetContext context, Class<? extends BaseSheet> clazz) throws Exception;


}
