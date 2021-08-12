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
public class Section1 extends BaseSheet {
    @Cell("Name")
    @CellValidation(required = true)
    private String name;
    @Cell("Fathers Name")
    private String fathersName;
    @Cell("Family Name")
    private String familyName;
    @Cell(value = "Age", row = 4)
    private Long age;


    //getters and setters
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFathersName() {
        return this.fathersName;
    }
    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }
    public String getFamilyName() {
        return this.familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}
