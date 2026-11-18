package com.github.salilvnair.excelprocessor.v2.features.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(value = "Report", dynamicHeaders = true, allowedValuesDropdown = true, freezeHeader = true)
public class ReportSheet extends DynamicHeaderSheet {
}
