package com.github.salilvnair.excelprocessor.v1.test.sheet;

import com.github.salilvnair.excelprocessor.v1.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.v1.reflect.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.v1.test.task.SchoolSheetTask;
import org.apache.poi.ss.usermodel.IndexedColors;

@ExcelSheet(
        value="School",
        hasValidation=true,
        customTaskValidator = SchoolSheetTask.class,
        highlightCellWithError = true,
        highlightedErrorCellColor = IndexedColors.RED,
        commentCellWithError = true )
public class SchoolSheet extends BaseExcelValidationSheet{
    @ExcelHeader("Name")
    @ExcelHeaderValidator(required = true)
    private String name;
    @ExcelHeader(value="State")
    @ExcelHeaderValidator(required = true)
    private String state;
    @ExcelHeader("No of students")
    @ExcelHeaderValidator(conditional = true, condition = "noOfStudentCheck")
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
