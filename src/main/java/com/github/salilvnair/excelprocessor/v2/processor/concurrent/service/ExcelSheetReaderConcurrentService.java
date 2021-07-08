package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

/**
 * @author Salil V Nair
 */
public interface ExcelSheetReaderConcurrentService {

    ExcelSheetReaderContext read(ExcelSheetContext context, Class<? extends BaseExcelSheet> clazz) throws Exception;


}
