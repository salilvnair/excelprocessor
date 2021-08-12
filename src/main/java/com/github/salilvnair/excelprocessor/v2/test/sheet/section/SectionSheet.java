package com.github.salilvnair.excelprocessor.v2.test.sheet.section;

import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
@Sheet(
        value="SectionSheet",
        vertical=true,
        sectional = true,
        headerRowAt=1,
        headerColumnAt="B"
)
public class SectionSheet extends BaseSheet {
    @Section
    private Section1 section1;
    @Section
    private Section2 section2;

    //getters and setters
    public Section1 getSection1() {
        return section1;
    }

    public void setSection1(Section1 section1) {
        this.section1 = section1;
    }

    public Section2 getSection2() {
        return section2;
    }

    public void setSection2(Section2 section2) {
        this.section2 = section2;
    }
}
