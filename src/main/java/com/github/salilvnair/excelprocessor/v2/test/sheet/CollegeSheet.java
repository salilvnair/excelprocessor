package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.CollegeSheetTaskValidator;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;
import com.github.salilvnair.excelprocessor.v2.type.PictureSourceType;

import java.util.List;

@Sheet(
        value="College",
        type = ExcelFileType.Extension.XLSX,
        excelTaskValidator = CollegeSheetTaskValidator.class,
        headerRowAt = 1,
        headerColumnAt = "B"
)
@DataCellStyle(
        customTask = "highlightYellowIfValueIsEmpty"
)
public class CollegeSheet extends BaseSheet {
    @Cell("Name")
    private String name;
    @CellValidation(customTask = "defaultUniversity")
    @Cell("University")
    private String university;
    @Cell("State")
    private String state;
    @CellValidation(conditional = true, condition = "shouldBeGreaterThanZero")
    @Cell("No of students")
    private Long noOfStudents;

    @Cell(value = "University Webpage URL", hyperLink = true, hyperLinkText = "Click here to open")
    private String universityHomepageURL;

    @HeaderCellStyle(columnWidthInUnits = 50*256)
    @Cell(value="Images",
        pictureResizeScale=-1,
        multiPicture = true,
        pictureHeightInPixels = 30,
        pictureWidthInPixels = 30,
        pictureMarginInPixels = 5,
        pictureSource= PictureSourceType.BYTE_ARRAY)
    private List<Byte[]> images;

    @HeaderCellStyle(columnWidthInUnits = 50*256)
    @Cell(value="Image",
        pictureResizeScale=-1,
        picture = true,
        pictureHeightInPixels = 30,
        pictureWidthInPixels = 30,
        pictureMarginInPixels = 5,
        pictureSource= PictureSourceType.BYTE_ARRAY)
    private Byte[] image;

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

    public String getUniversityHomepageURL() {
        return universityHomepageURL;
    }

    public void setUniversityHomepageURL(String universityHomepageURL) {
        this.universityHomepageURL = universityHomepageURL;
    }

    public List<Byte[]> getImages() {
        return images;
    }

    public void setImages(List<Byte[]> images) {
        this.images = images;
    }

    public Byte[] getImage() {
        return image;
    }

    public void setImage(Byte[] image) {
        this.image = image;
    }

}