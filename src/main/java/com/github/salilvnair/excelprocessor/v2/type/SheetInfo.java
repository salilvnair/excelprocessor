package com.github.salilvnair.excelprocessor.v2.type;

import java.util.List;

/**
 * @author Salil V Nair
 */
public class SheetInfo {
    private String name;
    private int totalRows;
    private int totalColumns;
    private List<CellInfo> cells;

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int totalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int totalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(int totalColumns) {
        this.totalColumns = totalColumns;
    }

    public List<CellInfo> cells() {
        return cells;
    }

    public void setCells(List<CellInfo> cells) {
        this.cells = cells;
    }
}
