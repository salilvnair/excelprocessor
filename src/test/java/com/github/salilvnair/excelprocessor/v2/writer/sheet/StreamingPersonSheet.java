package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.DataCellStyle;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.BorderStyle;

@Getter
@Setter
@Sheet(value = "PeopleStream", streamingWorkbook = true)
@DataCellStyle(hasBorderStyle = true, borderStyle = BorderStyle.THIN)
public class StreamingPersonSheet extends BaseSheet {
    @Cell("Name")
    private String name;

    @Cell("Age")
    private Long age;
}
