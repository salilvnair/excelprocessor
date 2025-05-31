package com.github.salilvnair.excelprocessor.v2.test.archived.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.SectionHint;
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
        ordered = true,
        sectionHints = {
                @SectionHint(beginningTextLike = "Complete Section", endingTextLike = "End of section", findClosestMatch = true),
                @SectionHint(beginningTextLike = "Use this sub-section", endingTextLike = "End of sub-section", findClosestMatch = true)
        }
)
@Getter
@Setter
public class SectionSheet extends BaseSheet {
    @Section(beginningText = "Complete Section 1", endingText = "End of section 1")
    private Section1 section1;

    @Section(beginningText = "Complete Section 2", endingText = "End of section 2")
    private Section2 section2;
}
