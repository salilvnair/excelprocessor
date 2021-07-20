package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

@Sheet(
        value="CountryStateInfo",
        vertical =true,
        headerRowAt=2,
        duplicateHeaders = true,
        ignoreHeaders = {
                "General",
                "Info"
        }
)
public class CountryStateInfoSheet extends BaseSheet {
    @Cell("Country")
    private String country;
    @CellValidation(required = true, messageId = "2000")
    @Cell("State")
    private String state;
    //@Cell("Number of schools")
    private Long numberOfSchools;
    //@Cell("State Govt")
    private Long stateGovt;
    //@Cell("Private")
    private Long privateCount;
    //@Cell(value = "Private", row = 9)
    private Long duplicatePrivateCount;

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
    public Long getNumberOfSchools() {
        return this.numberOfSchools;
    }
    public void setNumberOfSchools(Long numberOfSchools) {
        this.numberOfSchools = numberOfSchools;
    }
    public Long getStateGovt() {
        return this.stateGovt;
    }
    public void setStateGovt(Long stateGovt) {
        this.stateGovt = stateGovt;
    }
    public Long getPrivateCount() {
        return privateCount;
    }

    public void setPrivateCount(Long privateCount) {
        this.privateCount = privateCount;
    }

    public Long getDuplicatePrivateCount() {
        return duplicatePrivateCount;
    }

    public void setDuplicatePrivateCount(Long duplicatePrivateCount) {
        this.duplicatePrivateCount = duplicatePrivateCount;
    }

    @Override
    public String toString() {
        return "CountryStateInfoSheet{" +
                "country='" + country + '\'' +
                ", state='" + state + '\'' +
                ", numberOfSchools=" + numberOfSchools +
                ", stateGovt=" + stateGovt +
                ", privateCount=" + privateCount +
                ", duplicatePrivateCount=" + duplicatePrivateCount +
                '}';
    }
}