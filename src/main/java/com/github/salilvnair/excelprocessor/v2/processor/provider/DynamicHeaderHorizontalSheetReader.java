package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;

/**
 * @author Salil V Nair
 */
public class DynamicHeaderHorizontalSheetReader extends BaseHorizontalSheetReader implements DynamicHeaderSheetReader {
    public DynamicHeaderHorizontalSheetReader(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }
}
