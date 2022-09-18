package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import org.apache.commons.lang3.math.NumberUtils;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;



public class NumericValidator extends BaseExcelValidator {
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
		boolean isFieldNumeric = false;
		if(excelHeaderValidator.numeric()){
			isFieldNumeric = true;
		}
		String typeOfNumeric = excelHeaderValidator.typeOfNumeric();
		boolean isDefaultNumericType = false;
		if(typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_DEFAULT)) {
			isDefaultNumericType = true;
		}
		boolean invalidNumericDetected = false;
		if(isFieldNumeric) {
			if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
				invalidNumericDetected = true;
			}
			else if(columnValue instanceof String) {
				String columnStringValue = (String) columnValue;
				invalidNumericDetected = !(NumberUtils.isCreatable(columnStringValue));
			}	
			else if(columnValue instanceof Long) {
				if(!isDefaultNumericType) {
					if(!typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_LONG)) {
						invalidNumericDetected = true;
					}
				}
			}
			else if(columnValue instanceof Double) {
				if(!isDefaultNumericType) {
					if(!typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_DOUBLE)) {
						invalidNumericDetected = true;
					}
				}
			}
			else if(columnValue instanceof Short) {
				if(!isDefaultNumericType) {
					if(!typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_SHORT)) {
						invalidNumericDetected = true;
					}
				}
			}
			else if(columnValue instanceof Integer) {
				if(!isDefaultNumericType) {
					if(!typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_INTEGER)) {
						invalidNumericDetected = true;
					}
				}
			}
			else if(columnValue instanceof Float) {
				if(!isDefaultNumericType) {
					if(!typeOfNumeric.equals(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_FLOAT)) {
						invalidNumericDetected = true;
					}
				}
			}
			errorMessage = prepareErrorString(columnValue,
					validatorContext,
					headerKey,
					rowNum,
					columnName,
					invalidNumericDetected,
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
										boolean invalidNumericDetected,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		if(invalidNumericDetected) {
			String columnAt = getColumn(validatorContext);
			errorMessage = headerKey+" at row["+rowNum+"] is not a valid pattern format.";
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
				errorMessage = headerKey+"'s length at row["+rowNum+"],column["+columnAt+"] is not a valid pattern format.";
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
