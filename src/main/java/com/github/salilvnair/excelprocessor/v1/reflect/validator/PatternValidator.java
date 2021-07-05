package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;

public class PatternValidator extends BaseExcelValidator {
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
		String pattern = excelHeaderValidator.pattern();
		boolean fieldContainsPattern = false;
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(pattern)){
			fieldContainsPattern = true;
		}
		boolean invalidPatternDetected = false;
		if(fieldContainsPattern) {
			if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
				invalidPatternDetected = true;
			}
			else if(columnValue instanceof String) {
				String columnStringValue = (String) columnValue;
				invalidPatternDetected = !PatternValidator.isValid(pattern, columnStringValue);
			}	
			errorMessage = prepareErrorString(columnValue,
					validatorContext,
					headerKey,
					rowNum,
					columnName,
					invalidPatternDetected,
					excelHeaderValidator,
					excelSheet);
		}
		return errorMessage;
	}
	
	public static boolean isValid(String patternString, String inputString) { 
		Pattern pattern = Pattern.compile(patternString);
		Matcher m = pattern.matcher(inputString);
		return m.matches();
	}
	private String prepareErrorString(Object columnValue,
										ValidatorContext validatorContext,
										String headerKey,
										int rowNum ,
										String columnName,
										boolean invalidEmail,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		if(invalidEmail) {
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
