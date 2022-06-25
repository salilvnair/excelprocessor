package com.github.salilvnair.excelprocessor.v2.processor.model;

import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;

/**
 * @author Salil V Nair
 */
public class SectionRangeAddress {
    private int _sectionBeginningRow;
    private int _sectionFirstCol;
    private int _sectionEndingRow;
    private int _sectionLastCol;
    private String sectionBeginningText;
    private String sectionEndingText;


    public final int sectionFirstColIndex() {
        return _sectionFirstCol;
    }
    public final int sectionLastColIndex() {
        return _sectionLastCol;
    }

    public final String sectionFirstColumn() {
        return ExcelSheetReader.toIndentName(_sectionFirstCol+1);
    }
    public final String sectionLastColumn() {
        return ExcelSheetReader.toIndentName(_sectionLastCol+1);
    }

    public final int sectionBeginningRow() {
        return (_sectionBeginningRow + 1);
    }
    public final int sectionEndingRow() {
        return (_sectionEndingRow + 1);
    }

    public final int sectionBeginningRowIndex() {
        return _sectionBeginningRow;
    }

    public final int sectionEndingRowIndex() {
        return _sectionEndingRow;
    }

    public String sectionBeginningText() {
        return sectionBeginningText;
    }

    public String sectionEndingText() {
        return sectionEndingText;
    }

    public void setSectionBeginningRowIndex(int r) {
        this._sectionBeginningRow = r;
    }

    public void setSectionEndingRowIndex(int r) {
        this._sectionEndingRow = r;
    }

    public void setSectionFirstColIndex(int firstCol) {
        this._sectionFirstCol = firstCol;
    }

    public void setSectionLastColIndex(int lastCol) {
        this._sectionLastCol = lastCol;
    }

    public void setSectionBeginningText(String sectionBeginningText) {
        this.sectionBeginningText = sectionBeginningText;
    }

    public void setSectionEndingText(String sectionEndingText) {
        this.sectionEndingText = sectionEndingText;
    }
}
