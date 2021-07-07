
package com.github.salilvnair.excelprocessor.v1.reflect.constant;
public class ExcelHeaderConstant {

	private ExcelHeaderConstant(){}
	
	public static final String ROW_NUM_PLACEHOLDER = "{{rowNum}}";
	public static final String COLUMN_NAME_PLACEHOLDER = "{{columnName}}";
	public static final String TARGET_HEADER_PLACEHOLDER = "{{targetHeader}}";
	public static final String TARGET_HEADER_VALUE_PLACEHOLDER = "{{targetHeaderValue}}";
	public static final String DEPENDENT_HEADERS_PLACEHOLDER = "{{depenendentHeaders}}";
	public static final String PREDEFINED_HEADER_DATA_PLACEHOLDER = "{{predefinedHeaderData}}";
	
	
	public static final String COLUMN_VALUE_NUMERIC_INTEGER = "COLUMN_VALUE_NUMERIC_INTEGER";
	public static final String COLUMN_VALUE_NUMERIC_LONG = "COLUMN_VALUE_NUMERIC_LONG";
	public static final String COLUMN_VALUE_NUMERIC_DOUBLE = "COLUMN_VALUE_NUMERIC_DOUBLE";
	public static final String COLUMN_VALUE_NUMERIC_SHORT = "COLUMN_VALUE_NUMERIC_SHORT";
	public static final String COLUMN_VALUE_NUMERIC_FLOAT = "COLUMN_VALUE_NUMERIC_FLOAT";
	public static final String COLUMN_VALUE_NUMERIC_DATE = "COLUMN_VALUE_NUMERIC_DATE";
	public static final String COLUMN_VALUE_NUMERIC_DEFAULT = "COLUMN_VALUE_NUMERIC_DEFAULT";
	
	public static final String COLUMN_VALUE_CURRENCY_TYPE_DOLLAR = "$";
	
	public static final String USER_DEFINED_MESSAGE_KEY_SPLIT_HOLDER = ":";
	
	public static final String USER_DEFINED_MESSAGE_KEY_REQUIRED = "USER_DEFINED_MESSAGE_KEY_REQUIRED";
	
	public static final String USER_DEFINED_MESSAGE_KEY_UNIQUE = "USER_DEFINED_MESSAGE_KEY_UNIQUE";
	
	public static final String USER_DEFINED_MESSAGE_KEY_MIN_LENGTH = "USER_DEFINED_MESSAGE_KEY_MIN_LENGTH";
	
	public static final String USER_DEFINED_MESSAGE_KEY_MAX_LENGTH = "USER_DEFINED_MESSAGE_KEY_MAX_LENGTH";
	
	public static final String USER_DEFINED_MESSAGE_KEY_LENGTH = "USER_DEFINED_MESSAGE_KEY_LENGTH";
	
	public static final String USER_DEFINED_MESSAGE_KEY_EMAIL = "USER_DEFINED_MESSAGE_KEY_EMAIL";
	
	public static final String USER_DEFINED_MESSAGE_KEY_PATTERN = "USER_DEFINED_MESSAGE_KEY_PATTERN";
	
	
	public static final String WRITE_TO_EXCEL_DATE_FORMAT_M_D_YY = "m/d/yy";
	public static final String WRITE_TO_EXCEL_DATE_FORMAT_D_MMM_YY = "d-mmm-yy";
	public static final String WRITE_TO_EXCEL_TIME_FORMAT_H_MM_SS = "h:mm:ss";
	public static final String WRITE_TO_EXCEL_TIME_FORMAT_H_MM_SS_AM_PM = "h:mm:ss AM/PM";
	 
	public static final short WRITE_TO_EXCEL_DATE_FORMAT_M_D_YY_INDEX = 14;
	public static final short WRITE_TO_EXCEL_DATE_FORMAT_D_MMM_YY_INDEX = 15;
	public static final short WRITE_TO_EXCEL_TIME_FORMAT_H_MM_SS_INDEX = 21;
	public static final short WRITE_TO_EXCEL_TIME_FORMAT_H_MM_SS_AM_PM_INDEX = 19;
	
	public static final String EXCEL_SHEET_INFO_HEADERS = "EXCEL_INFO_MAP_HEADERS";
	public static final String EXCEL_SHEET_INFO_ROWS = "EXCEL_INFO_MAP_HEADERS";
	public static final String EXCEL_SHEET_INFO_CELLS = "EXCEL_INFO_MAP_HEADERS";
	public static final String EXCEL_SHEET_INFO_IS_VERTICAL= "EXCEL_SHEET_INFO_IS_VERTICAL";
	
}
