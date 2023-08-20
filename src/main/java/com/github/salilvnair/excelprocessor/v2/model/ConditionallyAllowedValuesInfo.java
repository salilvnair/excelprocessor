package com.github.salilvnair.excelprocessor.v2.model;


import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConditionallyAllowedValuesInfo {
	private String condition = "";
    private int[] range = {};
    private String[] value = {};
    private String dataSetKey = "";
    private boolean allowNull = false;
    private boolean allowEmpty = false;
    private boolean showValuesInMessage = false;
    private String message = "";
    private String messageId = "";
    private String messageType = MessageType.ERROR.name();
}
