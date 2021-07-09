package com.github.salilvnair.excelprocessor.v2.processor.factory;

import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.provider.ExcelSheetReaderImpl;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderFactory {
    private ExcelSheetReaderFactory(){}

    public static ExcelSheetReader generate() {
        return ExcelSheetReaderImpl.init();
    }
    public static ExcelSheetReader generate(boolean concurrent) {
        return ExcelSheetReaderImpl.init(concurrent, 100);
    }

    public static ExcelSheetReader generate(boolean concurrent, int batchSize) {
        return ExcelSheetReaderImpl.init(concurrent, batchSize);
    }

}
