package com.github.salilvnair.excelprocessor.v2.features.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.ExcelValue;

public enum Channel {
    @ExcelValue("Web shop") WEB,
    STORE,
    @ExcelValue({"Phone", "Call centre"}) PHONE
}
