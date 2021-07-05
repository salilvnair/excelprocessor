package com.github.salilvnair.excelprocessor.reflect.context;

import java.util.List;
import java.util.Map;

public class ExcelValidatorContext {
	private  Map<String,List<String>> predefinedDatasetMap;
	private  Map<String,Object> userValidatorMap;
	private boolean validateInDetail;

	public Map<String,List<String>> getPredefinedDatasetMap() {
		return predefinedDatasetMap;
	}

	public void setPredefinedDatasetMap(Map<String,List<String>> predefinedDatasetMap) {
		this.predefinedDatasetMap = predefinedDatasetMap;
	}

	public Map<String,Object> getUserValidatorMap() {
		return userValidatorMap;
	}

	public void setUserValidatorMap(Map<String,Object> userValidatorMap) {
		this.userValidatorMap = userValidatorMap;
	}

	public boolean validateInDetail() {
		return validateInDetail;
	}

	public void setValidateInDetail(boolean validateInDetail) {
		this.validateInDetail = validateInDetail;
	}
}
