package com.github.salilvnair.excelprocessor.v2.processor.validator.type;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Salil V Nair
 */
public enum MessageType {
    INFO,
    ERROR,
    WARNING,
    SUCCESS;


    public static MessageType type(String name) {
        Optional<MessageType> typeEnum = Arrays.stream(MessageType.values())
                .filter(comp -> comp.name().equals(name)).findFirst();
        return typeEnum.orElse(null);
    }
}