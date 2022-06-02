package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value="SectionSheet",
        vertical=true,
        duplicateHeaders=true,
        headerRowAt=2,
        headerColumnAt="B",
        sectional = true,
        ordered = true
)
@Getter
@Setter
public class SectionSheet extends BaseSheet {
    @Section
    private Section1 section1;

    @Section
    private Section2 section2;
}
