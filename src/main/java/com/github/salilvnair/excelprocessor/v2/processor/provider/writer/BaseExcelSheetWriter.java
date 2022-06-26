package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetWriter extends BaseExcelProcessor implements ExcelSheetWriter {
    abstract void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context);
    
    protected void writeDataToCell(org.apache.poi.ss.usermodel.Cell cell, Object value) {
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

    protected void addUserDefinedCellStyle(Cell cellFieldAnnotation, org.apache.poi.ss.usermodel.Cell cell, Workbook workbook) {
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_TOP, cellFieldAnnotation.borderStyle());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_RIGHT, cellFieldAnnotation.borderStyle());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_BOTTOM, cellFieldAnnotation.borderStyle());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_LEFT, cellFieldAnnotation.borderStyle());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.TOP_BORDER_COLOR, cellFieldAnnotation.borderColor().getIndex());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.RIGHT_BORDER_COLOR, cellFieldAnnotation.borderColor().getIndex());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BOTTOM_BORDER_COLOR, cellFieldAnnotation.borderColor().getIndex());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.LEFT_BORDER_COLOR, cellFieldAnnotation.borderColor().getIndex());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.FILL_FOREGROUND_COLOR, cellFieldAnnotation.foregroundColor().getIndex());
        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.FILL_PATTERN, cellFieldAnnotation.fillPattern());
    }


}
