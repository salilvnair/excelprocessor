package com.github.salilvnair.excelprocessor.test.sheet;

import com.github.salilvnair.excelprocessor.bean.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;

@ExcelSheet(
        value="VerticalInfoSheet",
        isVertical=true,
        hasValidation = true,
        verticallyScatteredHeaders = true,
        headerRowAt=1,
        headerColumnAt="A",
        ignoreHeaders = {
                "Test Data",
                "GENERAL INFORAMTION",
                "ADDITIONAL INFORAMTION",
                "Internet Info",
                "Comments"
        }
)
public class VerticalInfoSheet extends BaseExcelValidationSheet {
    @ExcelHeader("First Name")
    private String firstName;
    @ExcelHeader("Last Name")
    @ExcelHeaderValidator(required = true)
    private String lastName;
    @ExcelHeader("Email")
    @ExcelHeaderValidator(email = true, userDefinedMessage = "Invalid email address!")
    private String email;
    @ExcelHeader("Age")
    private String age;
    @ExcelHeader("Connection Type")
    private String connectionType;
    @ExcelHeader("Number Prefix")
    private String numberPrefix;
    @ExcelHeader("Note")
    private String note;

    //getters and setters
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getAge() {
        return this.age;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public String getConnectionType() {
        return this.connectionType;
    }
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
    public String getNumberPrefix() {
        return this.numberPrefix;
    }
    public void setNumberPrefix(String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}