package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;

import java.util.List;

public interface IExcelValidator {
	List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext);
}
