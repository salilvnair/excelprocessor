package com.github.salilvnair.excelprocessor.v2.test.sheet.section;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Section;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
@Sheet(
        sectional = true,
        vertical = true
)
public class Section2 extends BaseSheet {
    @CellValidation(required = true)
    @Cell("Mobile")
    private Long mobile;
    @Cell("Landphone")
    private Long landphone;
    @Cell("Fax")
    private Long fax;
    @Section
    private SubSection2 subSection2;
    @Cell(value = "Age", row = 14)
    private Long age;
    @Cell("Address1")
    private String address1;
    @Cell("Address2")
    private String address2;

    public Long getMobile() {
        return this.mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }

    public Long getLandphone() {
        return this.landphone;
    }

    public void setLandphone(Long landphone) {
        this.landphone = landphone;
    }

    public Long getFax() {
        return this.fax;
    }

    public void setFax(Long fax) {
        this.fax = fax;
    }

    public SubSection2 getSubSection2() {
        return subSection2;
    }

    public void setSubSection2(SubSection2 subSection2) {
        this.subSection2 = subSection2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}
