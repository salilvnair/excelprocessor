package com.github.salilvnair.excelprocessor.v2.test.sheet;


import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.MergedCell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.merged.OtherInfoMergedHeader;
import com.github.salilvnair.excelprocessor.v2.test.sheet.merged.RankInfoMergedHeader;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.CollegeSheetTaskValidator;

@Sheet(
        value="College With Merged HeaderInfo",
        headerRowAt = 2,
        mergedHeaders = true,
        mergedHeaderRowAt = 1,
        excelTaskValidator = CollegeSheetTaskValidator.class
)
public class CollegeMergedHeaderSheet extends BaseSheet {
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

    @MergedCell("RankInfo")
    private RankInfoMergedHeader rankInfo;
    @MergedCell("OtherInfo")
    private OtherInfoMergedHeader otherInfo;

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

    public RankInfoMergedHeader getRankInfo() {
        return rankInfo;
    }

    public void setRankInfo(RankInfoMergedHeader rankInfo) {
        this.rankInfo = rankInfo;
    }

    public OtherInfoMergedHeader getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(OtherInfoMergedHeader otherInfo) {
        this.otherInfo = otherInfo;
    }
}