package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.HorizontalSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class HorizontalSheetReaderConcurrentServiceImpl extends HorizontalSheetReader implements SheetReaderConcurrentService {
    public HorizontalSheetReaderConcurrentServiceImpl(boolean concurrent, int batchSize) {
        super(concurrent, batchSize);
    }

    @Override
    public void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseSheet> baseSheetList, Map<Integer, String> headerColumnIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Map<Cell, Field> headerCellFieldMap) {
        _read(clazz, context, workbook, baseSheetList, headerColumnIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerCellFieldMap);
    }
}
