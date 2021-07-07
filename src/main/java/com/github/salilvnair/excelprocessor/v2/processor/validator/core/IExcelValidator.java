package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;

import java.util.List;

public interface IExcelValidator {
	List<ValidationMessage> validate(Object currentInstance, ValidatorContext validatorContext);
}
