package com.github.salilvnair.excelprocessor.reflect.validator;

import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.service.AbstractCustomValidatorTask;

public class ConditionalValidator extends BaseExcelValidator {
	@Override
	public String validate(ValidatorContext validatorContext) {
		String errorMessage = null;			
		String headerKey = validatorContext.getHeaderKey(); 
		if(isUnknownExcelHeader(validatorContext, headerKey)) {
			return errorMessage;
		}
		AbstractCustomValidatorTask validatorTask = (AbstractCustomValidatorTask) validatorContext.getValidatorTask();
		if(validatorTask==null) {
			return null;
		}
		String methodName = null;
		ExcelHeaderValidator excelHeaderValidator = null;
		ExcelSheet sheetValidator = null;
		excelHeaderValidator = validatorContext.getExcelHeaderValidator();
		methodName = excelHeaderValidator.condition();
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(methodName)) {
			errorMessage = CustomMethodValidator.invokeCustomTask(validatorContext,methodName,sheetValidator,excelHeaderValidator,validatorTask);
		}
		return errorMessage;
	}

	
	
	
	
}
