package com.github.salilvnair.excelprocessor.v2.processor.model;

/**
 * @author Salil V Nair
 */
public class SectionRangeAddress {
    private int _firstRow;
    private int _firstCol;
    private int _lastRow;
    private int _lastCol;
    private String sectionBeginningText;
    private String sectionEndingText;


    public final int getFirstColumn() {
        return _firstCol;
    }

    public final int getFirstRow() {
        return _firstRow;
    }

    public final int getLastColumn() {
        return _lastCol;
    }

    public final int getLastRow() {
        return _lastRow;
    }

    public void setFirstRow(int firstRow) {
        this._firstRow = firstRow;
    }

    public void setFirstColumn(int firstCol) {
        this._firstCol = firstCol;
    }

    public void setLastRow(int lastRow) {
        this._lastRow = lastRow;
    }

    public void setLastColumn(int lastCol) {
        this._lastCol = lastCol;
    }

    public String getSectionBeginningText() {
        return sectionBeginningText;
    }

    public void setSectionBeginningText(String sectionBeginningText) {
        this.sectionBeginningText = sectionBeginningText;
    }

    public String getSectionEndingText() {
        return sectionEndingText;
    }

    public void setSectionEndingText(String sectionEndingText) {
        this.sectionEndingText = sectionEndingText;
    }
}
