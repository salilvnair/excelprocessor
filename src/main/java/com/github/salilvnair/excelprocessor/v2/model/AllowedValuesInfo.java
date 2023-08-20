package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllowedValuesInfo {
    private String[] value = {};
    private int[] range = {};
    private String dataSetKey = "";
    private boolean allowNull = false;
    private boolean allowEmpty = false;
    private boolean conditional = false;
    private String condition = "";
    private String[] conditions = {};
    private boolean showValuesInMessage = false;
    private String message = "";
    private String messageId = "";
    private String messageType = MessageType.ERROR.name();
    private ConditionallyAllowedValuesInfo[] conditionallyAllowedValues = {};

    public static AllowedValuesInfo.AllowedValuesInfoBuilder defaultValueBuilder() {
        return new AllowedValuesInfo().toBuilder();
    }
}
