package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        sectional = true,
        vertical = true
)
@Getter
@Setter
public class Section2 extends BaseSheet {
    @Cell("Mobile")
    private String mobile;
    @Cell("Landphone")
    private String landPhone;
    @Cell("Fax")
    private String fax;
    @Section
    private SubSection2 subSection2;
    @Cell("Age")
    private String section2Age;
    @Cell("Address1")
    private String address1;
    @Cell("Address2")
    private String address2;
}
