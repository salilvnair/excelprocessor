package com.github.salilvnair.excelprocessor.v2.model;

import lombok.*;
import org.apache.poi.ss.usermodel.IndexedColors;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TextStyleInfo {
    private IndexedColors color = IndexedColors.AUTOMATIC;

    private boolean italic =false;

    private boolean bold =false;

    private boolean strikeout =false;

    private short fontHeight =-1;

    private String fontName ="";
}
