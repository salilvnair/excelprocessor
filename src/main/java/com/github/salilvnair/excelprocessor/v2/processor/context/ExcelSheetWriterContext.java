package com.github.salilvnair.excelprocessor.v2.processor.context;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterContext extends BaseExcelSheetContext {
    public Workbook workbook() {
        return super.getWorkbook();
    }
}
