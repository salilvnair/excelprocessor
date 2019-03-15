package com.github.salilvnair.excelprocessor.test.sheet;

import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;

@ExcelSheet(value="Employer", hasValidation=true)
public class EmployerSheet extends BaseExcelValidationSheet{
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader("State")
    private String state;
    @ExcelHeader("CMM Level")
    private Integer cmmLevel;
    @ExcelHeader("No of employees")
    private Integer noOfEmployees;

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
    public Integer getCmmLevel() {
       return this.cmmLevel;
    }
    public void setCmmLevel(Integer cmmLevel) {
        this.cmmLevel = cmmLevel;
    }
    public Integer getNoOfEmployees() {
       return this.noOfEmployees;
    }
    public void setNoOfEmployees(Integer noOfEmployees) {
        this.noOfEmployees = noOfEmployees;
    }
	@Override
	public String toString() {
		return "EmployerSheet [name=" + name + ", state=" + state + ", cmmLevel=" + cmmLevel + ", noOfEmployees="
				+ noOfEmployees + "]";
	}
    
}
