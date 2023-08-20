package com.github.salilvnair.excelprocessor.v2.model;

import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StyleTemplateCellInfo {
    private int row = 1;
    private String column ="A";

    public static StyleTemplateCellInfo.StyleTemplateCellInfoBuilder defaultValueBuilder() {
        return new StyleTemplateCellInfo().toBuilder();
    }
}
