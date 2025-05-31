package com.github.salilvnair.excelprocessor.v2.test.archived.sheet.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.HeaderCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;


@DataCellStyle(
        customTask = "highlightYellowIfValueIsEmpty"
)
@HeaderCellStyle(ignoreStyleTemplate = true)
@Sheet(
        value="College",
//        excelTask = DynamicSheetTask.class,
        dynamicHeaders = true,
//        useDefaultNumberType = true,
//        readValuesAsString = true,
//        hideColumns = {"A"}
        headerRowAt = 2
)
public class DynamicCollegeSheet extends DynamicHeaderSheet {
}
