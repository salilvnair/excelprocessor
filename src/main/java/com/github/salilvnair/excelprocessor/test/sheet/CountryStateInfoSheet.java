package com.github.salilvnair.excelprocessor.test.sheet;

import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import org.apache.poi.ss.usermodel.IndexedColors;

@ExcelSheet(
        value="CountryStateInfo",
        isSingleValueVerticalSheet = true,
        hasValidation=true,
        isVertical=true,
        headerRowAt=2,
        headerColumnAt="B",
        highlightCellWithError = true,
        highlightedErrorCellColor = IndexedColors.RED,
        commentCellWithError = true,
        ignoreHeaders = {
                "General",
                "Info"
        }
)
public class CountryStateInfoSheet extends BaseExcelValidationSheet {
    @ExcelHeader("Country")
    private String country;
    @ExcelHeader("State")
    @ExcelHeaderValidator(required = true)
    private String state;
    @ExcelHeader("Number of schools")
    private Integer numberOfSchools;
    @ExcelHeader("State Govt")
    private Integer stateGovt;
    @ExcelHeader("Private")
    private Integer privateCount;

    //getters and setters
    public String getCountry() {
        return this.country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getState() {
        return this.state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Integer getNumberOfSchools() {
        return this.numberOfSchools;
    }
    public void setNumberOfSchools(Integer numberOfSchools) {
        this.numberOfSchools = numberOfSchools;
    }
    public Integer getStateGovt() {
        return this.stateGovt;
    }
    public void setStateGovt(Integer stateGovt) {
        this.stateGovt = stateGovt;
    }
    public Integer getPrivateCount() {
        return privateCount;
    }

    public void setPrivateCount(Integer privateCount) {
        this.privateCount = privateCount;
    }
}