package com.github.salilvnair.excelprocessor.v2.processor.provider.writer;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.processor.helper.DataCellStyleWriterUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.helper.HeaderCellStyleWriterUtil;
import com.github.salilvnair.excelprocessor.v2.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.v2.type.PictureType;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetWriterContext;
import com.github.salilvnair.excelprocessor.v2.processor.provider.core.BaseExcelProcessor;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * @author Salil V Nair
 */
public abstract class BaseExcelSheetWriter extends BaseExcelProcessor implements ExcelSheetWriter {
    abstract void write(List<? extends BaseSheet> sheetData, ExcelSheetWriterContext context);

    protected void writeDataToHeaderCell(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        processCommonCellData(sheetInfo, cellInfo, rowCell, value, context);
    }

    protected void writeDataToCell(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object value, ExcelSheetWriterContext context) {
        initCellProperties(sheetInfo, cellInfo, rowCell, cellField, value, context);
        if(cellInfo.hyperLink()) {
            processHyperLink(sheetInfo, cellInfo, rowCell, value, context);
        }
        else if (cellInfo.multiPicture() || cellInfo.picture()) {
            processCellImageData(sheetInfo, cellInfo, rowCell, value, context);
        }
        else {
            processCommonCellData(sheetInfo, cellInfo, rowCell, value, context);
        }
    }

    private void initCellProperties(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object value, ExcelSheetWriterContext context) {
        DataCellStyle dataCellStyle = DataCellStyleWriterUtil.extractDataCellStyle(cellField, context.getSheetDataObj());
        if(dataCellStyle == null) {
            return;
        }
        if(dataCellStyle.columnWidthInUnits() != -1) {
            rowCell.getSheet().setColumnWidth(rowCell.getColumnIndex(), dataCellStyle.columnWidthInUnits());
        }
    }

    @SuppressWarnings("unchecked")
    private void processCellImageData(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        if(cellInfo.picture()) {
            int r = rowCell.getRow().getRowNum();
            int c = rowCell.getColumnIndex();
            int dx1 = Units.pixelToEMU(cellInfo.pictureMarginInPixels());
            int dy1 = Units.pixelToEMU(0);
            int dx2 = Units.pixelToEMU(cellInfo.pictureWidthInPixels());
            int dy2 = Units.pixelToEMU(cellInfo.pictureHeightInPixels());
            drawImageOnExcelSheet(rowCell.getSheet(), r, r, c, c, dx1, dy1, dx2, dy2, value, cellInfo);
        }

        else if(cellInfo.multiPicture()) {
            List<Object> imageValues = (List<Object>) value;
            int i = 0;
            for(Object imageValue: imageValues) {
                int r = rowCell.getRow().getRowNum();
                int c = rowCell.getColumnIndex();
                int dx1 = Units.pixelToEMU(i * (cellInfo.pictureWidthInPixels()) + cellInfo.pictureMarginInPixels());
                int dy1 = Units.pixelToEMU(0);
                int dx2 = Units.pixelToEMU((i + 1) * cellInfo.pictureWidthInPixels());
                int dy2 = Units.pixelToEMU(cellInfo.pictureHeightInPixels());
                drawImageOnExcelSheet(rowCell.getSheet(), r, r, c, c, dx1, dy1, dx2, dy2, imageValue, cellInfo);
                i++;
            }
        }
    }

    protected void processCommonCellData(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        convertAndSetCellValue(rowCell, value);
    }

    protected void convertAndSetCellValue(org.apache.poi.ss.usermodel.Cell rowCell, Object value) {
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

    private void processHyperLink(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        if (value == null) {
            return;
        }
        if(value instanceof String) {
            boolean containsMultipleHyperLinks = ExcelSheetReaderUtil.containsMultipleHyperLinks(String.valueOf(value));
            if(containsMultipleHyperLinks && cellInfo.multiHyperLink()) {
                processMultiLinkCellValue(sheetInfo, cellInfo, rowCell, value, context);
            }
            else if (containsMultipleHyperLinks) {
                processMultiLinkAsSimpleText(sheetInfo, cellInfo, rowCell, value, context);
            }
            else {
                processUniLinkCellValue(sheetInfo, cellInfo, rowCell, value, context);
            }
        }
    }

    private void processMultiLinkAsSimpleText(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        processCommonCellData(sheetInfo, cellInfo, rowCell, value, context);
        Workbook workbook = rowCell.getSheet().getWorkbook();
        // Create a CellStyle for the formula cell
        CellStyle formulaCellStyle = workbook.createCellStyle();

        // Create a Font with desired settings
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setUnderline(FontUnderline.SINGLE.getByteValue());
        // Set the Font to the CellStyle
        formulaCellStyle.setFont(font);

        // Apply the CellStyle to the formula cell
        rowCell.setCellStyle(formulaCellStyle);
    }

    private void processUniLinkCellValue(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
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

    private void processMultiLinkCellValue(Sheet sheetInfo, Cell cellInfo, org.apache.poi.ss.usermodel.Cell rowCell, Object value, ExcelSheetWriterContext context) {
        Workbook workbook = rowCell.getSheet().getWorkbook();
        List<String> hyperlinks = ExcelSheetReaderUtil.extractHyperlinks(String.valueOf(value));
        String cellFormula = createHyperlinkFormula(hyperlinks);
        rowCell.setCellFormula(cellFormula);

        // Create a CellStyle for the formula cell
        CellStyle formulaCellStyle = workbook.createCellStyle();

        // Create a Font with desired settings
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setUnderline(FontUnderline.SINGLE.getByteValue());
        // Set the Font to the CellStyle
        formulaCellStyle.setFont(font);

        // Apply the CellStyle to the formula cell
        rowCell.setCellStyle(formulaCellStyle);
    }

    private static String createHyperlinkFormula(List<String> hyperlinks) {
        StringBuilder formulaBuilder = new StringBuilder();

        for (int i = 0; i < hyperlinks.size(); i++) {
            String link = hyperlinks.get(i);

            String hyperlink = "HYPERLINK(\"" + link + "\", \"" + link + "\")";
            formulaBuilder.append(hyperlink);

            if (i < hyperlinks.size() - 1) {
                formulaBuilder.append(" & CHAR(10) & ");
            }
        }

        return formulaBuilder.toString();
    }

    protected void applyHeaderCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext context) {
        HeaderCellStyleWriterUtil.applyCellStyles(sheet, cell, rowCell, cellField, fieldValue, context);
    }

    protected void applyDataCellStyles(Sheet sheet, Cell cell, org.apache.poi.ss.usermodel.Cell rowCell, Field cellField, Object fieldValue, ExcelSheetWriterContext context) {
        DataCellStyleWriterUtil.applyCellStyles(sheet, cell, rowCell, cellField, fieldValue, context);
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

    public void drawImageOnExcelSheet(org.apache.poi.ss.usermodel.Sheet sheet, int row1,
                                         int row2, int col1, int col2, int dx1, int dy1, int dx2, int dy2, Object pictureSource, Cell cell)  {

        try {
            byte[] bytes = null;
            if(cell.pictureSource().equals(PictureSourceType.BYTE_ARRAY)) {
                bytes = castFromWrapperByteArray(pictureSource);
            }
            else if(cell.pictureSource().equals(PictureSourceType.FILE_PATH)) {
                InputStream is = Files.newInputStream(Paths.get((String) pictureSource));
                bytes = IOUtils.toByteArray(is);
                is.close();
            }
            int pictureType = Workbook.PICTURE_TYPE_JPEG;
            if(cell.pictureType().equals(PictureType.PNG)){
                pictureType = Workbook.PICTURE_TYPE_PNG;
            }
            int pictureIdx = sheet.getWorkbook().addPicture(bytes,pictureType);
            CreationHelper helper = sheet.getWorkbook().getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(col1);
            anchor.setDx1(dx1);
            anchor.setRow1(row1);
            anchor.setDy1(dy1);
            anchor.setCol2(col2);
            anchor.setDx2(dx2);
            anchor.setRow2(row2);
            anchor.setDy2(dy2);
            anchor.setAnchorType(ClientAnchor.AnchorType.byId(cell.pictureAnchorType().value()));
            Picture pic = drawing.createPicture(anchor, pictureIdx);
            if(cell.pictureResizeScale()!=-1) {
                pic.resize(cell.pictureResizeScale());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] castFromWrapperByteArray(Object pictureSource) {
        Byte[] wrapperBytes = (Byte[]) pictureSource;
        return ArrayUtils.toPrimitive(wrapperBytes);
    }

}
