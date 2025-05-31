package com.github.salilvnair.excelprocessor.v2.test.archived.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
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
public class Section1 extends BaseSheet {
    @Cell("Name")
    private String name;
    @Cell("Fathers Name")
    private String fathersName;
    @Cell("Age")
    private String section1Age;
    @Cell("Family Name")
    private String familyName;
}
