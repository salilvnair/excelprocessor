package com.github.salilvnair.excelprocessor.v2.reader.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Sheet("Typed")
public class TypedReadSheet extends BaseSheet {
    @Cell("Name") private String name;
    @Cell("Age") private Integer age;
    @Cell("Joined") private Date joined;
    @Cell("Active") private Boolean active;
    @Cell("Total") private Double total;      // formula cell
    @Cell("2024") private Long sales;         // numeric header
}
