package com.github.salilvnair.excelprocessor.v2.test.archived.sheet.dynamic;

import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.HeaderCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.StyleTemplateCell;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;


@DataCellStyle(
        customTask = "highlightYellowIfValueIsEmpty"
)
@HeaderCellStyle(styleTemplateCell = @StyleTemplateCell(row = 1, column = "B"))
@Sheet(
        value="School",
        dynamicHeaders = true,
        headerRowAt = 5,
        headerColumnAt = "C",
        vertical = true
//        useDefaultNumberType = true,
//        readValuesAsString = true,
//        hideColumns = {"A"}

)
public class DynamicSchoolSheet extends DynamicHeaderSheet {
}
