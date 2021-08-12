package com.github.salilvnair.excelprocessor.v2.test.sheet.section;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
@Sheet(
        sectional = true,
        vertical = true
)
public class SubSection2 extends BaseSheet {
    @CellValidation(required = true)
    @Cell(value = "Age", row = 12)
    private Long age;

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}
