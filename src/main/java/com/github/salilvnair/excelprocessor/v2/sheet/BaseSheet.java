package com.github.salilvnair.excelprocessor.v2.sheet;

import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.util.List;
import java.util.Map;

public abstract class BaseSheet implements ExcelSheet {
    private int rowIndex;
    private int columnIndex;
    private int row;
    private String column;
    private Map<String, CellInfo> cells;
    private List<String> sheetHeaders;
    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
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

    public Map<String, CellInfo> cells() {
        return cells;
    }

    public void setCells(Map<String, CellInfo> cells) {
        this.cells = cells;
    }

    public List<String> sheetHeaders() {
        return sheetHeaders;
    }

    public void setSheetHeaders(List<String> sheetHeaders) {
        this.sheetHeaders = sheetHeaders;
    }
}
