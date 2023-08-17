package com.github.salilvnair.excelprocessor.v2.model;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

public class DataCellStyleInfo {
    public boolean conditional = false;

    public String condition = "";

    public boolean applyDefaultStyles = false;

    public String customTask = "";

    public String[] customTasks = {};

    public boolean hasForegroundColor = false;

    public boolean hasBackgroundColor = false;

    public boolean hasBorderStyle = false;

    public boolean hasBorderColor = false;

    public boolean wrapText = false;

    public IndexedColors foregroundColor = IndexedColors.AUTOMATIC;

    public IndexedColors backgroundColor = IndexedColors.AUTOMATIC;

    public FillPatternType fillPattern = FillPatternType.SOLID_FOREGROUND;

    public BorderStyle borderStyle = BorderStyle.NONE;

    public IndexedColors borderColor = IndexedColors.AUTOMATIC;

    public int columnWidthInUnits = -1;


    public StyleTemplateCellInfo styleTemplateCellInfo = new StyleTemplateCellInfo();
}
