package com.github.salilvnair.excelprocessor.test.sheet;

import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;

@ExcelSheet(value="College", hasValidation=true)
public class CollegeSheet extends BaseExcelValidationSheet{
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader("University")
    private String university;
    @ExcelHeader("State")
    private String state;
    @ExcelHeader("No of students")
    private Integer noOfStudents;

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
    public Integer getNoOfStudents() {
       return this.noOfStudents;
    }
    public void setNoOfStudents(Integer noOfStudents) {
        this.noOfStudents = noOfStudents;
    }
	@Override
	public String toString() {
		return "CollegeSheet [name=" + name + ", university=" + university + ", state=" + state + ", noOfStudents="
				+ noOfStudents + "]";
	}
    
}