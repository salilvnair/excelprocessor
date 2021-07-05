package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

@ExcelSheet(value="School", hasValidation=true)
public class SchoolSheet extends BaseExcelSheet {
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader(value="State")
    private String state;
    @ExcelHeader("No of students")
    private Double noOfStudents;
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
    public Double getNoOfStudents() {
       return this.noOfStudents;
    }
    public void setNoOfStudents(Double noOfStudents) {
        this.noOfStudents = noOfStudents;
    }
	@Override
	public String toString() {
		return "SchoolSheet [name=" + name + ", state=" + state + ", noOfStudents=" + noOfStudents + "]";
	}
}
