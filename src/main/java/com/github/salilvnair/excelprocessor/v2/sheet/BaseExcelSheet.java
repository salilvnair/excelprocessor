package com.github.salilvnair.excelprocessor.v2.sheet;

public abstract class BaseExcelSheet implements IExcelSheet {
    private int rowIndex;

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }
}
