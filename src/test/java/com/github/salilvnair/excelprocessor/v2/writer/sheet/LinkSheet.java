package com.github.salilvnair.excelprocessor.v2.writer.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Sheet(value = "Links")
public class LinkSheet extends BaseSheet {
    @Cell(value = "Link", hyperLink = true)
    private String link;

    @Cell(value = "Links", hyperLink = true, multiHyperLink = true)
    private String links;

    @Cell(value = "Flag")
    private Boolean flag;
}
