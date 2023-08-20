package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDefinedMessageInfo {
    private String validatorType;
    private String message = "";
    private String messageId = "";
    private String messageType = MessageType.ERROR.name();
}
