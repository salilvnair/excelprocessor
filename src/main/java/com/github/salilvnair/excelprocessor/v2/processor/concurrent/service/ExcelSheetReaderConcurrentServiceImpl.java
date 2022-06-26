package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.reader.ExcelSheetReaderImpl;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderConcurrentServiceImpl extends ExcelSheetReaderImpl implements ExcelSheetReaderConcurrentService {
    public ExcelSheetReaderConcurrentServiceImpl(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }
    public ExcelSheetReaderContext read(ExcelSheetContext context, Class<? extends BaseSheet> clazz) throws Exception {
        return _read(context ,clazz);
    }


}
