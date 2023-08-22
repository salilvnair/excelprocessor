package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FieldInfo {
    private String type;
    private boolean dateString;
    private boolean dateTimeString;
    private String dateFormat = DateParsingUtil.DateFormat.SLASH_MM_DD_YYYY.value();
    private String dateTimeFormat = DateParsingUtil.DateTimeFormat.DASH_MM_DD_YYYY_HH_MM.value();
}
