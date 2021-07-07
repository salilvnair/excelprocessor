package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.CollegeSheetTaskValidator;

@ExcelSheet(
        value="College",
        hasValidation=true,
        excelTaskValidator = CollegeSheetTaskValidator.class
)
public class CollegeSheet extends BaseExcelSheet {
    @ExcelHeader("Name")
    private String name;
    @ExcelHeaderValidator(customTask = "defaultUniversity")
    @ExcelHeader("University")
    private String university;
    @ExcelHeader("State")
    private String state;
    @ExcelHeaderValidator(conditional = true, condition = "shouldBeGreaterThanZero")
    @ExcelHeader("No of students")
    private Long noOfStudents;

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
    public Long getNoOfStudents() {
       return this.noOfStudents;
    }
    public void setNoOfStudents(Long noOfStudents) {
        this.noOfStudents = noOfStudents;
    }
	@Override
	public String toString() {
		return "CollegeSheet [name=" + name + ", university=" + university + ", state=" + state + ", noOfStudents="
				+ noOfStudents + "]";
	}
    
}