package com.github.salilvnair.excelprocessor.v2.test.sheet.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.HeaderCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.StyleTemplateCell;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;


@DataCellStyle(
        customTask = "highlightYellowIfValueIsEmpty"
)
@HeaderCellStyle(ignoreStyleTemplate = true)
@Sheet(
        value="College",
        type = ExcelFileType.Extension.XLSX,
//        excelTask = DynamicSheetTask.class,
        dynamicHeaders = true,
        useDefaultNumberType = true,
        readValuesAsString = true,
        hideColumns = {"A"}
)
public class DynamicCollegeSheet extends DynamicHeaderSheet {
}
