package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;



public class AlphaNumericValidator extends BaseExcelValidator {
	@Override
	public String validate(ValidatorContext validatorContext) {
		String errorMessage = null;
		Object columnValue = validatorContext.getColumnValue();				
		String headerKey = validatorContext.getHeaderKey(); 
		if(isUnknownExcelHeader(validatorContext, headerKey)) {
			return errorMessage;
		}
		int rowNum = validatorContext.getRowNum();
		String columnName = validatorContext.getColumnName();
		ExcelHeaderValidator excelHeaderValidator = validatorContext.getExcelHeaderValidator();
		ExcelSheet excelSheet = validatorContext.getExcelSheet();
		boolean isFieldAlphaNumeric = false;
		if(excelHeaderValidator.alphaNumeric()){
			isFieldAlphaNumeric = true;
		}
		boolean invalidAlphaNumericDetected = false;
		if(isFieldAlphaNumeric) {
			if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
				invalidAlphaNumericDetected = true;
			}
			else if(columnValue instanceof String) {
				String columnStringValue = (String) columnValue;
				String patternString = "[A-Za-z0-9]+";				
				invalidAlphaNumericDetected = !(PatternValidator.isValid(patternString, columnStringValue));
			}	
			else {
				invalidAlphaNumericDetected = true;
			}
			errorMessage = prepareErrorString(columnValue,
					validatorContext,
					headerKey,
					rowNum,
					columnName,
					invalidAlphaNumericDetected,
					excelHeaderValidator,
					excelSheet);
		}
		return errorMessage;
	}
	
	private String prepareErrorString(Object columnValue,
										ValidatorContext validatorContext,
										String headerKey,
										int rowNum ,
										String columnName,
										boolean invalidAlphaNumericDetected,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		if(invalidAlphaNumericDetected) {
			String columnAt = getColumn(validatorContext);
			errorMessage = headerKey+" at row["+rowNum+"] is not a valid alphanumric format.";
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
				errorMessage = headerKey+"'s length at row["+rowNum+"],column["+columnAt+"] is not a valid alphanumric format.";
			}
			String userDefinedMessage = excelHeaderValidator.userDefinedMessage();
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.userDefinedMessage())){
				if(excelSheet!=null && excelSheet.isVertical()){
					errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, ""+columnName);
				}
				else{
					errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowNum);	
				}				
			}
			if(excelHeaderValidator.userDefinedMessages().length>0) {
				for(String userDefinedMessageItr : excelHeaderValidator.userDefinedMessages()) {
					if(userDefinedMessageItr.contains(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_PATTERN)) {
						userDefinedMessageItr = userDefinedMessageItr.split(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_SPLIT_HOLDER)[1];
						if(excelSheet!=null && excelSheet.isVertical()){
							errorMessage = userDefinedMessageItr.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, ""+columnName);
						}
						else{
							errorMessage = userDefinedMessageItr.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowNum);	
						}
					}
				}
			}
		}
		return errorMessage;
	}
}
