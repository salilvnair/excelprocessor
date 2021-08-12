package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(
        value="Employer",
        dynamicHeaders = true
)
public class EmployerSheet extends DynamicHeaderSheet {

}
