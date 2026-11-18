package com.github.salilvnair.excelprocessor.v2.features.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Sheet("Typed")
public class TypedRowSheet extends BaseSheet {
    @Cell("Joined")
    private LocalDate joined;

    @Cell("Created")
    private LocalDateTime created;

    @Cell("Amount")
    private BigDecimal amount;

    @Cell("Quantity")
    private int quantity;

    @Cell("Active")
    private boolean active;

    @Cell("Channel")
    private Channel channel;

    /** text cell "00042" */
    @Cell("Code")
    private Long code;

    @Cell("Grade")
    private Character grade;

    /** ISO text in the sheet */
    @Cell("Due")
    private LocalDate due;
}
