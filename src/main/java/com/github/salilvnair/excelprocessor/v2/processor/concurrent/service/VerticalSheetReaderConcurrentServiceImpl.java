package com.github.salilvnair.excelprocessor.v2.processor.concurrent.service;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.HorizontalSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.provider.VerticalSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class VerticalSheetReaderConcurrentServiceImpl extends VerticalSheetReader implements SheetReaderConcurrentService {
    public VerticalSheetReaderConcurrentServiceImpl(boolean concurrent) {
        super(concurrent);
    }

    @Override
    public void read(Class<? extends BaseExcelSheet> clazz, ExcelSheetReaderContext context, Workbook workbook, List<BaseExcelSheet> baseSheetList, Map<Integer, String> headerRowIndexKeyedHeaderValueMap, Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap, Map<String, Field> headerKeyFieldMap) {
        _read(clazz, context, workbook, baseSheetList, headerRowIndexKeyedHeaderValueMap, rowIndexKeyedHeaderKeyCellInfoMap, headerKeyFieldMap);
    }
}
