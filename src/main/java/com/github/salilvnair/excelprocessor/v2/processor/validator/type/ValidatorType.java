package com.github.salilvnair.excelprocessor.v2.processor.validator.type;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Salil V Nair
 */
public enum ValidatorType {
    NA,
    REQUIRED,
    CONDITIONAL,
    DATE,
    UNIQUE,
    PATTERN,
    MIN_ITEMS,
    MAX_ITEMS,
    EMAIL,
    MINLENGTH,
    MAXLENGTH,
    LENGTH,
    NUMERIC,
    ALPHANUMERIC,
    ALLOWED_VALUES;

    public static ValidatorType type(String name) {
        Optional<ValidatorType> typeEnum = Arrays.stream(ValidatorType.values())
                .filter(comp -> comp.name().equals(name)).findFirst();
        return typeEnum.orElse(null);
    }
}