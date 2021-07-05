package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

@ExcelSheet(value="College", hasValidation=true)
public class CollegeSheet extends BaseExcelSheet {
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader("University")
    private String university;
    @ExcelHeader("State")
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
    public String getUniversity() {
       return this.university;
    }
    public void setUniversity(String university) {
        this.university = university;
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
		return "CollegeSheet [name=" + name + ", university=" + university + ", state=" + state + ", noOfStudents="
				+ noOfStudents + "]";
	}
    
}