package com.github.salilvnair.excelprocessor.v2.type;

import java.lang.reflect.Type;

public class CellInfo {
    public static final String CELL_TYPE_STRING = "String";
    public static final String CELL_TYPE_DOUBLE = "Double";
    public static final String CELL_TYPE_DATE = "Date";
    public static final String CELL_TYPE_BOOLEAN = "Boolean";
    private Object value;
    private int rowIndex;
    private int columnIndex;
    private Type cellType;
    private String cellTypeString;

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int rowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int columnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public Type cellType() {
        return cellType;
    }

    public void setCellType(Type cellType) {
        this.cellType = cellType;
    }

    public String cellTypeString() {
        return cellTypeString;
    }

    public void setCellTypeString(String cellTypeString) {
        this.cellTypeString = cellTypeString;
    }
}
