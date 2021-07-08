package com.github.salilvnair.excelprocessor.v2.type;

import java.lang.reflect.Type;

public class CellInfo {
    private Object value;
    private int rowIndex;
    private int columnIndex;
    private Type cellType;

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
}
