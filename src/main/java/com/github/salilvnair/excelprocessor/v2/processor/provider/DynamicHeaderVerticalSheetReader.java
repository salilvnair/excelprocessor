package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderVerticalSheetReader extends BaseVerticalSheetReader implements DynamicHeaderSheetReader {
    public DynamicHeaderVerticalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }
}
