package com.github.salilvnair.excelprocessor.reflect.validator;

import com.github.salilvnair.excelprocessor.reflect.context.ExcelValidationMessage;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;

public interface IExcelValidator {
	
	String validate(ValidatorContext validatorContext);

	ExcelValidationMessage validateInDetail(ValidatorContext validatorContext);
	
}
