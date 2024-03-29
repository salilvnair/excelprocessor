package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.processor.provider.writer.ExcelSheetWriterImpl;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterFactory {
    private ExcelSheetWriterFactory(){}

    public static ExcelSheetWriter generate() {
        return ExcelSheetWriterImpl.init();
    }
}
