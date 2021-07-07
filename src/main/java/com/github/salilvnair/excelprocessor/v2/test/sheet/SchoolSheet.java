package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.SchoolSheetTaskValidator;

@ExcelSheet(
        value="School",
        hasValidation=true,
        excelTaskValidator = SchoolSheetTaskValidator.class
)
public class SchoolSheet extends BaseExcelSheet {
    @ExcelHeader("Name")
    private String name;
    @ExcelHeaderValidator(required = true, messageId = "10001")
    @ExcelHeader(value="State")
    private String state;
    @ExcelHeaderValidator(conditional = true, condition = "shouldBeGreaterThanZero", messageId = "2000")
    @ExcelHeader("No of students")
    private Long noOfStudents;
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
    public Long getNoOfStudents() {
       return this.noOfStudents;
    }
    public void setNoOfStudents(Long noOfStudents) {
        this.noOfStudents = noOfStudents;
    }
	@Override
	public String toString() {
		return "SchoolSheet [name=" + name + ", state=" + state + ", noOfStudents=" + noOfStudents + "]";
	}
}
