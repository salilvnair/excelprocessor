package com.github.salilvnair.excelprocessor.v2.reader.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

@Sheet(value = "Read", dynamicHeaders = true)
public class ReadDynamicSheet extends DynamicHeaderSheet {
}
