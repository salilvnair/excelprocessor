package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.poi.ss.usermodel.BorderStyle;

@Sheet(value = "Dynamic", dynamicHeaders = true)
@DataCellStyle(hasBorderStyle = true, borderStyle = BorderStyle.THIN)
public class StyledDynamicSheet extends DynamicHeaderSheet {
}
