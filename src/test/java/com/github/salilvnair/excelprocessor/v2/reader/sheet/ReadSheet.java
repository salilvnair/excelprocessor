package com.github.salilvnair.excelprocessor.v2.reader.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Sheet("Read")
public class ReadSheet extends BaseSheet {
    @Cell("Name")
    private String name;

    @Cell("Age")
    private Long age;

    /** numeric header cell: used to throw IllegalStateException from getStringCellValue() */
    @Cell("2024")
    private Double sales;
}
