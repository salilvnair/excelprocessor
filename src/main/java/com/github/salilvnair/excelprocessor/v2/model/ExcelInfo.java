package com.github.salilvnair.excelprocessor.v2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Salil V Nair
 */
public class ExcelInfo {
    private List<SheetInfo> sheets;

    public List<SheetInfo> sheets() {
        if(sheets == null) {
            sheets = new ArrayList<>();
        }
        return sheets;
    }

    public void setSheets(List<SheetInfo> sheets) {
        this.sheets = sheets;
    }
}
