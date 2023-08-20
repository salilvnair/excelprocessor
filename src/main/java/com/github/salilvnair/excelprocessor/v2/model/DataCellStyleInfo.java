package com.github.salilvnair.excelprocessor.v2.model;

import lombok.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataCellStyleInfo {
    private boolean conditional = false;

    private String condition = "";

    private boolean applyDefaultStyles = false;

    private String customTask = "";

    private String[] customTasks = {};

    private boolean hasForegroundColor = false;

    private boolean hasBackgroundColor = false;

    private boolean hasBorderStyle = false;

    private boolean hasBorderColor = false;

    private boolean wrapText = false;

    private IndexedColors foregroundColor = IndexedColors.AUTOMATIC;

    private IndexedColors backgroundColor = IndexedColors.AUTOMATIC;

    private FillPatternType fillPattern = FillPatternType.SOLID_FOREGROUND;

    private BorderStyle borderStyle = BorderStyle.NONE;

    private IndexedColors borderColor = IndexedColors.AUTOMATIC;

    private int columnWidthInUnits = -1;

    private StyleTemplateCellInfo styleTemplateCellInfo = new StyleTemplateCellInfo();
}
