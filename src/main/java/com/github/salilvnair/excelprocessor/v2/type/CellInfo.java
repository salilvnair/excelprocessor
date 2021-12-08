package com.github.salilvnair.excelprocessor.v2.type;

import org.apache.poi.ss.usermodel.Color;

import java.lang.reflect.Type;

public class CellInfo {
    public static final String CELL_TYPE_STRING = "String";
    public static final String CELL_TYPE_DOUBLE = "Double";
    public static final String CELL_TYPE_DATE = "Date";
    public static final String CELL_TYPE_BOOLEAN = "Boolean";
    private String originalHeader;
    private String header;
    private Object value;
    private int rowIndex;
    private int columnIndex;
    private int row;
    private String column;
    private Type cellType;
    private String cellTypeString;
    private Color backgroundColor;
    private Color foregroundColor;
    private String backgroundHexString;
    private String foregroundHexString;
    private short[] backgroundRgb;
    private short[] foregroundRgb;

    //util functions
    public Object value() {
        return value;
    }

    public int rowIndex() {
        return rowIndex;
    }

    public int columnIndex() {
        return columnIndex;
    }

    public int row() {
        return row;
    }

    public String column() {
        return column;
    }

    public Type cellType() {
        return cellType;
    }

    public String cellTypeString() {
        return cellTypeString;
    }

    public String originalHeader() {
        return originalHeader;
    }

    public String header() {
        return header;
    }

    //getters and setters
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public void setCellType(Type cellType) {
        this.cellType = cellType;
    }

    public void setCellTypeString(String cellTypeString) {
        this.cellTypeString = cellTypeString;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public Object getValue() {
        return value;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public Type getCellType() {
        return cellType;
    }

    public String getCellTypeString() {
        return cellTypeString;
    }

    public String getBackgroundHexString() {
        return backgroundHexString;
    }

    public void setBackgroundHexString(String backgroundHexString) {
        this.backgroundHexString = backgroundHexString;
    }

    public String getForegroundHexString() {
        return foregroundHexString;
    }

    public void setForegroundHexString(String foregroundHexString) {
        this.foregroundHexString = foregroundHexString;
    }

    public short[] getBackgroundRgb() {
        return backgroundRgb;
    }

    public void setBackgroundRgb(short[] backgroundRgb) {
        this.backgroundRgb = backgroundRgb;
    }

    public short[] getForegroundRgb() {
        return foregroundRgb;
    }

    public void setForegroundRgb(short[] foregroundRgb) {
        this.foregroundRgb = foregroundRgb;
    }

    public String getOriginalHeader() {
        return originalHeader;
    }

    public void setOriginalHeader(String originalHeader) {
        this.originalHeader = originalHeader;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
