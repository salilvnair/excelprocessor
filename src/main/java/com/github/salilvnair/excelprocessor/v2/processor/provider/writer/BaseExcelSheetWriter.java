package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetWriter extends BaseExcelProcessor implements ExcelSheetWriter {
    abstract void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context);

    protected void writeDataToHeaderCell(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value) {
        processCommonCellData(sheetInfo, cellInfo, rowCell, value);
    }

    protected void writeDataToCell(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value) {
        if(cellInfo.hyperLink()) {
            processHyperLink(sheetInfo, cellInfo, rowCell, value);
        }
        else {
            processCommonCellData(sheetInfo, cellInfo, rowCell, value);
        }
    }

    private void processCommonCellData(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value) {
        if(value instanceof Double) {
            rowCell.setCellValue(TypeConvertor.convert(value, Double.class));
        }
        else if(value instanceof String) {
            rowCell.setCellValue(TypeConvertor.convert(value, String.class));
        }
        else if(value instanceof Date) {
            rowCell.setCellValue(TypeConvertor.convert(value, Date.class));
        }
        else if(value instanceof Integer) {
            rowCell.setCellValue(TypeConvertor.convert(Integer.class, Double.class, value));
        }
        else if(value instanceof Long) {
            rowCell.setCellValue(TypeConvertor.convert(Long.class, Double.class, value));
        }
        else if(value instanceof Float) {
            rowCell.setCellValue(TypeConvertor.convert(Float.class, Double.class, value));
        }
        else if(value instanceof BigInteger) {
            rowCell.setCellValue(TypeConvertor.convert(BigInteger.class, Double.class, value));
        }
        else if(value instanceof BigDecimal) {
            rowCell.setCellValue(TypeConvertor.convert(BigDecimal.class, Double.class, value));
        }
    }

    private void processHyperLink(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value) {
        if(value instanceof String) {
            Workbook workbook = rowCell.getSheet().getWorkbook();
            CreationHelper creationHelper = workbook.getCreationHelper();
            Hyperlink link = creationHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress((String) value);
            String linkText = "".equals(cellInfo.hyperLinkText()) ? (String) value : cellInfo.hyperLinkText();
            rowCell.setCellValue(linkText);
            rowCell.setHyperlink(link);
            Font font = workbook.createFont();
            font.setBold(true);
            font.setUnderline(FontUnderline.SINGLE.getByteValue());
            font.setColor(IndexedColors.BLUE.getIndex());
            CellUtil.setFont(rowCell, font);

        }
    }

    protected void applyCellStyles(org.apache.poi.ss.usermodel.Cell rowCell, Field cellField) {
        Cell cellAnnotatedField = cellField.getAnnotation(Cell.class);
        CellUtil.setCellStyleProperty(rowCell, CellUtil.WRAP_TEXT, cellAnnotatedField.wrapText());
        if(cellAnnotatedField.hasBorderStyle()) {
            CellUtil.setCellStyleProperty(rowCell, CellUtil.BORDER_TOP, cellAnnotatedField.borderStyle());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.BORDER_RIGHT, cellAnnotatedField.borderStyle());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.BORDER_BOTTOM, cellAnnotatedField.borderStyle());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.BORDER_LEFT, cellAnnotatedField.borderStyle());
        }
        if(cellAnnotatedField.hasBorderColor()) {
            CellUtil.setCellStyleProperty(rowCell, CellUtil.TOP_BORDER_COLOR, cellAnnotatedField.borderColor().getIndex());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.RIGHT_BORDER_COLOR, cellAnnotatedField.borderColor().getIndex());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.BOTTOM_BORDER_COLOR, cellAnnotatedField.borderColor().getIndex());
            CellUtil.setCellStyleProperty(rowCell, CellUtil.LEFT_BORDER_COLOR, cellAnnotatedField.borderColor().getIndex());
        }
        if(cellAnnotatedField.hasFillPattern()) {
            CellUtil.setCellStyleProperty(rowCell, CellUtil.FILL_PATTERN, cellAnnotatedField.fillPattern());
        }
        if(cellAnnotatedField.hasBackgroundColor()) {
            CellUtil.setCellStyleProperty(rowCell, CellUtil.FILL_BACKGROUND_COLOR, cellAnnotatedField.backgroundColor().getIndex());
        }
        if(cellAnnotatedField.hasForegroundColor()) {
            CellUtil.setCellStyleProperty(rowCell, CellUtil.FILL_FOREGROUND_COLOR, cellAnnotatedField.foregroundColor().getIndex());
        }
    }

    public void copyRowStyle(Workbook workbook, org.apache.poi.ss.usermodel.Sheet oldSheet, org.apache.poi.ss.usermodel.Sheet newSheet, int oldRowNum, int newRowNum, int oldCellNum, int newCellNum) {
        Row newRow = newSheet.getRow(newRowNum);
        Row oldRow = oldSheet.getRow(oldRowNum);
        if(newRow!=null && oldRow!=null) {
            newRow.setHeight(oldRow.getHeight());
            org.apache.poi.ss.usermodel.Cell oldCell = oldRow.getCell(oldCellNum);
            org.apache.poi.ss.usermodel.Cell newCell = newRow.getCell(newCellNum);
            copyCellStyle(oldCell, newCell);
        }
    }

    public void copyCellStyle(org.apache.poi.ss.usermodel.Cell oldCell, org.apache.poi.ss.usermodel.Cell newCell){
        if(oldCell!=null && newCell!=null) {
            CellStyle newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }
        }
    }
}
