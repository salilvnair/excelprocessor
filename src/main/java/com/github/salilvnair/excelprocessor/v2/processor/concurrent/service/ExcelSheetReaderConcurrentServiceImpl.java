package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.ExcelSheetReaderImpl;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderConcurrentServiceImpl extends ExcelSheetReaderImpl implements ExcelSheetReaderConcurrentService {
    public ExcelSheetReaderConcurrentServiceImpl(boolean concurrent) {
        super(concurrent);
    }
    public ExcelSheetReaderContext read(ExcelSheetContext context, Class<? extends BaseExcelSheet> clazz) throws Exception {
        return _read(context ,clazz);
    }


}
