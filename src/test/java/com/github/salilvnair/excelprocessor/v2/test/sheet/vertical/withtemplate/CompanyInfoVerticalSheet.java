package com.github.salilvnair.excelprocessor.v2.test.sheet.vertical.withtemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value = "AllHandsEvent",
        vertical = true,
        userDefinedTemplate = true
)
@Getter
@Setter
public class CompanyInfoVerticalSheet extends BaseSheet {
    @Cell(
            value = "Name"
    )
    private String name;
    @Cell(
            value = "Company Id"
    )
    private String companyId;
    @Cell(
            value = "Front Desk Phone Number"
    )
    private String frontDeskPhoneNumber;
    @Cell(
            value = "Address"
    )
    private String address;
}
