package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.ss.usermodel.BorderStyle;

@Sheet(value = "TaskStyled", dynamicHeaders = true, excelTask = HighlightTask.class)
@DataCellStyle(applyDefaultStyles = true, hasBorderStyle = true, borderStyle = BorderStyle.THIN, customTask = "highlightEmpty")
public class TaskStyledDynamicSheet extends DynamicHeaderSheet {
}
