package com.github.salilvnair.excelprocessor.v2.processor.provider;

import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetWriter extends BaseExcelProcessor implements ExcelSheetWriter {
    abstract void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context);
    
    protected void writeDataToCell(Cell cell, Object value) {
        if(value instanceof Double) {
            cell.setCellValue(TypeConvertor.convert(value, Double.class));
        }
        else if(value instanceof String) {
            cell.setCellValue(TypeConvertor.convert(value, String.class));
        }
        else if(value instanceof Date) {
            cell.setCellValue(TypeConvertor.convert(value, Date.class));
        }
        else if(value instanceof Integer) {
            cell.setCellValue(TypeConvertor.convert(Integer.class, Double.class, value));
        }
        else if(value instanceof Long) {
            cell.setCellValue(TypeConvertor.convert(Long.class, Double.class, value));
        }
        else if(value instanceof Float) {
            cell.setCellValue(TypeConvertor.convert(Float.class, Double.class, value));
        }
        else if(value instanceof BigInteger) {
            cell.setCellValue(TypeConvertor.convert(BigInteger.class, Double.class, value));
        }
        else if(value instanceof BigDecimal) {
            cell.setCellValue(TypeConvertor.convert(BigDecimal.class, Double.class, value));
        }
    }
}
