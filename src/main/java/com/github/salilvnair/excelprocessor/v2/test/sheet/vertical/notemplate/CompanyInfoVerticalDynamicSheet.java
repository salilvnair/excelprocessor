package com.github.salilvnair.excelprocessor.v2.test.sheet.vertical.notemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(
        value = "AllHandsEvent",
        dynamicHeaders = true,
        vertical = true
)
public class CompanyInfoVerticalDynamicSheet extends DynamicHeaderSheet {

}
