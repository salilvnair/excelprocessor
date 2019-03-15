package com.github.salilvnair.excelprocessor.test.sheet;

import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.reflect.type.PictureSourceType;

@ExcelSheet(value="School", hasValidation=true)
public class SchoolSheet extends BaseExcelValidationSheet{
    @ExcelHeader("Name")
    private String name;
    @ExcelHeader(value="State")
    private String state;
    @ExcelHeader("No of students")
    private Integer noOfStudents;
    @ExcelHeader(value="Image",
    			pictureResizeScale=-1,
    			picture=true,
    			pictureAnchorType = PictureAnchorType.DONT_MOVE_AND_RESIZE,
    			pictureSource=PictureSourceType.BYTE_ARRAY)
    private Byte[] image;
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
    public Integer getNoOfStudents() {
       return this.noOfStudents;
    }
    public void setNoOfStudents(Integer noOfStudents) {
        this.noOfStudents = noOfStudents;
    }
	@Override
	public String toString() {
		return "SchoolSheet [name=" + name + ", state=" + state + ", noOfStudents=" + noOfStudents + "]";
	}
	public Byte[] getImage() {
		return image;
	}
	public void setImage(Byte[] image) {
		this.image = image;
	}
       
}
