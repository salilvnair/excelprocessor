package com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.withtemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(
        value = "AllHandsEvent",
        dynamicHeaders = true,
        vertical = true,
        userDefinedTemplate = true
)
public class UserInfoMultiOrientedVerticalDynamicSheet extends DynamicHeaderSheet {

}
