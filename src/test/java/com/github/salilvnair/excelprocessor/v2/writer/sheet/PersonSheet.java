package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.NumberStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.annotation.TextStyle;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

@Getter
@Setter
@Sheet(value = "People")
@DataCellStyle(hasBorderStyle = true, borderStyle = BorderStyle.THIN)
public class PersonSheet extends BaseSheet {
    @Cell("Name")
    private String name;

    @Cell("Age")
    @DataCellStyle(hasBackgroundColor = true, backgroundColor = IndexedColors.YELLOW,
                   customTextStyle = true, textStyle = @TextStyle(bold = true),
                   numberStyle = @NumberStyle(custom = true, customFormat = "0"))
    private Long age;

    @Cell("Website")
    private String website;
}
