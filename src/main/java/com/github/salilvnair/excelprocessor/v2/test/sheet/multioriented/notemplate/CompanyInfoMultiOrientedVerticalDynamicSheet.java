package com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(
        value = "AllHandsEvent",
        dynamicHeaders = true
)
public class CompanyInfoMultiOrientedVerticalDynamicSheet extends DynamicHeaderSheet {

}
