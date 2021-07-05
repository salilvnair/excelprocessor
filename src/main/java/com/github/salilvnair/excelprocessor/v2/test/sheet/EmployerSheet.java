package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

@ExcelSheet(value="Employer", hasValidation=true)
public class EmployerSheet extends BaseExcelSheet{
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader("State")
    private String state;
    @ExcelHeader("CMM Level")
    private Double cmmLevel;
    @ExcelHeader("No of employees")
    private Double noOfEmployees;

  //getters and setters
    public String getName() {
       return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getState() {
       return this.state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Double getCmmLevel() {
       return this.cmmLevel;
    }
    public void setCmmLevel(Double cmmLevel) {
        this.cmmLevel = cmmLevel;
    }
    public Double getNoOfEmployees() {
       return this.noOfEmployees;
    }
    public void setNoOfEmployees(Double noOfEmployees) {
        this.noOfEmployees = noOfEmployees;
    }
	@Override
	public String toString() {
		return "EmployerSheet [name=" + name + ", state=" + state + ", cmmLevel=" + cmmLevel + ", noOfEmployees="
				+ noOfEmployees + "]";
	}
    
}
