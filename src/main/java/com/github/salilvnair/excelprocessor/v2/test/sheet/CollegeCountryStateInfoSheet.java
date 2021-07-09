package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

/**
 * @author Salil V Nair
 */
@MultiOrientedSheet(
        name = "MultiOrientedSheet",
        value = {
                MultiOrientedCountryStateInfoSheet.class,
                MultiOrientedCollegeSheet.class
        }
)
public class CollegeCountryStateInfoSheet extends BaseSheet {
}
