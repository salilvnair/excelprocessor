package com.github.salilvnair.excelprocessor.v2.context;

import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DynamicCellValidationContext {
    private String header;
    private CellValidationInfo cellValidationInfo;
}
