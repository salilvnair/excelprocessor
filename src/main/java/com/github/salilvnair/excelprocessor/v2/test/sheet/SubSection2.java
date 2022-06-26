package com.github.salilvnair.excelprocessor.v2.test.sheet;

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
public class SubSection2 extends BaseSheet {
    @Cell("Age")
    private String subSection2Age;
}
