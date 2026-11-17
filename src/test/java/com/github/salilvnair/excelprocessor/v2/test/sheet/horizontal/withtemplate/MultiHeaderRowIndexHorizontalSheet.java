package com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.withtemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value = "Sheet1",
        userDefinedTemplate = true,
        multiPositionalHeaders = true,
        valueRowAt = 3,
        headerColumnEndsAt = "H"
)
@Getter
@Setter
public class MultiHeaderRowIndexHorizontalSheet extends DynamicHeaderSheet {
    @Cell(
            value = "A"
    )
    private String a;
    @Cell(
            value = "B"
    )
    private String b;
    @Cell(
            value = "C"
    )
    private String c;
    @Cell(
            value = "D",
            row = 2
    )
    private String d;
    @Cell(
            value = "E",
            row = 2
    )
    private String e;
    @Cell(
            value = "F",
            row = 2
    )
    private String f;
    @Cell(
            value = "G"
    )
    private String g;
    @Cell(
            value = "H"
    )
    private String h;
}
