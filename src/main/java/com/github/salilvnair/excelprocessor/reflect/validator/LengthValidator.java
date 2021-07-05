package com.github.salilvnair.excelprocessor.reflect.validator;

import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;

public class LengthValidator extends BaseExcelValidator {

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
		int minLength = excelHeaderValidator.minLength();
		int maxLength = excelHeaderValidator.maxLength();
		int length = excelHeaderValidator.length();
		boolean minLengthViolated = false;
		boolean maxLengthViolated = false;
		boolean lengthViolated = false;
		if(minLength!=-1 || maxLength!=-1 || length!=-1) {
			if(minLength>0) {
				if(columnValue==null || ExcelValidatorConstant.EMPTY_STRING.equals(columnValue)){
					minLengthViolated = true;
				}
				else if(columnValue instanceof String) {
					String columnStringValue = (String) columnValue;
					if(columnStringValue.length()<minLength) {
						minLengthViolated = true;
					}
				}
				else if(columnValue instanceof Long) {
					Long columnLongValue = (Long) columnValue;
					if(columnLongValue!=null && columnLongValue.toString().length()<minLength) {
						minLengthViolated = true;
					}
				}
				else {					
					if(columnValue!=null && columnValue.toString().length()<minLength) {
						minLengthViolated = true;
					}
				}
			}
			if(maxLength>0) {
				if(columnValue==null || ExcelValidatorConstant.EMPTY_STRING.equals(columnValue)){
					maxLengthViolated = true;
				}
				else if(columnValue instanceof String) {
					String columnStringValue = (String) columnValue;
					if(columnStringValue.length()>maxLength) {
						maxLengthViolated = true;
					}
				}
				else if(columnValue instanceof Long) {
					Long columnLongValue = (Long) columnValue;
					if(columnLongValue!=null && columnLongValue.toString().length()>maxLength) {
						maxLengthViolated = true;
					}
				}
				else {					
					if(columnValue!=null && columnValue.toString().length()>maxLength) {
						maxLengthViolated = true;
					}
				}
			}
			if(length>0) {
				if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
					lengthViolated = true;
				}
				else if(columnValue instanceof String) {
					String columnStringValue = (String) columnValue;
					if(columnStringValue.length()!=length) {
						lengthViolated = true;
					}
				}
				else if(columnValue instanceof Long) {
					Long columnLongValue = (Long) columnValue;
					if(columnLongValue!=null && columnLongValue.toString().length()!=length) {
						lengthViolated = true;
					}
				}
				else {					
					if(columnValue!=null && columnValue.toString().length()!=length) {
						lengthViolated = true;
					}
				}
			}
		}
		errorMessage = prepareErrorString(columnValue,
											validatorContext,
											headerKey,
											rowNum,
											columnName,
											lengthViolated,
											minLengthViolated,
											maxLengthViolated,
											excelHeaderValidator,
											excelSheet);
		return errorMessage;
	}
	private String prepareErrorString(Object columnValue,
										ValidatorContext validatorContext,
										String headerKey,
										int rowNum ,
										String columnName,
										boolean lengthViolated,
										boolean minLengthViolated,
										boolean maxLengthViolated,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		int minLength = excelHeaderValidator.minLength();
		int maxLength = excelHeaderValidator.maxLength();
		int length = excelHeaderValidator.length();
		if(minLengthViolated || maxLengthViolated || lengthViolated) {
			String columnAt = getColumn(validatorContext);
			int lengthData = 0;
			String lengthInfo = "";
			String userDefinedLengthPlaceHolder = "";
			if(minLengthViolated) {
				lengthData = minLength;
				lengthInfo = "cannot be less than";
				userDefinedLengthPlaceHolder = ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_MIN_LENGTH;
			}
			if (maxLengthViolated) {
				lengthData = maxLength;
				lengthInfo = "cannot be greater than";
				userDefinedLengthPlaceHolder = ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_MAX_LENGTH;
			}
			if (lengthViolated) {
				lengthData = length;
				lengthInfo = "should be equal to";
				userDefinedLengthPlaceHolder = ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_LENGTH;
			}
			errorMessage = headerKey+"'s length at row["+rowNum+"] "+lengthInfo+" "+lengthData+".";
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
				errorMessage = headerKey+"'s length at row["+rowNum+"],column["+columnAt+"] "+lengthInfo+" "+lengthData;
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
			else if(excelHeaderValidator.userDefinedMessages().length>0) {
				for(String userDefinedMessageItr : excelHeaderValidator.userDefinedMessages()) {
					if(userDefinedMessageItr.contains(userDefinedLengthPlaceHolder)) {
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
