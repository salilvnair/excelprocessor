package com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(
        value = "AllHandsEvent",
        dynamicHeaders = true,
        headerRowAt = 3,
        headerColumnAt = "F"
)
public class CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet extends DynamicHeaderSheet {

}
