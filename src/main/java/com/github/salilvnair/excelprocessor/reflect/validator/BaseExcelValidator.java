package com.github.salilvnair.excelprocessor.reflect.validator;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;

public abstract class BaseExcelValidator implements IExcelValidator {

	public static Integer getRow(ValidatorContext validatorContext) {
		int row = -1;
		if(getRowOrColumn(validatorContext, true)!=null) {
			row = (Integer) getRowOrColumn(validatorContext, true);
		}
		return row;
	}
	
	public static String getColumn(ValidatorContext validatorContext) {
		String column = ExcelValidatorConstant.EMPTY_STRING;
		if(getRowOrColumn(validatorContext, false)!=null) {
			column = (String) getRowOrColumn(validatorContext, false);
		}
		return column;
	}
	
	public static Object getRowOrColumn(ValidatorContext validatorContext,boolean isRow) {
		String jsonKey = validatorContext.getJsonKey();
		try {
			if(validatorContext.getExcelValidationMetaDataMap()!=null) {
				if(isRow) {
					if(validatorContext.getExcelValidationMetaDataMap().containsKey(ExcelValidatorConstant.EXCEL_FIELD_KEY_ROW_VALUE_MAP)) {
						JSONObject jkR = (JSONObject) validatorContext.getExcelValidationMetaDataMap().get(ExcelValidatorConstant.EXCEL_FIELD_KEY_ROW_VALUE_MAP);
						return jkR.get(jsonKey);
					}
				}
				else {
					if(validatorContext.getExcelValidationMetaDataMap().containsKey(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP)) {
						JSONObject jkC = (JSONObject) validatorContext.getExcelValidationMetaDataMap().get(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP);
						return jkC.get(jsonKey);
					}
				}
			}
		}
		catch(Exception ex) {

		}
		return null;
	}
	
	public boolean isUnknownExcelHeader(ValidatorContext validatorContext, String headerKey) {
		Map<String,Object> excelValidationMetaDataMap = validatorContext.getExcelValidationMetaDataMap();
		Set<?> uploadedExcelHeaders = null;
		if(excelValidationMetaDataMap.containsKey(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP)) {
			uploadedExcelHeaders = (Set<?>) excelValidationMetaDataMap.get(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP);
			if(!uploadedExcelHeaders.contains(headerKey)) {
				return true;
			}
		}
		return false;
	}
	
}
