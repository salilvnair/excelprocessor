package com.github.salilvnair.excelprocessor.v2.test.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.MultiOrientedSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

/**
 * @author Salil V Nair
 */
@MultiOrientedSheet(
        name = "MultiOrientedSheet",
        value = {
                CountryStateInfoSheet.class,
                CollegeSheet.class
        }
)
public class CollegeCountryStateInfoSheet extends BaseExcelSheet {
}
