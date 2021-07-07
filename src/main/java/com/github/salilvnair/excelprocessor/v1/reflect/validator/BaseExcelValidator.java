package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import java.util.Map;
import java.util.Set;

import com.github.salilvnair.excelprocessor.v1.helper.ExcelProcessorUtil;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ExcelValidationMessage;
import org.json.JSONObject;

import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;

public abstract class BaseExcelValidator implements IExcelValidator {

	@Override
	public ExcelValidationMessage validateInDetail(ValidatorContext validatorContext) {
		String message = validate(validatorContext);
		ExcelValidationMessage excelValidationMessage = new ExcelValidationMessage();
		excelValidationMessage.setMessage(message);
		processRowAndColumnDetails(validatorContext, excelValidationMessage);
		excelValidationMessage.setHeader(validatorContext.getHeaderKey());
		excelValidationMessage.setMappedFieldName(validatorContext.getJsonKey());
		return excelValidationMessage;
	}

	@SuppressWarnings("unchecked")
	private void processRowAndColumnDetails(ValidatorContext validatorContext, ExcelValidationMessage excelValidationMessage) {
		ExcelSheet excelSheet = validatorContext.getExcelSheet();
		String jsonKey = validatorContext.getJsonKey();
		int row = validatorContext.getRowNum();
		String col = validatorContext.getColumnName();
		if(excelSheet.isVertical()){
			int rowAt = getRow(validatorContext);
			row = rowAt == -1 ? row : rowAt;
		}
		else {
			String columnAt = getColumn(validatorContext);
			col = ExcelValidatorConstant.EMPTY_STRING .equals(columnAt) ? validatorContext.getColumnName() : columnAt;
		}
		if(excelSheet.verticallyScatteredHeaders()) {
			Map<String, String> headerKeyPositionInfo  = (Map<String, String>) validatorContext.getExcelValidationMetaDataMap().get(ExcelValidatorConstant.EXCEL_HEADER_KEY_POSITION_INFO_MAP);
			String rowUndCol = headerKeyPositionInfo.get(jsonKey);
			String rowIndex = rowUndCol.split("_")[0];
			int rowIndexNum = Integer.parseInt(rowIndex);
			row = rowIndexNum+1;
			String colIndex = rowUndCol.split("_")[1];
			int colIndexNum = Integer.parseInt(colIndex);
			col = ExcelProcessorUtil.toIndentName(colIndexNum+2);
		}
		excelValidationMessage.setRow(row);
		excelValidationMessage.setColumn(col);
	}

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
			return !uploadedExcelHeaders.contains(headerKey);
		}
		return false;
	}
	
}
