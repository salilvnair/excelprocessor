package com.github.salilvnair.excelprocessor.reflect.context;

import java.util.List;
import java.util.Map;

import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.Predefined;
import com.github.salilvnair.excelprocessor.reflect.service.ICustomValidatorTask;

public class ValidatorContext {

	private ICustomValidatorTask customValidatorTask;
	private int rowNum;
	private String columnName;
	private BaseExcelValidationSheet baseExcelValidationSheet;
	private Map<String,String> fieldNameHeaderNameMap;
	private String jsonKey;
	private String headerKey;
	private Object columnValue;
	private ExcelHeader excelHeader;
	private ExcelHeaderValidator excelHeaderValidator;
	private Predefined predefined;
	private String customTaskMethod;
	private ExcelSheet excelSheet;
	private boolean sheetCustomValidation = false;
	private ExcelValidatorContext excelValidatorContext;
	private Map<String,Object> excelValidationMetaDataMap;
	private String customSheetTask;
	private Map<String,Object> userValidatorMap;
	private List<Object> uniqueValueHolderList;
	
	//getters and setters
	public ICustomValidatorTask getValidatorTask() {
		return customValidatorTask;
	}
	public void setValidatorTask(ICustomValidatorTask customValidatorTask) {
		this.customValidatorTask = customValidatorTask;
	}
	public int getRowNum() {
		return rowNum;
	}
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	public BaseExcelValidationSheet getBaseExcelValidationSheet() {
		return baseExcelValidationSheet;
	}
	public void setBaseExcelValidationSheet(BaseExcelValidationSheet baseExcelValidationSheet) {
		this.baseExcelValidationSheet = baseExcelValidationSheet;
	}
	public Map<String, String> getFieldNameHeaderNameMap() {
		return fieldNameHeaderNameMap;
	}
	public void setFieldNameHeaderNameMap(Map<String, String> fieldNameHeaderNameMap) {
		this.fieldNameHeaderNameMap = fieldNameHeaderNameMap;
	}
	public String getJsonKey() {
		return jsonKey;
	}
	public void setJsonKey(String jsonKey) {
		this.jsonKey = jsonKey;
	}
	public String getHeaderKey() {
		return headerKey;
	}
	public void setHeaderKey(String headerKey) {
		this.headerKey = headerKey;
	}
	public Object getColumnValue() {
		return columnValue;
	}
	public void setColumnValue(Object columnValue) {
		this.columnValue = columnValue;
	}

	public String getCustomTaskMethod() {
		return customTaskMethod;
	}
	public void setCustomTaskMethod(String customTaskMethod) {
		this.customTaskMethod = customTaskMethod;
	}

	public Predefined getPredefined() {
		return predefined;
	}
	public void setPredefined(Predefined predefined) {
		this.predefined = predefined;
	}
	public ExcelSheet getExcelSheet() {
		return excelSheet;
	}
	public void setExcelSheet(ExcelSheet excelSheet) {
		this.excelSheet = excelSheet;
	}
	public ExcelHeaderValidator getExcelHeaderValidator() {
		return excelHeaderValidator;
	}
	public void setExcelHeaderValidator(ExcelHeaderValidator excelHeaderValidator) {
		this.excelHeaderValidator = excelHeaderValidator;
	}
	public boolean isSheetCustomValidation() {
		return sheetCustomValidation;
	}
	public void setSheetCustomValidation(boolean sheetCustomValidation) {
		this.sheetCustomValidation = sheetCustomValidation;
	}
	public ExcelValidatorContext getExcelValidatorContext() {
		return excelValidatorContext;
	}
	public void setExcelValidatorContext(ExcelValidatorContext excelValidatorContext) {
		this.excelValidatorContext = excelValidatorContext;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Map<String,Object> getExcelValidationMetaDataMap() {
		return excelValidationMetaDataMap;
	}
	public void setExcelValidationMetaDataMap(Map<String,Object> excelValidationMetaDataMap) {
		this.excelValidationMetaDataMap = excelValidationMetaDataMap;
	}
	public String getCustomSheetTask() {
		return customSheetTask;
	}
	public void setCustomSheetTask(String customSheetTask) {
		this.customSheetTask = customSheetTask;
	}
	public Map<String,Object> getUserValidatorMap() {
		return userValidatorMap;
	}
	public void setUserValidatorMap(Map<String,Object> userValidatorMap) {
		this.userValidatorMap = userValidatorMap;
	}
	public List<Object> getUniqueValueHolderList() {
		return uniqueValueHolderList;
	}
	public void setUniqueValueHolderList(List<Object> uniqueValueHolderList) {
		this.uniqueValueHolderList = uniqueValueHolderList;
	}
	public ExcelHeader getExcelHeader() {
		return excelHeader;
	}
	public void setExcelHeader(ExcelHeader excelHeader) {
		this.excelHeader = excelHeader;
	}
	
	
	
}
