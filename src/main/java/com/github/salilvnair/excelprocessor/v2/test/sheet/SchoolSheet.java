package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.SchoolSheetTaskValidator;

@Sheet(
        value = "School",
        hasValidation = true,
        hasDuplicateHeaders = true,
        excelTaskValidator = SchoolSheetTaskValidator.class
)
public class SchoolSheet extends BaseSheet {
    @Cell("Name")
    private String name;
    @CellValidation(required = true, messageId = "10001")
    @Cell(value="State")
    private String state;
    @CellValidation(conditional = true, condition = "shouldBeGreaterThanZero", messageId = "2000")
    @Cell("No of students")
    private Long noOfStudents;
    @Cell(value = "Name", column = "D")
    private String duplicateName;
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
    public String getDuplicateName() {
        return duplicateName;
    }

    public void setDuplicateName(String duplicateName) {
        this.duplicateName = duplicateName;
    }
	@Override
	public String toString() {
		return "SchoolSheet [name=" + name + ", state=" + state + ", noOfStudents=" + noOfStudents + ", duplicateName="+duplicateName+"]";
	}
}
