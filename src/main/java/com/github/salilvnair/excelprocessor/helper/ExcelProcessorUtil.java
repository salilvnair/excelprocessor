package com.github.salilvnair.excelprocessor.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.salilvnair.excelprocessor.bean.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.annotation.Predefined;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelSheetConstant;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ExcelValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.helper.AnnotationUtil;
import com.github.salilvnair.excelprocessor.reflect.helper.DateParsingUtil;
import com.github.salilvnair.excelprocessor.reflect.helper.ExcelDateFormat;
import com.github.salilvnair.excelprocessor.reflect.helper.ReflectionUtil;
import com.github.salilvnair.excelprocessor.reflect.service.AbstractCustomValidatorTask;
import com.github.salilvnair.excelprocessor.reflect.service.ICustomValidatorTask;
import com.github.salilvnair.excelprocessor.reflect.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.reflect.type.PictureType;
import com.github.salilvnair.excelprocessor.reflect.validator.IExcelValidator;
import com.github.salilvnair.excelprocessor.reflect.validator.ValidatorFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <b>ExcelProcessorUtil generates MS-Excel(.xls,.xlsx) file from List of Bean(POJO) values using JSON AND Apache-POI APIs and vice-versa</b>
 * @author <b>Name:</b> Salil V Nair 
 */
public class ExcelProcessorUtil {
	private static Object staticFileCreatorObjectLock = new Object();
	protected final Log logger = LogFactory.getLog(getClass());
	private String firstRowValue="";
	private String lastRowValue="";
	private Map<String,String> customHeader;
	private double rowHeight=0;
	private short formatDateIndex=0;
	private int headerRowNumber=0;
	private String headerColumn=EXCEL_COLUMN_INDENT_START;
	private boolean hasStyleTemplate = false;
	private int copyStyleFromRow = -1;
	private boolean hasExcelTemplate = false;
	private boolean ignoreFormatting = false;
	private boolean wrapTexting = false;
	private File excelTemplate;
	public static final String EXCEL_FILE_TYPE_XLS = ExcelSheetConstant.EXCEL_FILE_TYPE_XLS;
	public static final String EXCEL_FILE_TYPE_XLSX = ExcelSheetConstant.EXCEL_FILE_TYPE_XLSX;
	public static final String APPEND_UNDERSCORE="_";
	public static final String IGNORE_LIST_ITEM="IGNORE_LIST_ITEM";
	private static final String HEADER_KEY_NUMBER = "1";
	private static final String EXCEL_COLUMN_INDENT_START = "A";
	private static final String EXCEL_COLUMN_FIELD_TYPE_INTEGER = "Integer";
	private static final String EXCEL_COLUMN_FIELD_TYPE_LONG = "Long";
	private static final String EXCEL_COLUMN_FIELD_TYPE_DOUBLE = "Double";
	private static final String EXCEL_COLUMN_FIELD_TYPE_BOOLEAN = "Boolean";
	private static final String EXCEL_COLUMN_FIELD_TYPE_STRING = "String";
	private static final String EXCEL_COLUMN_FIELD_TYPE_DATE = "Date";
	private static final String EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING = "String";
	public static final String EXCEL_ERROR_LIST = "excelErrorList";
	private boolean forceAutoSizing;
	private ExcelValidatorContext excelValidatorContext;
	private List<String> ignoreHeaderList;
	private boolean ignoreExcelAnnotation = false;
	private boolean enableHBMGenerator = false;
	private boolean enableTableGenerator = false;
	private List<String> toExcelOrderedFieldNameList;
	private Map<String,List<String>> toExcelOrderedFieldNameMap;
	private Set<String> dynamicFields;
	private Map<String,Set<String>> dynamicFieldMap;
	private Map<String,String> dynamicFieldHeaderMap;
	private boolean copyHeaderStyle = false;
	private List<? extends Object> multiOrientedExcelList;
	
	public class ValidRowPredicate<T> implements Predicate{
	    @Override
	    public boolean evaluate(Object object) {
	       Row row = (Row) object;
	       Cell cell = row.getCell(2);
	       if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
	          return false;
	       } else if (cell.getCellType() == Cell.CELL_TYPE_STRING && 
	                  cell.getStringCellValue().isEmpty()) {
	         return false;
	      }
	      return true;
	    }
	}

	public void uploadExcelOnGivenPath(Workbook workbook,String fileName,String filePath) throws Exception{
		logger.debug("ExcelProcessorUtil>>writeFileOnServer>>begins..");
		File destinationFile = null;
		synchronized (staticFileCreatorObjectLock) {
			destinationFile = new File(new File(filePath), fileName);
		}
		FileOutputStream fout=new FileOutputStream(destinationFile);
		workbook.write(fout);
		fout.close();		
		logger.debug("ExcelProcessorUtil>>writeFileOnServer>>ends..uploaded excel on server "+destinationFile);
	}

	public static Workbook getWorkbook(FileInputStream inputStream, String excelFilePath)
	        throws IOException {
	    Workbook workbook = null;
	    if (excelFilePath.endsWith(EXCEL_FILE_TYPE_XLSX)) {
	        workbook = new XSSFWorkbook(inputStream);
	    } else if (excelFilePath.endsWith(EXCEL_FILE_TYPE_XLS)) {
	        workbook = new HSSFWorkbook(inputStream);
	    } else {
	        throw new IllegalArgumentException("The specified file is not Excel file");
	    }
	    return workbook;
	}

	public static Workbook getWorkbook(File file)
	        throws IOException, InvalidFormatException {
		String excelFilePath = file.getAbsolutePath();
	    Workbook workbook = null;
	    FileInputStream inputS = new FileInputStream(file);
	    if (excelFilePath.endsWith(EXCEL_FILE_TYPE_XLSX)) {
		    OPCPackage opcPackage = OPCPackage.open(excelFilePath);
	        workbook = new XSSFWorkbook(opcPackage);
	    } else if (excelFilePath.endsWith(EXCEL_FILE_TYPE_XLS)) {
	        workbook = new HSSFWorkbook(inputS);
	    }
	    return workbook;
	}

	private  static Workbook setWorkbook(String excelFileType)
	        throws IOException {
	    Workbook workbook = null;
	    if (excelFileType.endsWith(EXCEL_FILE_TYPE_XLSX)) {
	        workbook = new XSSFWorkbook();
	    } else if (excelFileType.endsWith(EXCEL_FILE_TYPE_XLS)) {
	        workbook = new HSSFWorkbook();
	    } else {
	        throw new IllegalArgumentException("The specified file is not Excel file");
	    }
	    return workbook;
	}

	public HashMap<String,Object> excelSheetInfo(File file, Class<? extends BaseExcelSheet> excelHeaderBeanClass) throws InstantiationException, IllegalAccessException{
		logger.debug("ExcelProcessorUtil>>excelInfo>>begins");
		HashMap<String, Object> excelSheetInfoMap = new HashMap<String,Object>();
		FileInputStream inputS;
		Workbook workbook=null;
		List<String> headersList=new ArrayList<String>();
		try {
			inputS = new FileInputStream(file);
			workbook = getWorkbook(inputS,file.getAbsolutePath());
		} catch (FileNotFoundException fnfex) {
			logger.error("ExcelProcessorUtil>>excelInfo>>FileNotFoundException:"+fnfex);
		} catch (IOException ioex) {
			logger.error("ExcelProcessorUtil>>excelInfo>>IOException:"+ioex);
		}
		BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
		processSheetAnnotation(baseExcelSheet);
		ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
		Sheet sheet  = workbook.getSheet(excelSheet.value());
		int headerRowNum=0;
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		int rowCounter=sheet.getPhysicalNumberOfRows();
		int cellCounter=sheet.getRow(headerRowNum).getPhysicalNumberOfCells();
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		String jsonKey = "";
		if(excelSheet.isVertical()) {
			for(int i=0;i<rowCounter;i++){
				int columnHeader = toIndentNumber(this.headerColumn)  - 1;
				if (sheet.getRow(i)==null){
					continue;
				}
				if (sheet.getRow(i).getCell(columnHeader) == null ||
						ExcelValidatorConstant.EMPTY_STRING.equals(sheet.getRow(i).getCell(columnHeader).getStringCellValue())
					) {
					continue;
				}
				
				jsonKey=sheet.getRow(i).getCell(columnHeader).getStringCellValue();
				headersList.add(jsonKey);
			}
		}
		else {
			for(int i=0;i<cellCounter;i++){
				if(sheet.getRow(headerRowNum)==null) {
					continue;
				}
				if(sheet.getRow(headerRowNum).getCell(i)==null
						|| ExcelValidatorConstant.EMPTY_STRING.equals(sheet.getRow(headerRowNum).getCell(i).getStringCellValue())) {
					continue;
				}
				jsonKey=sheet.getRow(headerRowNum).getCell(i).getStringCellValue();
				headersList.add(jsonKey);
			}
		}
		String uptoColumn = toIndentName(cellCounter);
		excelSheetInfoMap.put(ExcelHeaderConstant.EXCEL_SHEET_INFO_ROWS,rowCounter);
		excelSheetInfoMap.put(ExcelHeaderConstant.EXCEL_SHEET_INFO_CELLS,cellCounter+"("+uptoColumn+")");
		excelSheetInfoMap.put(ExcelHeaderConstant.EXCEL_SHEET_INFO_HEADERS,headersList);
		excelSheetInfoMap.put(ExcelHeaderConstant.EXCEL_SHEET_INFO_IS_VERTICAL,excelSheet.isVertical());
		logger.debug("ExcelProcessorUtil>>excelInfo>>ends>>excelSheetInfoMap:"+excelSheetInfoMap);
		return excelSheetInfoMap;
	}
	
	public HashMap<String, HashMap<String, Object>> excelInfo(File file, Map<String,Class<? extends BaseExcelSheet>> excelHeaderBeanMap) throws InstantiationException, IllegalAccessException{
		logger.debug("ExcelProcessorUtil>>excelInfo>>begins");
		HashMap<String, HashMap<String, Object>> excelInfoMap = new HashMap<>();
		FileInputStream inputS;
		Workbook workbook=null;
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		try {
			inputS = new FileInputStream(file);
			workbook = getWorkbook(inputS,file.getAbsolutePath());
		} catch (FileNotFoundException fnfex) {
			logger.error("ExcelProcessorUtil>>excelInfo>>FileNotFoundException:"+fnfex);
		} catch (IOException ioex) {
			logger.error("ExcelProcessorUtil>>excelInfo>>IOException:"+ioex);
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}			
			if(excelHeaderBeanMap.containsKey(sheetName.trim())){
				excelHeaderBeanClass=excelHeaderBeanMap.get(sheetName.trim());
			}
			else {
				continue;
			}
			excelInfoMap.put(sheetName, excelSheetInfo(file, excelHeaderBeanClass));
		}
		return excelInfoMap;
	}

	public Workbook toExcel(List<? extends Object> toExcelList, String excelFileType, String sheetName)
			throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.debug("ExcelProcessorUtil>>toExcel>>begins..");
		Workbook workbook = setWorkbook(excelFileType);
		if(toExcelList==null || toExcelList.size()<=0){
			logger.debug("ExcelProcessorUtil>>toExcel>>ends as the toExcelList doesn't have any data to write on excel");
			return workbook;
		}
		CellStyle cellStyle=workbook.createCellStyle();
		Sheet sheet = workbook.createSheet(sheetName);		
		List<Object[]> valObjList = new ArrayList<Object[]>();
		String className = toExcelList.get(0).getClass().getName();
		ClassLoader classLoader = ExcelProcessorUtil.class.getClassLoader();
		Class<?> clazz = classLoader.loadClass(className);
		List<String> fieldNameList = new ArrayList<String>();
		Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
		Object object = clazz.newInstance();
		//commenting below to get only the fields which are annotated with ExcelHeader
		//making ExcelHeader mandatory for this utility to recognize the header name to be exported
		//Set<Field> fields = ReflectionUtil.getFields(clazz);
		Set<Field> fields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
		for (Field field : fields) {
			fieldNameList.add(field.getName());
			prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
		}
		fieldNameList = prepareOrderedFieldNameList(fieldNameList,sheetName,this.customHeader);
		Object[] headobjArr = new Object[fieldNameList.size()];
		for(int f=0;f<fieldNameList.size();f++){
			headobjArr[f] = fieldNameList.get(f);
		}
		for (int k = 0; k < toExcelList.size(); k++) {
			Object[] valueobjectArray = new Object[fieldNameList.size()];
			object = toExcelList.get(k);
			Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();
			String jsonString = gson.toJson(object);
			JSONObject jasonObject = new JSONObject(jsonString);
			int keyValCounter = 0;
			for (String key : fieldNameList) {
				Object hasValue=jasonObject.opt(key);
				if(hasValue!=null){
					Object jasonValue = jasonObject.get(key);
					valueobjectArray[keyValCounter] = jasonValue;
				}
				keyValCounter++;
			}
			valObjList.add(valueobjectArray);
		}
		Map<String, Object> excelData = new TreeMap<String, Object>();
		int headerRowNum=0;
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		excelData.put(HEADER_KEY_NUMBER, headobjArr);
		logger.debug("ExcelProcessorUtil>>toExcel>>Headers Count:"+fieldNameList.size());
		logger.debug("ExcelProcessorUtil>>toExcel>>Rows Count:"+toExcelList.size());
		for (int i = 0; i < toExcelList.size(); i++) {
			Integer k = i + 2;
			Object[] excelvalobjArr = new Object[valObjList.size()];
			excelvalobjArr = valObjList.get(i);
			excelData.put(k.toString(), excelvalobjArr);
		}
		Set<String> excelDataKeyset = excelData.keySet();
		int rownum = 0;
		if(hasExcelTemplate) {
			workbook = getWorkbookTemplate(workbook);
			sheet = workbook.getSheet(sheetName);
		}
		for (String key : excelDataKeyset) {
			Row row = null;
			if(hasExcelTemplate) {
				while(headerRowNum==rownum){
					rownum++;
				}
			}
			else {
				row = sheet.createRow(rownum++);
			}
			Object[] valObjArray = (Object[]) excelData.get(key);
			int cellnum = 0;
			for (Object valObj : valObjArray) {
				Cell cell = row.createCell(cellnum++);
			    if(hasStyleTemplate) {
				    copySheetStyle(workbook,sheetName,sheet,cell,rownum-1,cellnum-1);
			    }
				if (valObj instanceof String)
					try{ 
						Long longValObj=Long.parseLong((String)valObj); 
						cell.setCellValue((Long)longValObj);
					}
					catch(Exception e){
						cell.setCellValue((String) valObj);
					}
				else if (valObj instanceof Integer){
					cell.setCellValue((Integer) valObj);
				}
				else if (valObj instanceof Long){
					cell.setCellValue((Long) valObj);
				}	
				else if (valObj instanceof Double){
					cell.setCellValue((Double) valObj);
				}
				else if (valObj instanceof Character){
					cell.setCellValue((Character) valObj);
				}
				else if (valObj instanceof Boolean){
					cell.setCellValue((Boolean) valObj);
				}
				else if(valObj instanceof java.sql.Timestamp||valObj instanceof java.sql.Date||valObj instanceof java.util.Date){
					cell.setCellValue((Date) valObj);
					if(getFormatDateIndex()!=0){
						cellStyle.setDataFormat(getFormatDateIndex());
					}
					else{
						cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
					}
					cell.setCellStyle(cellStyle);
				}
			}
		}
		logger.debug("ExcelProcessorUtil>>toExcel>>ends..");
		if(!ignoreFormatting) {
			Row headerRow=sheet.getRow(0);
			if(!hasExcelTemplate) {
				makeRowBold(workbook,headerRow);
			}
			if(forceAutoSizing){
				autoSizeColumn(headerRow,sheet);
			}
		}
		return workbook;
	}
	
	private List<String> prepareOrderedFieldNameList(List<String> fieldNameList, String sheetName, Map<String, String> headerFieldMap) {
		if(toExcelOrderedFieldNameMap!=null && 
				!toExcelOrderedFieldNameMap.isEmpty() && toExcelOrderedFieldNameMap.containsKey(sheetName)) {
			toExcelOrderedFieldNameList = toExcelOrderedFieldNameMap.get(sheetName);
		}
		if(toExcelOrderedFieldNameList!=null && !toExcelOrderedFieldNameList.isEmpty()) {
			List<String> orderedFieldNameList = new ArrayList<>();
			for(String fieldName:toExcelOrderedFieldNameList) {
				if(headerFieldMap.containsKey(fieldName)) {
					orderedFieldNameList.add(fieldName);
				}
			}
			return orderedFieldNameList;
		}
		return fieldNameList;
	}

	@SuppressWarnings("unchecked")
	public  List<? extends Object> fromExcelBeanList(File excelfile,Class<? extends BaseExcelSheet> excelHeaderBeanClass,
													 boolean hasCustomHeader) 
				throws IOException, JSONException,NoSuchFieldException, SecurityException,
				ClassNotFoundException, InstantiationException, IllegalAccessException, InvalidFormatException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList>>begins..");
		int headerRowNum=0;
		List<BaseExcelSheet> excelHeaderBeanList=new ArrayList<BaseExcelSheet>();
		Class<?> clazz = excelHeaderBeanClass;
		Map<String,Object> fieldArgumentMap = new HashMap<>();
		Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
		//commenting below to get only the fields which are annotated with ExcelHeader
		//making ExcelHeader mandatory for this utility to recognize the header name
		//Field[] fieldArray = clazz.getDeclaredFields();
		Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
		for (Field field : excelHeaderFields) {
			fieldArgumentMap.put(field.getName(), field.getType().getName());
			prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
		}
		FileInputStream inputS = new FileInputStream(excelfile);
		Workbook workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
		processSheetAnnotation(baseExcelSheet);
		ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
		JSONObject jsonKV = new JSONObject();
		JSONObject jsonKColumns = new JSONObject();
		boolean pivotEnabled = false;
		Sheet firstSheet  = null;
		if(excelSheet!=null) {
			pivotEnabled = excelSheet.isVertical();
			firstSheet =  workbook.getSheet(excelSheet.value());
		}
		else {
			firstSheet =  workbook.getSheetAt(0);
		}
		if(pivotEnabled) {
			Map<String, Class<? extends BaseExcelSheet>> excelHeaderBeanMap = new HashMap<>();
			excelHeaderBeanMap.put(excelSheet.value(), baseExcelSheet.getClass());
			Map<String, List<? extends Object>> sheetBeanMap = fromExcelVerticalSheetBeanMap(excelfile, workbook, excelHeaderBeanMap, hasCustomHeader, pivotEnabled);
			excelHeaderBeanList = (List<BaseExcelSheet>) sheetBeanMap.get(excelSheet.value());
		}
		else {
			firstSheet = removeTrailingEmptyRowsFromSheet(firstSheet);
			int rowCounter=firstSheet.getPhysicalNumberOfRows();
			if(excelSheet!=null && excelSheet.valueRowEndsAt()!=-1) {
				if(rowCounter>excelSheet.valueRowEndsAt()) {
					rowCounter = excelSheet.valueRowEndsAt();
				}
			}
			if(getHeaderRowNumber()!=0){
				headerRowNum=getHeaderRowNumber()-1;
			}
			int cellCounter=firstSheet.getRow(headerRowNum).getPhysicalNumberOfCells();
			int valueRowNum=headerRowNum+1;
			Set<String> uploadedExcelHeaders = new HashSet<>();
			for(int j=valueRowNum;j<rowCounter;j++){
				for(int i=0;i<cellCounter;i++){
					if(firstSheet.getRow(headerRowNum)==null) {
						continue;
					}
					if(
							firstSheet.getRow(headerRowNum).getCell(i)==null ||
							ExcelValidatorConstant.EMPTY_STRING.equals(firstSheet.getRow(headerRowNum).getCell(i).getStringCellValue())
					 ) {
						continue;
					}
					Integer jsonIntegerValue=null;
					Long jsonLongValue=null;
					Double jsonDoubleValue=null;
					String jsonStringValue=null;				
					Boolean jsonBooleanValue=null;
					Date jsonDateValue=null;
						Cell cellValue=firstSheet.getRow(j).getCell(i);
						if(cellValue!=null){
						switch (cellValue.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								jsonStringValue=cellValue.getStringCellValue();
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								jsonBooleanValue=cellValue.getBooleanCellValue();
								jsonStringValue = jsonBooleanValue.toString();
								break;
							case Cell.CELL_TYPE_NUMERIC:
								jsonDoubleValue=cellValue.getNumericCellValue();
								jsonLongValue=new BigDecimal(cellValue.getNumericCellValue()).longValue();
								jsonIntegerValue=new BigDecimal(cellValue.getNumericCellValue()).intValue();
								jsonDateValue=cellValue.getDateCellValue();
								jsonStringValue = jsonDoubleValue.toString();
								break;					
							// for Formula
							case Cell.CELL_TYPE_FORMULA:
								FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
								CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
								switch (cellValue.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
									jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
									jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
									jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
									jsonStringValue = jsonDoubleValue.toString();
									break;
								case Cell.CELL_TYPE_STRING:							
									jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
									break;
								}
								break;
						 }
					  }
					String jsonKey=firstSheet.getRow(headerRowNum).getCell(i).getStringCellValue();
					jsonKey = cleanJsonKey(jsonKey);
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
							continue;
					}
					if(firstSheet.getRow(headerRowNum)==null) {
						continue;
					}
					if(firstSheet.getRow(headerRowNum).getCell(i)==null) {
						continue;
					}
					if(jsonKey==null || ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)) {
						continue;
					}
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;						
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,i,j);
						uploadedExcelHeaders.add(modifiedJsonKey);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
						uploadedExcelHeaders.add(jsonKey);
					}					
					if(fieldArgumentMap.containsKey(jsonKey.trim())){
						String fullySpecfiedfieldTypeName=(String) fieldArgumentMap.get(jsonKey.trim());
						boolean isWrapper=fullySpecfiedfieldTypeName.contains("java.lang");
						if(!isWrapper){
							logger.debug("ExcelProcessorUtil>>fromExcelExcelList>>one of the beans field is not of type Wrapper class!.");
						}
						String[] fullySpecfiedfieldTypeNameArray=Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
						String fieldType=fullySpecfiedfieldTypeNameArray[2];
						if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)){
							ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
							jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
							if(jsonStringValue!=null) {
								jsonStringValue = advancedTrim(jsonStringValue);
							}
							jsonKV.put(jsonKey, jsonStringValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)){
							jsonKV.put(jsonKey, jsonLongValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)){
							jsonKV.put(jsonKey, jsonDoubleValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)){
							jsonKV.put(jsonKey, jsonBooleanValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)){
							jsonKV.put(jsonKey, jsonIntegerValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)){
							if(jsonDateValue==null) {
								if(jsonStringValue!=null) {
									ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
									if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
										jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
									}
								}
							}
							jsonKV.put(jsonKey, jsonDateValue);
						}
						String columnAt = toIndentName(i+1);
						jsonKColumns.put(jsonKey, columnAt);
					}
				}
				Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();			
				baseExcelSheet= gson.fromJson(jsonKV.toString(), baseExcelSheet.getClass());
				excelHeaderBeanList.add(baseExcelSheet);
			}
			Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP, jsonKColumns);
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);
			processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
		}
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList>>ends..");
		return excelHeaderBeanList;
	}
	
	private Sheet removeTrailingEmptyRowsFromSheet(Sheet sheet) {
		boolean stop = false;
        boolean nonBlankRowFound;
        short c;
        Row lastRow = null;
        Cell cell = null;

        while (stop == false) {
            nonBlankRowFound = false;
            lastRow = sheet.getRow(sheet.getLastRowNum());
            if(lastRow!=null) {
                for (c = lastRow.getFirstCellNum(); c <= lastRow.getLastCellNum(); c++) {
                    cell = lastRow.getCell(c);
                    if (cell != null && lastRow.getCell(c).getCellType() != Cell.CELL_TYPE_BLANK
                    		&& !ExcelValidatorConstant.EMPTY_STRING.equals(cell.toString())) {
                        nonBlankRowFound = true;
                    }
                }
                if (nonBlankRowFound == true) {
                    stop = true;
                } 
                else {
                    sheet.removeRow(lastRow);
                }
            }
        }
		return sheet;
	}

	public  Map<String,List<? extends Object>> fromExcelBeanMap(File excelfile,
			Workbook workbook,Map<String,Class<? extends BaseExcelSheet>> excelHeaderBeanMap,
			boolean hasCustomHeader) throws IOException, JSONException, NoSuchFieldException,
			SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>begins..");
		int headerRowNum=0;
		Map<String,List<? extends Object>> fromExcelMultiSheetMap=new HashMap<String,List<? extends Object>>();
		Map<String,List<String>> sheetErrorListMap=new HashMap<>();
		List<Map<String,List<String>>> sheetErrorMapList = new ArrayList<>();
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		if(workbook==null) {
			FileInputStream inputS = new FileInputStream(excelfile);
			workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}			
			if(excelHeaderBeanMap.containsKey(sheetName.trim())){
				excelHeaderBeanClass=excelHeaderBeanMap.get(sheetName.trim());
			}
			else {
				continue;
			}
			currentSheet = removeTrailingEmptyRowsFromSheet(currentSheet);
			List<String> errorList = new ArrayList<>();
			Class<?> clazz = excelHeaderBeanClass;
			BaseExcelSheet baseExcelSheet=excelHeaderBeanClass.newInstance();
			processSheetAnnotation(baseExcelSheet);
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			if(getHeaderRowNumber()!=0){
				headerRowNum=getHeaderRowNumber()-1;
			}
			int valueRowNum=headerRowNum+1;
			Map<String,Object> fieldArgumentMap = new HashMap<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				fieldArgumentMap.put(field.getName(), field.getType().getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			List<BaseExcelSheet> excelHeaderBeanList=new ArrayList<BaseExcelSheet>();
			JSONObject jsonKV = new JSONObject();
			JSONObject jsonKColumns = new JSONObject();
			int rowCounter=currentSheet.getPhysicalNumberOfRows();
			if(excelSheet!=null && excelSheet.valueRowEndsAt()!=-1) {
				if(rowCounter>excelSheet.valueRowEndsAt()) {
					rowCounter = excelSheet.valueRowEndsAt();
				}
			}
			int cellCounter=currentSheet.getRow(headerRowNum).getPhysicalNumberOfCells();
			Set<String> uploadedExcelHeaders = new HashSet<>();
			for(int j=valueRowNum;j<rowCounter;j++){
				for(int i=0;i<cellCounter;i++){
					if(currentSheet.getRow(headerRowNum)==null) {
						continue;
					}
					if(
							currentSheet.getRow(headerRowNum).getCell(i)==null ||
							ExcelValidatorConstant.EMPTY_STRING.equals(currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue())
					  ) {
						continue;
					}
					String jsonKey=currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue();
					jsonKey = cleanJsonKey(jsonKey);
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
						continue;
					}
					Integer jsonIntegerValue=null;
					Long jsonLongValue=null;
					Double jsonDoubleValue=null;
					String jsonStringValue=null;				
					Boolean jsonBooleanValue=null;
					Date jsonDateValue=null;
						Cell cellValue=currentSheet.getRow(j).getCell(i);
						if(cellValue!=null){
						switch (cellValue.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								jsonStringValue=cellValue.getStringCellValue();
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								jsonBooleanValue=cellValue.getBooleanCellValue();
								jsonStringValue = jsonBooleanValue.toString();
								break;
							case Cell.CELL_TYPE_NUMERIC:
								jsonDoubleValue=cellValue.getNumericCellValue();
								jsonLongValue=new BigDecimal(cellValue.getNumericCellValue()).longValue();
								jsonIntegerValue=new BigDecimal(cellValue.getNumericCellValue()).intValue();
								jsonDateValue=cellValue.getDateCellValue();
								jsonStringValue = jsonDoubleValue.toString();
								break;					
							// for Formula
							case Cell.CELL_TYPE_FORMULA:
								FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
								CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
								switch (cellValue.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
									jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
									jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
									jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
									jsonStringValue = jsonDoubleValue.toString();
									break;
								case Cell.CELL_TYPE_STRING:							
									jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
									break;
								}
								break;
						 }
					  }

					jsonKey = cleanJsonKey(jsonKey);
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,i,j);
						uploadedExcelHeaders.add(modifiedJsonKey);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
						uploadedExcelHeaders.add(jsonKey);
					}					
					if(fieldArgumentMap.containsKey(jsonKey.trim())){
						String fullySpecfiedfieldTypeName=(String) fieldArgumentMap.get(jsonKey.trim());
						boolean isWrapper=fullySpecfiedfieldTypeName.contains("java.lang");
						if(!isWrapper){
							logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>one of the beans field is not of type Wrapper class!.");
						}
						String[] fullySpecfiedfieldTypeNameArray=Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
						String fieldType=fullySpecfiedfieldTypeNameArray[2];
						if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)){
							ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
							jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
							if(jsonStringValue!=null) {
								jsonStringValue = advancedTrim(jsonStringValue);
							}
							jsonKV.put(jsonKey, jsonStringValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)){
							jsonKV.put(jsonKey, jsonLongValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)){
							jsonKV.put(jsonKey, jsonDoubleValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)){
							jsonKV.put(jsonKey, jsonBooleanValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)){
							jsonKV.put(jsonKey, jsonIntegerValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)){
							if(jsonDateValue==null) {
								if(jsonStringValue!=null) {
									ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
									if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
										jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
									}
								}
							}
							jsonKV.put(jsonKey, jsonDateValue);
						}
						String columnAt = toIndentName(i+1);
						jsonKColumns.put(jsonKey, columnAt);
					}
				}
				jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_EXCEL_ROW_INDEX, j);
				jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_ROW_NUM, (j+1));
				Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();
				baseExcelSheet= gson.fromJson(jsonKV.toString(), baseExcelSheet.getClass());
				excelHeaderBeanList.add(baseExcelSheet);
			}
			Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP, jsonKColumns);
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);
			List<String> processedErrorList = processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
			if(!processedErrorList.isEmpty()){	
				errorList.addAll(processedErrorList);
				//add the error list to the fromExcelMultiSheetMap
				sheetErrorListMap.put(sheetName, errorList);
			}			
			fromExcelMultiSheetMap.put(sheetName, excelHeaderBeanList);
		}
		sheetErrorMapList.add(sheetErrorListMap);
		fromExcelMultiSheetMap.put(EXCEL_ERROR_LIST, sheetErrorMapList);
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>ends..");
		return fromExcelMultiSheetMap;
	}

	public  Map<String,Map<String,List<? extends Object>>> fromExcelBeanMap(File excelfile,
			Workbook workbook,Integer fromRow,Integer toRow,Map<String,
			Class<? extends BaseExcelSheet>> excelHeaderBeanMap,
			boolean hasCustomHeader) throws IOException, JSONException, NoSuchFieldException,
			SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>begins>>fromRow:"+fromRow+" toRow:"+toRow);
		int headerRowNum=0;
		Map<String,List<? extends Object>> fromExcelMultiSheetMap=new HashMap<>();
		Map<String,List<String>> sheetErrorListMap=new HashMap<>();
		List<Map<String,List<String>>> sheetErrorMapList = new ArrayList<>();
		Map<String,Map<String,List<? extends Object>>> fromExcelMultiSheetMapCollection=new HashMap<>();		
		if(workbook==null) {
			FileInputStream inputS = new FileInputStream(excelfile);
			workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}

			Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
			if(excelHeaderBeanMap.containsKey(sheetName.trim())){
				excelHeaderBeanClass=excelHeaderBeanMap.get(sheetName.trim());
			}
			else {
				continue;
			}
			currentSheet = removeTrailingEmptyRowsFromSheet(currentSheet);
			List<String> errorList = new ArrayList<>();
			Class<?> clazz = excelHeaderBeanClass;
			BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
			processSheetAnnotation(baseExcelSheet);
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			if(getHeaderRowNumber()!=0){
				headerRowNum=getHeaderRowNumber()-1;
			}
			int valueRowNum=headerRowNum+1;
			Map<String,Object> fieldArgumentMap = new HashMap<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				fieldArgumentMap.put(field.getName(), field.getType().getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			List<BaseExcelSheet> excelHeaderBeanList=new ArrayList<>();			
			JSONObject jsonKV = new JSONObject();
			JSONObject jsonKColumns = new JSONObject();
			int rowCounter=currentSheet.getPhysicalNumberOfRows();
			if(excelSheet!=null && excelSheet.valueRowEndsAt()!=-1) {
				if(rowCounter>excelSheet.valueRowEndsAt()) {
					rowCounter = excelSheet.valueRowEndsAt();
				}
			}
			int cellCounter=currentSheet.getRow(headerRowNum).getPhysicalNumberOfCells();
			Set<String> uploadedExcelHeaders = new HashSet<>();
			for(int j=valueRowNum;j<rowCounter;j++){
				for(int i=0;i<cellCounter;i++){
					if(currentSheet.getRow(headerRowNum)==null) {
						continue;
					}
					if(
							currentSheet.getRow(headerRowNum).getCell(i)==null ||
							ExcelValidatorConstant.EMPTY_STRING.equals(currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue())
					  ) {
						continue;
					}
					String jsonKey=currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue();
					jsonKey = cleanJsonKey(jsonKey);
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
						continue;
					}
					Integer jsonIntegerValue=null;
					Long jsonLongValue=null;
					Double jsonDoubleValue=null;
					String jsonStringValue=null;				
					Boolean jsonBooleanValue=null;
					Date jsonDateValue=null;
						Cell cellValue=currentSheet.getRow(j).getCell(i);
						if(cellValue!=null){
						switch (cellValue.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								jsonStringValue=cellValue.getStringCellValue();
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								jsonBooleanValue=cellValue.getBooleanCellValue();
								jsonStringValue = jsonBooleanValue.toString();
								break;
							case Cell.CELL_TYPE_NUMERIC:
								jsonDoubleValue=cellValue.getNumericCellValue();
								jsonLongValue=new BigDecimal(cellValue.getNumericCellValue()).longValue();
								jsonIntegerValue=new BigDecimal(cellValue.getNumericCellValue()).intValue();
								jsonDateValue=cellValue.getDateCellValue();
								jsonStringValue = jsonDoubleValue.toString();
								break;					
							// for Formula
							case Cell.CELL_TYPE_FORMULA:
								FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
								CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
								switch (cellValue.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
									jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
									jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
									jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
									jsonStringValue = jsonDoubleValue.toString();
									break;
								case Cell.CELL_TYPE_STRING:							
									jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
									break;
								}
								break;
						 }
					  }
					jsonKey = cleanJsonKey(jsonKey);
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,i,j);
						uploadedExcelHeaders.add(modifiedJsonKey);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
						uploadedExcelHeaders.add(jsonKey);
					}					
					if(fieldArgumentMap.containsKey(jsonKey.trim())){
						String fullySpecfiedfieldTypeName=(String) fieldArgumentMap.get(jsonKey.trim());
						boolean isWrapper=fullySpecfiedfieldTypeName.contains("java.lang");
						if(!isWrapper){
							logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>one of the beans field is not of type Wrapper class!.");
						}
						String[] fullySpecfiedfieldTypeNameArray=Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
						String fieldType=fullySpecfiedfieldTypeNameArray[2];
						if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)){
							ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
							jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
							if(jsonStringValue!=null) {
								jsonStringValue = advancedTrim(jsonStringValue);
							}
							jsonKV.put(jsonKey, jsonStringValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)){
							jsonKV.put(jsonKey, jsonLongValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)){
							jsonKV.put(jsonKey, jsonDoubleValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)){
							jsonKV.put(jsonKey, jsonBooleanValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)){
							jsonKV.put(jsonKey, jsonIntegerValue);
						}
						else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)){
							if(jsonDateValue==null) {
								if(jsonStringValue!=null) {
									ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
									if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
										jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
									}
								}
							}
							jsonKV.put(jsonKey, jsonDateValue);
						}						
					}
				}				
				jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_EXCEL_ROW_INDEX, j);
				jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_ROW_NUM, (j+1));
				Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();				
				baseExcelSheet = gson.fromJson(jsonKV.toString(), excelHeaderBeanClass);
				excelHeaderBeanList.add(baseExcelSheet);
			}
			Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP, jsonKColumns);
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);
			List<String> processedErrorList = processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
			if(!processedErrorList.isEmpty()){	
				errorList.addAll(processedErrorList);
				//add the error list to the fromExcelMultiSheetMap
				sheetErrorListMap.put(sheetName, errorList);
			}		
			fromExcelMultiSheetMap.put(sheetName, excelHeaderBeanList);
		}
		sheetErrorMapList.add(sheetErrorListMap);
		fromExcelMultiSheetMap.put(EXCEL_ERROR_LIST, sheetErrorMapList);
		fromExcelMultiSheetMapCollection.put(fromRow+"-"+toRow, fromExcelMultiSheetMap);
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets)>>ends>>fromRow:"+fromRow+" toRow:"+toRow);
		
		return fromExcelMultiSheetMapCollection;
	}
	
	public  Map<String,List<? extends Object>> fromExcelBeanMap(File excelfile,Map<String,Class<? extends BaseExcelSheet>> excelHeaderBeanMap,boolean hasCustomHeader,boolean hasInfoRowFirst,boolean hasInfoRowLast) throws IOException, JSONException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets&&InfoRow)>>begins..");
		Map<String,List<? extends Object>> fromExcelMultiSheetMap=new HashMap<String,List<? extends Object>>();
		Map<String,List<String>> sheetErrorListMap=new HashMap<>();
		List<Map<String,List<String>>> sheetErrorMapList = new ArrayList<>();		
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		FileInputStream inputS = new FileInputStream(excelfile);
		Workbook workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			List<String> errorList = new ArrayList<>();
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}
			if(excelHeaderBeanMap.containsKey(sheetName.trim())){
				excelHeaderBeanClass=excelHeaderBeanMap.get(sheetName.trim());
			}
			else {
				continue;
			}
			currentSheet = removeTrailingEmptyRowsFromSheet(currentSheet);
			Class<?> clazz = excelHeaderBeanClass;
			Map<String,Object> fieldArgumentMap = new HashMap<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				fieldArgumentMap.put(field.getName(), field.getType().getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			List<BaseExcelSheet> excelHeaderBeanList=new ArrayList<BaseExcelSheet>();			
			JSONObject jsonKV = new JSONObject();
			JSONObject jsonKColumns = new JSONObject();
			int rowCounter=currentSheet.getPhysicalNumberOfRows();
			Set<String> uploadedExcelHeaders = new HashSet<>();
			int headerRow=0;			
			if(hasInfoRowFirst){
				headerRow=1;
			}
			int cellCounter=currentSheet.getRow(headerRow).getPhysicalNumberOfCells();
			int j=1;
			if(hasInfoRowFirst){
				Cell cellValue=currentSheet.getRow(0).getCell(0);
				String infoRowValue=cellValue.getStringCellValue();
				jsonKV.put("infoRowFirst", infoRowValue);
				j=2;
			}
			if(hasInfoRowLast){
				Cell cellValue=currentSheet.getRow(rowCounter-1).getCell(0);
				String infoRowValue=cellValue.getStringCellValue();
				jsonKV.put("infoRowLast", infoRowValue);
			}
			for(;j<rowCounter;j++){
				for(int i=0;i<cellCounter;i++){
					if(currentSheet.getRow(headerRow)==null) {
						continue;
					}
					if(
							currentSheet.getRow(headerRow).getCell(i)==null ||
							ExcelValidatorConstant.EMPTY_STRING.equals(currentSheet.getRow(headerRow).getCell(i).getStringCellValue())
					  ) {
						continue;
					}
					String jsonKey=currentSheet.getRow(headerRow).getCell(i).getStringCellValue();
					jsonKey = cleanJsonKey(jsonKey);
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
						continue;
					}
					Integer jsonIntegerValue=null;
					Long jsonLongValue=null;
					Double jsonDoubleValue=null;
					String jsonStringValue=null;				
					Boolean jsonBooleanValue=null;
					Date jsonDateValue=null;
						Cell cellValue=currentSheet.getRow(j).getCell(i);
						if(cellValue!=null){
						switch (cellValue.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								jsonStringValue=cellValue.getStringCellValue();
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								jsonBooleanValue=cellValue.getBooleanCellValue();
								jsonStringValue = jsonBooleanValue.toString();
								break;
							case Cell.CELL_TYPE_NUMERIC:
								jsonDoubleValue=cellValue.getNumericCellValue();
								jsonLongValue=new BigDecimal(cellValue.getNumericCellValue()).longValue();
								jsonIntegerValue=new BigDecimal(cellValue.getNumericCellValue()).intValue();
								jsonDateValue=cellValue.getDateCellValue();
								jsonStringValue = jsonDoubleValue.toString();
								break;					
							// for Formula
							case Cell.CELL_TYPE_FORMULA:
								FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
								CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
								switch (cellValue.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
									jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
									jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
									jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
									jsonStringValue = jsonDoubleValue.toString();
									break;
								case Cell.CELL_TYPE_STRING:							
									jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
									break;
								}
								break;
						 }
					  }
						
						if(hasInfoRowFirst){
							jsonKey=currentSheet.getRow(headerRow).getCell(i).getStringCellValue();
						}
						else{
							jsonKey=currentSheet.getRow(headerRow).getCell(i).getStringCellValue();
						}
						jsonKey = cleanJsonKey(jsonKey);
						uploadedExcelHeaders.add(jsonKey.trim());
						if(hasCustomHeader){							
							if(getCustomHeader().containsKey(jsonKey.trim())){
								jsonKey=getCustomHeader().get(jsonKey.trim());
							}
							else{
								//trim the spaces so that Gson can parse value to the respective objects.
								jsonKey=jsonKey.trim();
							}
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}						
						if(fieldArgumentMap.containsKey(jsonKey.trim())){
							String fullySpecfiedfieldTypeName=(String) fieldArgumentMap.get(jsonKey.trim());
							boolean isWrapper=fullySpecfiedfieldTypeName.contains("java.lang");
							if(!isWrapper){
								logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets&&InfoRow)>>one of the beans field is not of type Wrapper class!.");
							}
							String[] fullySpecfiedfieldTypeNameArray=Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
							String fieldType=fullySpecfiedfieldTypeNameArray[2];
							if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)){
								ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
								jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
								if(jsonStringValue!=null) {
									jsonStringValue = advancedTrim(jsonStringValue);
								}
								jsonKV.put(jsonKey, jsonStringValue);
							}
							else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)){
								jsonKV.put(jsonKey, jsonLongValue);
							}
							else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)){
								jsonKV.put(jsonKey, jsonDoubleValue);
							}
							else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)){
								jsonKV.put(jsonKey, jsonBooleanValue);
							}
							else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)){
								jsonKV.put(jsonKey, jsonIntegerValue);
							}
							else if(fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)){
								if(jsonDateValue==null) {
									if(jsonStringValue!=null) {
										ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
										if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
											jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
										}
									}
								}
								jsonKV.put(jsonKey, jsonDateValue);
							}
						}
					}
					jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_EXCEL_ROW_INDEX, j);
					jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_ROW_NUM, (j+1));
					Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();
					BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
					baseExcelSheet= gson.fromJson(jsonKV.toString(), baseExcelSheet.getClass());
					excelHeaderBeanList.add(baseExcelSheet);
				}
				Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
				excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_COLUMN_VALUE_MAP, jsonKColumns);
				excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);
				List<String> processedErrorList = processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
				if(!processedErrorList.isEmpty()){	
					errorList.addAll(processedErrorList);
					//add the error list to the fromExcelMultiSheetMap
					sheetErrorListMap.put(sheetName, errorList);
				}
				fromExcelMultiSheetMap.put(sheetName, excelHeaderBeanList);
			}
			sheetErrorMapList.add(sheetErrorListMap);
			fromExcelMultiSheetMap.put(EXCEL_ERROR_LIST, sheetErrorMapList);
			logger.debug("ExcelProcessorUtil>>fromExcelExcelList(MultiSheets&&InfoRow)>>ends..");
			return fromExcelMultiSheetMap;
	}

	public Map<String, List<? extends Object>> fromExcelVerticalSheetBeanMap(File excelfile,Workbook workbook,Map<String, Class<? extends BaseExcelSheet>> excelHeaderBeanMap, boolean hasCustomHeader,boolean pivotEnabled) throws IOException, JSONException, NoSuchFieldException, SecurityException, ClassNotFoundException,InstantiationException, IllegalAccessException, InvalidFormatException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>begins..");
		Map<String, List<? extends Object>> fromExcelMultiSheetMap = new HashMap<String, List<? extends Object>>();
		Map<String,List<String>> sheetErrorListMap=new HashMap<>();
		List<Map<String,List<String>>> sheetErrorMapList = new ArrayList<>();
		if (!pivotEnabled) {
			logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>isPivotEnabled " + pivotEnabled+" calling the normal fromExcelExcelList");
			fromExcelMultiSheetMap=fromExcelBeanMap(excelfile,null,excelHeaderBeanMap,hasCustomHeader);
			return fromExcelMultiSheetMap;
		}		
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		if(workbook==null) {
			//commenting below for performance reason
			//refer https://stackoverflow.com/questions/11154678/xssfworkbook-takes-a-lot-of-time-to-load
			//FileInputStream inputS = new FileInputStream(excelfile);
			workbook = getWorkbook(excelfile);
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			List<String> errorList = new ArrayList<>();
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}
			if (excelHeaderBeanMap.containsKey(sheetName.trim())) {
				excelHeaderBeanClass = excelHeaderBeanMap.get(sheetName.trim());
			} else {
				continue;
			}
			currentSheet = removeTrailingEmptyRowsFromSheet(currentSheet);
			Class<?> clazz = excelHeaderBeanClass;
			BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
			processSheetAnnotation(baseExcelSheet);
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			Map<String,Object> fieldArgumentMap = new HashMap<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				fieldArgumentMap.put(field.getName(), field.getType().getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			List<BaseExcelSheet> excelHeaderBeanList = new ArrayList<BaseExcelSheet>();
			int rowCounter = 0;
			if(getHeaderRowNumber()!=0){
				rowCounter=getHeaderRowNumber()-1;
			}
			int totalRowCount = currentSheet.getPhysicalNumberOfRows();
			int columnHeader = toIndentNumber(this.headerColumn)  - 1;		
			int totalColumn = currentSheet.getRow(rowCounter).getPhysicalNumberOfCells();
			if(excelSheet!=null && (excelSheet.valueRowEndsAt()!=-1||!"".equals(excelSheet.valueColumnEndsAt()))) {
				if(rowCounter>excelSheet.valueRowEndsAt()) {
					rowCounter = excelSheet.valueRowEndsAt();
				}
				int columnEnd = toIndentNumber(excelSheet.valueColumnEndsAt())  - 1;	
				if(totalColumn>columnEnd) {
					rowCounter = columnEnd;
				}				
			}	
			JSONObject jsonKeyValues = new JSONObject();
			JSONObject jsonKeyRowNums = new JSONObject();
			Set<String> uploadedExcelHeaders = new HashSet<>();
			for(int i = rowCounter ; i<=totalRowCount;i++){
				if(currentSheet.getRow(i)==null){
					continue;
				}
				if(currentSheet.getRow(i).getCell(columnHeader)==null){
					continue;
				}
				String jsonKey=currentSheet.getRow(i).getCell(columnHeader).getStringCellValue();
				if(jsonKey==null || ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)){
					continue;
				}
				if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
					continue;
				}
				if (jsonKey != null && !ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)){
					jsonKey = cleanJsonKey(jsonKey);
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,columnHeader,i);
						uploadedExcelHeaders.add(modifiedJsonKey);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
						uploadedExcelHeaders.add(jsonKey);
					}					
				}
				if(!fieldArgumentMap.containsKey(jsonKey)){
					continue;
				}				
				jsonKeyRowNums.put(jsonKey, (i+1));
				Object[] valueArray = new Object[totalColumn-(columnHeader+1)];
				int valueArrayCounter = 0;
				for (int k = columnHeader+1; k < totalColumn; k++) {
					if (currentSheet.getRow(i).getCell(k) == null) {
						continue;
					}
					Cell cellValue = currentSheet.getRow(i).getCell(k);
					Integer jsonIntegerValue = null;
					Long jsonLongValue = null;
					Double jsonDoubleValue = null;
					String jsonStringValue = null;
					Boolean jsonBooleanValue = null;
					Date jsonDateValue = null;
					if (cellValue != null) {
						switch (cellValue.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							jsonStringValue = cellValue.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							jsonBooleanValue = cellValue.getBooleanCellValue();
							jsonStringValue = jsonBooleanValue.toString();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							jsonDoubleValue = cellValue.getNumericCellValue();
							jsonLongValue = new BigDecimal(cellValue.getNumericCellValue()).longValue();
							jsonIntegerValue = new BigDecimal(cellValue.getNumericCellValue()).intValue();
							jsonDateValue = cellValue.getDateCellValue();
							jsonStringValue = jsonDoubleValue.toString();
							break;
						// for Formula
						case Cell.CELL_TYPE_FORMULA:
							FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
							CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
							switch (cellValue.getCachedFormulaResultType()) {
							case Cell.CELL_TYPE_NUMERIC:
								jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
								jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
								jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
								jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
								jsonStringValue = jsonDoubleValue.toString();
								break;
							case Cell.CELL_TYPE_STRING:							
								jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
								break;
							}
							break;
						}
					}
					Object jsonVal = null;
					if (fieldArgumentMap.containsKey(jsonKey)) {
						String fullySpecfiedfieldTypeName = (String) fieldArgumentMap.get(jsonKey);
						String[] fullySpecfiedfieldTypeNameArray = Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
						String fieldType = fullySpecfiedfieldTypeNameArray[2];
						if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)) {
							ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
							jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
							if(jsonStringValue!=null) {
								jsonStringValue = advancedTrim(jsonStringValue);
							}
							jsonVal = jsonStringValue;
						} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)) {
							jsonVal = jsonLongValue;
						} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)) {
							jsonVal = jsonDoubleValue;
						} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)) {
							jsonVal = jsonBooleanValue;
						} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)) {
							jsonVal = jsonIntegerValue;
						} 
						else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)) {
							if(jsonDateValue==null) {
								if(jsonStringValue!=null) {
									ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
									if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
										jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
									}
								}
							}
							jsonVal = jsonDateValue;
						}
					}
					valueArray[valueArrayCounter]=jsonVal;
					valueArrayCounter++;
				}//column loop ends here
				jsonKeyValues.put(jsonKey, valueArray);
			}//row loop ends here
			Set<String> jsonKeySet = fieldArgumentMap.keySet();
			int valueArrayCounter = 0;
			while(valueArrayCounter<totalColumn-(columnHeader+1)){
				JSONObject jsonKV = new JSONObject();
				for(String jsonKey:jsonKeySet){
					Object containsValueArray = jsonKeyValues.opt(jsonKey);
					if(containsValueArray==null) {
						continue;
					}
					Object[] valueArray = (Object[]) jsonKeyValues.get(jsonKey);					
					Object value = valueArray[valueArrayCounter];
					jsonKV.put(jsonKey, value);
				}
				String columnNum = toIndentName((columnHeader+2)+valueArrayCounter);
				jsonKV.put(ExcelValidatorConstant.EXCEL_VALIDATOR_COLUMN_NAME, columnNum);
				Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();			
				baseExcelSheet = gson.fromJson(jsonKV.toString(), baseExcelSheet.getClass());
				excelHeaderBeanList.add(baseExcelSheet);
				valueArrayCounter++;
			}
			fromExcelMultiSheetMap.put(sheetName, excelHeaderBeanList);
			Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_ROW_VALUE_MAP, jsonKeyRowNums);
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);
			List<String> processedErrorList = processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
			if(!processedErrorList.isEmpty()){	
				errorList.addAll(processedErrorList);
				//add the error list to the fromExcelMultiSheetMap
				sheetErrorListMap.put(sheetName, errorList);
			}
		}
		sheetErrorMapList.add(sheetErrorListMap);
		fromExcelMultiSheetMap.put(EXCEL_ERROR_LIST, sheetErrorMapList);
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>ends..");
		return fromExcelMultiSheetMap;
	}
	
	public Map<String, List<? extends Object>> fromSingleValueVerticalSheetBeanMap(File excelfile,Workbook workbook,Map<String, Class<? extends BaseExcelSheet>> excelHeaderBeanMap, boolean hasCustomHeader,boolean pivotEnabled) throws IOException, JSONException, NoSuchFieldException, SecurityException, ClassNotFoundException,InstantiationException, IllegalAccessException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>begins..");
		Map<String, List<? extends Object>> fromExcelMultiSheetMap = new HashMap<String, List<? extends Object>>();
		Map<String,List<String>> sheetErrorListMap=new HashMap<>();
		List<Map<String,List<String>>> sheetErrorMapList = new ArrayList<>();
		if (!pivotEnabled) {
			logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>isPivotEnabled " + pivotEnabled+" calling the normal fromExcelExcelList");
			fromExcelMultiSheetMap=fromExcelBeanMap(excelfile,null,excelHeaderBeanMap,hasCustomHeader);
			return fromExcelMultiSheetMap;
		}		
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		if(workbook==null) {
			FileInputStream inputS = new FileInputStream(excelfile);
			workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			List<String> errorList = new ArrayList<>();
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}
			if (excelHeaderBeanMap.containsKey(sheetName.trim())) {
				excelHeaderBeanClass = excelHeaderBeanMap.get(sheetName.trim());
			} else {
				continue;
			}
			Class<?> clazz = excelHeaderBeanClass;
			BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
			processSheetAnnotation(baseExcelSheet);
			Map<String,Object> fieldArgumentMap = new HashMap<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				fieldArgumentMap.put(field.getName(), field.getType().getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			List<BaseExcelSheet> excelHeaderBeanList = new ArrayList<BaseExcelSheet>();
			JSONObject jsonKV = new JSONObject();
			JSONObject jsonKeyRowNums = new JSONObject();
			Set<String> uploadedExcelHeaders = new HashSet<>();
			int totalRows = currentSheet.getPhysicalNumberOfRows();
			int rowCounter = 0;
			if(getHeaderRowNumber()!=0){
				rowCounter=getHeaderRowNumber()-1;
			}
			for(int i=rowCounter;i<totalRows;i++){
				int columnHeader = toIndentNumber(this.headerColumn)  - 1;
				if (currentSheet.getRow(i)==null){
					continue;
				}
				if (currentSheet.getRow(i).getCell(columnHeader) == null) {
					continue;
				}
				
				String jsonKey=currentSheet.getRow(i).getCell(columnHeader).getStringCellValue();
				if(jsonKey==null ||ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)) {
					continue;
				}
				if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
					continue;
				}
				if (jsonKey != null && !ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)){					
					jsonKey = cleanJsonKey(jsonKey);
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,columnHeader,i);
						uploadedExcelHeaders.add(modifiedJsonKey);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
						uploadedExcelHeaders.add(jsonKey);
					}					
				}
				jsonKeyRowNums.put(jsonKey, (i+1));
				//get value
				Cell cellValue = currentSheet.getRow(i).getCell(columnHeader+1);
				Integer jsonIntegerValue = null;
				Long jsonLongValue = null;
				Double jsonDoubleValue = null;
				String jsonStringValue = null;
				Boolean jsonBooleanValue = null;
				Date jsonDateValue = null;
				if (cellValue != null) {
					switch (cellValue.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						jsonStringValue = cellValue.getStringCellValue();
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						jsonBooleanValue = cellValue.getBooleanCellValue();
						jsonStringValue = jsonBooleanValue.toString();
						break;
					case Cell.CELL_TYPE_NUMERIC:
						jsonDoubleValue = cellValue.getNumericCellValue();
						jsonLongValue = new BigDecimal(cellValue.getNumericCellValue()).longValue();
						jsonIntegerValue = new BigDecimal(cellValue.getNumericCellValue()).intValue();
						jsonDateValue = cellValue.getDateCellValue();
						jsonStringValue = jsonDoubleValue.toString();
						break;
					// for Formula
					case Cell.CELL_TYPE_FORMULA:
						FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); 
						CellValue formulaEvaluatedCellValue = formulaEvaluator.evaluate(cellValue);
						switch (cellValue.getCachedFormulaResultType()) {
						case Cell.CELL_TYPE_NUMERIC:
							jsonDoubleValue = formulaEvaluatedCellValue.getNumberValue();
							jsonLongValue = new BigDecimal(jsonDoubleValue).longValue();
							jsonIntegerValue = new BigDecimal(jsonDoubleValue).intValue();
							jsonDateValue = DateUtil.getJavaDate(jsonDoubleValue);//cellValue.getDateCellValue();
							jsonStringValue = jsonDoubleValue.toString();
							break;
						case Cell.CELL_TYPE_STRING:							
							jsonStringValue = cellValue.getStringCellValue().replaceAll("'", "");
							break;
						}
						break;
					}
				}
				Object jsonVal = null;
				if (fieldArgumentMap.containsKey(jsonKey)) {
					String fullySpecfiedfieldTypeName = (String) fieldArgumentMap.get(jsonKey);
					String[] fullySpecfiedfieldTypeNameArray = Arrays.copyOfRange(fullySpecfiedfieldTypeName.split("\\."), 0, 3);
					String fieldType = fullySpecfiedfieldTypeNameArray[2];
					if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_STRING)) {
						ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
						jsonStringValue = processNumericString(excelHeader,jsonStringValue,jsonLongValue,jsonDoubleValue,jsonBooleanValue,jsonIntegerValue,jsonDateValue);
						if(jsonStringValue!=null) {
							jsonStringValue = advancedTrim(jsonStringValue);
						}
						jsonVal = jsonStringValue;
					} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_LONG)) {
						jsonVal = jsonLongValue;
					} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DOUBLE)) {
						jsonVal = jsonDoubleValue;
					} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_BOOLEAN)) {
						jsonVal = jsonBooleanValue;
					} else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_INTEGER)) {
						jsonVal = jsonIntegerValue;
					} 
					else if (fieldType.equals(EXCEL_COLUMN_FIELD_TYPE_DATE)) {
						if(jsonDateValue==null) {
							if(jsonStringValue!=null) {
								ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
								if(DateParsingUtil.isDate(jsonStringValue, excelHeader.fromExcelDateFormats())) {
									jsonDateValue = DateParsingUtil.parseDate(jsonStringValue, excelHeader.fromExcelDateFormats());
								}
							}
						}
						jsonVal = jsonDateValue;
					}
				}
				if (jsonVal == null) {
					continue;
				}						
				jsonKV.put(jsonKey, jsonVal);
			}		
			Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();			
			baseExcelSheet = gson.fromJson(jsonKV.toString(), baseExcelSheet.getClass());
			excelHeaderBeanList.add(baseExcelSheet);
			fromExcelMultiSheetMap.put(sheetName, excelHeaderBeanList);
			Map<String,Object> excelValidationMetaDataMap = new HashMap<>();
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_FIELD_KEY_ROW_VALUE_MAP, jsonKeyRowNums);
			excelValidationMetaDataMap.put(ExcelValidatorConstant.EXCEL_HEADER_KEYS_MAP, uploadedExcelHeaders);			
			List<String> processedErrorList = processExcelValidation(excelHeaderBeanList,excelValidationMetaDataMap);
			if(!processedErrorList.isEmpty()){	
				errorList.addAll(processedErrorList);
				//add the error list to the fromExcelMultiSheetMap
				sheetErrorListMap.put(sheetName, errorList);
			}
		}
		sheetErrorMapList.add(sheetErrorListMap);
		fromExcelMultiSheetMap.put(EXCEL_ERROR_LIST, sheetErrorMapList);
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>ends..");
		return fromExcelMultiSheetMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<? extends Object>> fromExcelBeanMap(File excelfile,boolean hasMultiReaderSheet,Map<String, Object[]> excelHeaderBeanSheetReaderMetaDataMap, boolean hasCustomHeader) throws IOException, JSONException, NoSuchFieldException, SecurityException, ClassNotFoundException,InstantiationException, IllegalAccessException, InvalidFormatException {
		logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>begins..");
		Map<String, List<? extends Object>> fromExcelMultiSheetMap = new HashMap<String, List<? extends Object>>();
		FileInputStream inputS = new FileInputStream(excelfile);
		Workbook workbook = getWorkbook(inputS, excelfile.getAbsolutePath());
		Set<String> sheetNameSet = excelHeaderBeanSheetReaderMetaDataMap.keySet();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}
			if(!excelHeaderBeanSheetReaderMetaDataMap.containsKey(sheetName)) {
				continue;
			}
			Object[] sheetReaderMetaData = excelHeaderBeanSheetReaderMetaDataMap.get(sheetName);
			Boolean pivotEnabled = false;
			Integer rowHeaderStartIndex = 0;
			String headerColumn = EXCEL_COLUMN_INDENT_START;
			BaseExcelSheet baseExcelSheet = null;
			HashMap<String,String> customHeaderData = new HashMap<>();
			Map<String,Class<? extends BaseExcelSheet>> excelHeaderBeanMap = new HashMap<>();
			if(sheetReaderMetaData!=null) {
				if(sheetReaderMetaData.length>0) {
					if(sheetReaderMetaData[0] instanceof Integer) {
						rowHeaderStartIndex = (Integer) sheetReaderMetaData[0];
					}
					else if (sheetReaderMetaData[0] instanceof String) {
						headerColumn = (String) sheetReaderMetaData[0];
					}					
				}
				if(sheetReaderMetaData.length>1) {
					pivotEnabled = (Boolean) sheetReaderMetaData[1];
				}

				if(sheetReaderMetaData.length>2) {
					customHeaderData =  (HashMap<String, String>) sheetReaderMetaData[2];
				}
				if(sheetReaderMetaData.length>3) {
					baseExcelSheet =  (BaseExcelSheet) sheetReaderMetaData[3];
					excelHeaderBeanMap.put(sheetName, baseExcelSheet.getClass());
				}
			}
			this.headerRowNumber = rowHeaderStartIndex;
			this.headerColumn = headerColumn;
			this.customHeader  = customHeaderData;			
			if (!pivotEnabled) {
				logger.debug("ExcelProcessorUtil>>fromExcelExcelList(isPivotEnabled)>>isPivotEnabled " + pivotEnabled+" calling the normal fromExcelExcelList");
				fromExcelMultiSheetMap.putAll(fromExcelBeanMap(excelfile,workbook,excelHeaderBeanMap,hasCustomHeader));			
			}
			else {
				fromExcelMultiSheetMap.putAll(fromExcelVerticalSheetBeanMap(excelfile,workbook,excelHeaderBeanMap,hasCustomHeader,pivotEnabled));
			}
		}
		return fromExcelMultiSheetMap;
	}
	
	public Workbook toExcel(boolean hasMultiOrientationInSheet,List<? extends Object> toExcelList, String excelFileType, String sheetName,Workbook existingWorkBook,boolean hasCustomHeader,boolean pivotEnabled)
			throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, ParseException {
		Workbook wb = toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, pivotEnabled);
		if(multiOrientedExcelList!=null && !multiOrientedExcelList.isEmpty()) {
			processSheetAnnotation((BaseExcelSheet) multiOrientedExcelList.get(0));
			this.customHeader = flipMap(this.customHeader);
			ExcelSheet excelSheet = multiOrientedExcelList.get(0).getClass().getAnnotation(ExcelSheet.class);
			pivotEnabled = excelSheet.isVertical();
			wb = toExcel(multiOrientedExcelList, excelFileType, sheetName, wb, hasCustomHeader, pivotEnabled);
		}
		return wb;
	}

	public Workbook toExcel(List<? extends Object> toExcelList, String excelFileType, String sheetName,Workbook existingWorkBook,boolean hasCustomHeader,boolean pivotEnabled)
			throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, ParseException {
		logger.debug("ExcelProcessorUtil>>toExcel(existingWorkBook&customHeader)>>begins.. where isExistingWorkBook: "+(existingWorkBook instanceof XSSFWorkbook||existingWorkBook instanceof HSSFWorkbook)+" hasCustomHeader: "+hasCustomHeader);
		Workbook workbook=null;
		if(existingWorkBook==null){
			workbook = setWorkbook(excelFileType);
		}
		else{
			workbook = existingWorkBook;
		}
		if(toExcelList==null || toExcelList.size()<=0){
			logger.debug("ExcelProcessorUtil>>toExcel(existingWorkBook&customHeader)>>ends as the toExcelList doesn't have any data to write on excel");
			return workbook;
		}
		CellStyle cellStyle = workbook.createCellStyle();
		Sheet sheet = null;
		if(!hasExcelTemplate) {
			sheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(sheet)==-1) {
				sheet = workbook.createSheet(sheetName);
			}
		}		
		List<Object[]> valObjList = new ArrayList<Object[]>();
		List<Object[]> valObjVerticalList = new ArrayList<Object[]>();
		Class<?> clazz = toExcelList.get(0).getClass();
		List<String> fieldNameList = new ArrayList<String>();
		Object object = clazz.newInstance();
		Map<String,Class<?>> fieldArgumentMap = new HashMap<String,Class<?>>();
		Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
		//commenting below to get only the fields which are annotated with ExcelHeader
		//making ExcelHeader mandatory for this utility to recognize the header name to be exported
		//Set<Field> fields = ReflectionUtil.getFields(clazz);
		Set<Field> fields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
		fields = filterDynamicFields(fields,sheetName);
		for (Field field : fields) {
			fieldArgumentMap.put(field.getName(), field.getType());
			prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			fieldNameList.add(field.getName());
		}
		Map<String,String> headerFieldMap = flipMap(this.customHeader);
		fieldNameList = prepareOrderedFieldNameList(fieldNameList,sheetName,this.customHeader);
		Object[] headobjArr = new Object[fieldNameList.size()];
		for(int f=0;f<fieldNameList.size();f++){
			headobjArr[f] =fieldNameList.get(f);
		}
		if(pivotEnabled){
			for (String key : fieldNameList) {
				Object[] valueObjectVerticalArray = new Object[toExcelList.size()+1];
				int keyValCounter = 0;
				String excelHeader = key;
				if(hasCustomHeader){
					Map<String,String> customizeHeader=getCustomHeader();
					if(customizeHeader!=null && customizeHeader.containsKey(key.trim())){
						excelHeader=customizeHeader.get(key.trim());
						if(dynamicFieldHeaderMap!=null && !dynamicFieldHeaderMap.isEmpty()) {
							if(dynamicFieldHeaderMap.containsKey(key.trim())) {
								excelHeader=dynamicFieldHeaderMap.get(key.trim());
							}
						}
					}
				}
				if(!hasExcelTemplate) {
					valueObjectVerticalArray[keyValCounter] = excelHeader;	
					keyValCounter++;
				}
				for(Object objectItr :toExcelList){
					Object value = ReflectionUtil.getFieldValue(objectItr, key);
					valueObjectVerticalArray[keyValCounter]=value;
					keyValCounter++;
				}
				valObjVerticalList.add(valueObjectVerticalArray);
			}
		}
		else{
			for (int k = 0; k < toExcelList.size(); k++) {
				Object[] valueobjectArray = new Object[fieldNameList.size()];
				object = toExcelList.get(k);
				Gson gson = new GsonBuilder().setDateFormat("E MMM dd hh:mm:ss Z yyyy").create();
				String jsonString = gson.toJson(object);
				JSONObject jasonObject = new JSONObject(jsonString);
				JSONObject jasonCastedObject = new JSONObject();
				int keyValCounter = 0;
				Iterator<?> iterator = jasonObject.keys();
				while (iterator.hasNext()) {
					String fieldKey = (String) iterator.next();
					if(fieldNameList.contains(fieldKey)){
						Object jasonValue = null;
						Class<?> returnType=fieldArgumentMap.get(fieldKey);
						if(returnType.getName().equals("java.lang.Long")){
							 jasonValue = returnType.cast(Long.parseLong(jasonObject.get(fieldKey).toString()));
							 jasonCastedObject.put(fieldKey, jasonValue);
						}
						else if(returnType.toString().contains("java.lang.Byte")) {
							try{
								Field field = object.getClass().getDeclaredField(fieldKey);
								field.setAccessible(true);
								Object value = field.get(object);
								jasonCastedObject.put(fieldKey, value);
							}
							catch(Exception ex) {
								jasonCastedObject.put(fieldKey, jasonValue);
							}							
						}
						else{
							 jasonValue = jasonObject.get(fieldKey);
							 jasonCastedObject.put(fieldKey, jasonValue);
						}					
					}
				}
				for (String key : fieldNameList) {
					Object hasValue=jasonCastedObject.opt(key);
					if(hasValue!=null){
						Object jasonValue = jasonCastedObject.get(key);
						if(isDate(jasonValue.toString())){
							SimpleDateFormat format =  new SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy");
							Date date=format.parse(jasonValue.toString());
							jasonValue=date;
						}
						valueobjectArray[keyValCounter] = jasonValue;
					}
					keyValCounter++;
				}
				valObjList.add(valueobjectArray);
			}
		}		
		Map<Integer, Object> excelData = new HashMap<Integer, Object>();
		if(pivotEnabled){
			for (int i = 0; i < fieldNameList.size(); i++) {
				Integer k = i + 1;
				Object[] excelvalobjArr = new Object[toExcelList.size()+1];
				excelvalobjArr = valObjVerticalList.get(i);
				excelData.put(k, excelvalobjArr);
			}
		}
		else{
			if(hasCustomHeader){
				Map<String,String> customizeHeader=getCustomHeader();
				for(int hb=0;hb<headobjArr.length;hb++){
					String headerData=(String) headobjArr[hb];
					if(customizeHeader!=null && customizeHeader.containsKey(headerData.trim())){
						headobjArr[hb]=customizeHeader.get(headerData.trim());
						if(dynamicFieldHeaderMap!=null && !dynamicFieldHeaderMap.isEmpty()) {
							if(dynamicFieldHeaderMap.containsKey(headerData.trim())) {
								headobjArr[hb]=dynamicFieldHeaderMap.get(headerData.trim());
							}
						}
					}
				}
			}
			excelData.put(1, headobjArr);
			logger.debug("ExcelProcessorUtil>>toExcel(existingWorkBook&customHeader)>>Headers Count:"+fieldNameList.size());
			logger.debug("ExcelProcessorUtil>>toExcel(existingWorkBook&customHeader)>>Rows Count:"+toExcelList.size());
			for (int i = 0; i < toExcelList.size(); i++) {
				Integer k = i + 2;
				Object[] excelvalobjArr = new Object[valObjList.size()];
				excelvalobjArr = valObjList.get(i);
				excelData.put(k, excelvalobjArr);
			}	
		}
		int headerRowNum=0;
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		SortedSet<Integer> excelDataKeyset = new TreeSet<Integer>(excelData.keySet());
		int rownum = 0;
		int headerRowIndex =0;
		Row headerRowData = null;
		if(hasExcelTemplate) {
			if(existingWorkBook==null) {
				workbook = getWorkbookTemplate(workbook);
			}
			sheet = workbook.getSheet(sheetName);
		}
		for (Integer key : excelDataKeyset) {
			Row row = null;
			String headerKey = null;
			String jsonKey = null;
			String dateFormat = null;
			ExcelHeader pictureTypeHeader = null;
			if(hasExcelTemplate && isCopyHeaderStyle() && !pivotEnabled) {
				headerRowData = sheet.getRow(headerRowIndex);
			}
			if(hasExcelTemplate && !isCopyHeaderStyle() && !pivotEnabled) {
				if(headerRowIndex==0) {
					headerRowIndex = headerRowNum;
					headerRowData = sheet.getRow(headerRowIndex);
				}
				if(rownum==0) {
					rownum = headerRowNum+1;
					continue;
				}
			}
			if(hasExcelTemplate && pivotEnabled){
				if(rownum==0) {
					rownum = headerRowNum;
				}
				if(headerRowIndex==0) {
					headerRowIndex = headerRowNum;
				}
				row = sheet.getRow(headerRowIndex++);
				int columnHeader = toIndentNumber(this.headerColumn)  - 1;
				Cell headerRowCell = row.getCell(columnHeader);
				headerKey = headerRowCell.toString();
				headerKey = cleanJsonKey(headerKey);
				headerKey = processSimilarKey(headerKey,(BaseExcelSheet) object , columnHeader, rownum);
				if(headerFieldMap.containsKey(headerKey)) {
					jsonKey = headerFieldMap.get(headerKey);
					dateFormat = getToExcelFieldDateFormat(jsonKey,fieldExcelHeaderMap);
				}
				row = sheet.getRow(rownum++);
				Cell firstCellWithIgnoreLegend = row.getCell(0);
				if(firstCellWithIgnoreLegend!=null) {
					String legendString = firstCellWithIgnoreLegend.toString();
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(legendString)) {
						row = sheet.getRow(rownum++);
						headerRowIndex++;
						headerRowCell = row.getCell(columnHeader);
						headerKey = headerRowCell.toString();
						headerKey = cleanJsonKey(headerKey);
						headerKey = processSimilarKey(headerKey,(BaseExcelSheet) object , columnHeader, rownum-1);
						if(headerFieldMap.containsKey(headerKey)) {
							jsonKey = headerFieldMap.get(headerKey);
							dateFormat = getToExcelFieldDateFormat(jsonKey,fieldExcelHeaderMap);
						}
					}
				}
			}
			else{
				if(rownum==0) {
					rownum = headerRowNum;
				}
				row = sheet.createRow(rownum++);
			}
			Object[] valObjArray = (Object[]) excelData.get(key);
			int cellnum = 0;
			int cellIndex = 0;
			int columnHeader = toIndentNumber(this.headerColumn)  - 1;
			for (Object valObj : valObjArray) {
				if(hasExcelTemplate && pivotEnabled){
					while(cellnum<=columnHeader){
						cellnum++;	
					}	
				}

				if(hasExcelTemplate && !isCopyHeaderStyle() && !pivotEnabled) {
					Cell headerCell = headerRowData.getCell(cellIndex++);
					headerKey = headerCell.toString();
					if(headerFieldMap.containsKey(headerKey)) {
						jsonKey = headerFieldMap.get(headerKey);
						dateFormat = getToExcelFieldDateFormat(jsonKey,fieldExcelHeaderMap);
						pictureTypeHeader = getToExcelPictureHeader(jsonKey,fieldExcelHeaderMap);
					}
				}
				
				if(hasExcelTemplate && !pivotEnabled && isCopyHeaderStyle()) {
					if(key!=1) {
						Cell headerCell = headerRowData.getCell(cellnum);
						headerKey = headerCell.toString();
						if(headerFieldMap.containsKey(headerKey)) {
							jsonKey = headerFieldMap.get(headerKey);
							dateFormat = getToExcelFieldDateFormat(jsonKey,fieldExcelHeaderMap);
							pictureTypeHeader = getToExcelPictureHeader(jsonKey,fieldExcelHeaderMap);
						}	
					}
				}
				
				Cell cell = row.createCell(cellnum++);
				
			    if(hasStyleTemplate && !isCopyHeaderStyle()) {
				    copySheetStyle(workbook,sheetName,sheet,cell,rownum-1,cellnum-1);
			    }
			    
			    if(!hasExcelTemplate && !pivotEnabled) {
			    	headerRowData = sheet.getRow(headerRowNum);
					Cell headerCell = headerRowData.getCell(cellIndex++);
					headerKey = headerCell.toString();
					if(headerFieldMap.containsKey(headerKey)) {
						jsonKey = headerFieldMap.get(headerKey);
						dateFormat = getToExcelFieldDateFormat(jsonKey,fieldExcelHeaderMap);
						pictureTypeHeader = getToExcelPictureHeader(jsonKey,fieldExcelHeaderMap);
					}
			    }
			    
			    
			    if(rownum == headerRowNum+1 && isCopyHeaderStyle() && !pivotEnabled) {
			    	Cell headerCell = headerRowData.getCell(cellIndex++);
					copyCellStyle(headerCell, cell);
			    }
			    
				if (valObj instanceof String) {
					if(pictureTypeHeader!=null && pictureTypeHeader.pictureSource().equals(PictureSourceType.FILE_PATH)) {
						int r = cell.getRow().getRowNum();
						int c = cell.getColumnIndex();
						drawImageOnExcelSheet(sheet, r, r+1, c, c+1,(String) valObj,pictureTypeHeader);
					}
					else {
						try{ 
							Long longValObj=Long.parseLong((String)valObj); 
							cell.setCellValue((Long)longValObj);
						}
						catch(Exception e){
							cell.setCellValue((String) valObj);
						}
					}
				}
				else if (valObj instanceof Byte[]) {
					if(pictureTypeHeader!=null && pictureTypeHeader.pictureSource().equals(PictureSourceType.BYTE_ARRAY)) {
						int r = cell.getRow().getRowNum();
						int c = cell.getColumnIndex();
						drawImageOnExcelSheet(sheet, r, r+1, c, c+1,valObj,pictureTypeHeader);
					}
				}
				else if (valObj instanceof Integer){
					cell.setCellValue((Integer) valObj);
				}
				else if (valObj instanceof Long){
					cell.setCellValue((Long) valObj);
				}	
				else if (valObj instanceof Double){
					cell.setCellValue((Double) valObj);
				}
				else if (valObj instanceof Character){
					cell.setCellValue((Character) valObj);
				}
				else if (valObj instanceof Boolean){
					cell.setCellValue((Boolean) valObj);
				}					
				else if(valObj instanceof java.sql.Timestamp||valObj instanceof java.sql.Date||valObj instanceof java.util.Date){
					cell.setCellValue((Date) valObj);
					cellStyle = workbook.getCellStyleAt((short) (cellnum-1));
					if(getFormatDateIndex()!=0){
						cellStyle.setDataFormat(getFormatDateIndex());
					}
					CreationHelper creationHelper = workbook.getCreationHelper();					 
					if(dateFormat!=null) {
						cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(dateFormat));
					}
					else{
						cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
					}
					cell.setCellStyle(cellStyle);
				}
				setUserDefinedCellStyle(cell, workbook, jsonKey, fieldExcelHeaderMap);
			}
		}
		logger.debug("ExcelProcessorUtil>>toExcel(existingWorkBook&customHeader)>>ends..");
		if(sheet.getRow(headerRowNum).getPhysicalNumberOfCells()<=25 && !forceAutoSizing){
			forceAutoSizing = true;
		}
		if(!ignoreFormatting) {
			if(pivotEnabled){
				for(int i=headerRowNum;i<fieldNameList.size();i++){
					Row headerRow=sheet.getRow(i);
					if(!hasExcelTemplate) {
						makeVerticalHeaderBold(workbook, headerRow);	
					}
					if(this.wrapTexting) {
						wrapText(workbook,sheet);
					}				
				}
				Row headerRow=sheet.getRow(headerRowNum);
				if(forceAutoSizing && !hasExcelTemplate){
					autoSizeColumn(headerRow,sheet);
				}	
			}
			else{
				Row headerRow=sheet.getRow(headerRowNum);
				if(this.wrapTexting) {
					wrapText(workbook,sheet);
				}
				if(!hasExcelTemplate) {
					makeRowBold(workbook,headerRow);
				}
				if(forceAutoSizing && !hasExcelTemplate){
					autoSizeColumn(headerRow,sheet);
				}
			}
		}
		return workbook;
	}
	
	private Set<Field> filterDynamicFields(Set<Field> fields,String sheetName) {
		Set<String> dynamicFields = this.dynamicFields;
		if(dynamicFields==null) {
			if(this.dynamicFieldMap!=null && this.dynamicFieldMap.containsKey(sheetName)) {
				dynamicFields = this.dynamicFieldMap.get(sheetName);
			}
			else {
				return fields;
			}
		}
		Set<Field> dynamicFieldSet = new HashSet<Field>();
		for(Field field:fields) {
			if(dynamicFields.contains(field.getName())) {
				dynamicFieldSet.add(field);
			}
		}
		return dynamicFieldSet;
	}

	private byte[] castFromWrapperByteArray(Object pictureSource) {
		Byte[] wrapperBytes = (Byte[]) pictureSource;
		return ArrayUtils.toPrimitive(wrapperBytes);
	}
	
	private ExcelHeader getToExcelPictureHeader(String jsonKey, Map<String, ExcelHeader> fieldExcelHeaderMap) {
		if(fieldExcelHeaderMap.containsKey(jsonKey)) {
			ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
			if(excelHeader.picture()) {
				return excelHeader;
			}
		}
		return null;
	}

	private void drawImageOnExcelSheet(Sheet sheet, int row1,
        int row2, int col1, int col2, Object pictureSource, ExcelHeader pictureTypeHeader) {
		try {
	        byte[] bytes = null;
			if(pictureTypeHeader.pictureSource().equals(PictureSourceType.BYTE_ARRAY)) {
				bytes = castFromWrapperByteArray(pictureSource);
			}
			else if(pictureTypeHeader.pictureSource().equals(PictureSourceType.FILE_PATH)) {
		        InputStream is = new FileInputStream((String) pictureSource);
				bytes = IOUtils.toByteArray(is);
		        is.close();
			}
			int pictureType = Workbook.PICTURE_TYPE_JPEG;
			if(pictureTypeHeader.pictureType().equals(PictureType.PNG)){
				pictureType = Workbook.PICTURE_TYPE_PNG;
			}
			else if(pictureTypeHeader.pictureType().equals(PictureType.JPG)){
				pictureType = Workbook.PICTURE_TYPE_JPEG;
			}
	        int pictureIdx = sheet.getWorkbook().addPicture(bytes,pictureType);
	        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
	        Drawing drawing = sheet.createDrawingPatriarch();
	        ClientAnchor anchor = helper.createClientAnchor();
	        anchor.setRow1(row1);
	        anchor.setRow2(row2);
	        anchor.setCol1(col1);
	        anchor.setCol2(col2);
	        anchor.setAnchorType(pictureTypeHeader.pictureAnchorType().value());
	        Picture pic = drawing.createPicture(anchor, pictureIdx);
	        if(pictureTypeHeader.pictureResizeScale()!=-1) {
	        	pic.resize(pictureTypeHeader.pictureResizeScale());
	        }
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public Picture drawImageOnExcelSheet(Sheet sheet, 
	  int col1, int row1, int dx1/*1/256th of a character width*/, int dy1/*points*/,
	  int col2, int row2, int dx2/*1/256th of a character width*/, int dy2/*points*/, 
	  String pictureurl, int picturetype, boolean resize) throws Exception {

	  int DEFAULT_COL_WIDTH = 10 * 256;
	  float DEFAULT_ROW_HEIGHT = 12.75f;

	  Row row = sheet.getRow(row1);
	  float rowheight1 = (row!=null)?row.getHeightInPoints():DEFAULT_ROW_HEIGHT;
	  row = sheet.getRow(row2);
	  float rowheight2 = (row!=null)?row.getHeightInPoints():DEFAULT_ROW_HEIGHT;

	  int colwidth1 = sheet.getColumnWidth(col1);
	  int colwidth2 = sheet.getColumnWidth(col2);

	  InputStream is = new FileInputStream(pictureurl);
	  byte[] bytes = IOUtils.toByteArray(is);
	  int pictureIdx = sheet.getWorkbook().addPicture(bytes, picturetype);
	  is.close();

	  CreationHelper helper = sheet.getWorkbook().getCreationHelper();

	  Drawing drawing = sheet.createDrawingPatriarch();

	  ClientAnchor anchor = helper.createClientAnchor();
	  anchor.setAnchorType(ClientAnchor.DONT_MOVE_AND_RESIZE);

	  anchor.setRow1(row1); //first anchor determines upper left position
	  if (sheet instanceof XSSFSheet) {
	   anchor.setDy1(dy1 * Units.EMU_PER_POINT);
	  } else if (sheet instanceof HSSFSheet) {
	   anchor.setDy1((int)Math.round(dy1 * Units.EMU_PER_PIXEL / Units.EMU_PER_POINT * 14.75 * DEFAULT_ROW_HEIGHT / rowheight1));
	  }
	  anchor.setCol1(col1); 
	  if (sheet instanceof XSSFSheet) {
	   anchor.setDx1((int)Math.round(dx1 * Units.EMU_PER_PIXEL * Units.EMU_PER_PIXEL / 256f));
	  } else if (sheet instanceof HSSFSheet) {
	   anchor.setDx1((int)Math.round(dx1 * Units.EMU_PER_PIXEL / 256f * 14.75 * DEFAULT_COL_WIDTH / colwidth1));
	  }

	  if (!resize) {
		   anchor.setRow2(row2); //second anchor determines bottom right position
		   if (sheet instanceof XSSFSheet) {
			   anchor.setDy2(dy2 * Units.EMU_PER_POINT);
		   } 
		   else if (sheet instanceof HSSFSheet) {
			   anchor.setDy2((int)Math.round(dy2 * Units.EMU_PER_PIXEL / Units.EMU_PER_POINT * 14.75 * DEFAULT_ROW_HEIGHT / rowheight2));
		   }
		   anchor.setCol2(col2);
		   if (sheet instanceof XSSFSheet) {
			   anchor.setDx2((int)Math.round(dx2 * Units.EMU_PER_PIXEL * Units.EMU_PER_PIXEL / 256f));
		   } 
		   else if (sheet instanceof HSSFSheet) {
			   anchor.setDx2((int)Math.round(dx2 * Units.EMU_PER_PIXEL / 256f * 14.75 * DEFAULT_COL_WIDTH / colwidth2));
		   }
	  }

	  Picture picture = drawing.createPicture(anchor, pictureIdx);

	  if (resize) picture.resize();

	  return picture;
	}

	private String getToExcelFieldDateFormat(String jsonKey, Map<String, ExcelHeader> fieldExcelHeaderMap) {
		if(fieldExcelHeaderMap.containsKey(jsonKey)) {
			ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
			return excelHeader.toExcelDateFormat();
		}
		return ExcelDateFormat.DASH_MM_DD_YYYY;
	}

	public Workbook toExcel(List<? extends Object> toExcelList, String excelFileType,
			String sheetName,Workbook existingWorkBook,boolean hasCustomHeader,boolean hasInfoRowFirst,boolean hasInfoRowLast)
			throws JSONException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, ParseException {
		Workbook workbook =  toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, false);
		BaseExcelSheet excelSheetObject = (BaseExcelSheet) toExcelList.get(0);
		ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(excelSheetObject);
		Sheet sheet = workbook.getSheet(sheetName);
		int rowCounter = sheet.getPhysicalNumberOfRows();
		int headerRowIndex = excelSheet.headerRowAt()-1;
		int columnCounter = sheet.getRow(headerRowIndex).getPhysicalNumberOfCells();
		if(!(!hasInfoRowFirst && !hasInfoRowLast)){
			mergeCells(workbook,sheet,columnCounter,rowCounter,hasInfoRowFirst,hasInfoRowLast);
		}		
		return workbook;
	}
	
	public  Map<String,Map<String,Object>> validateExcelBeanMap(File excelfile,Workbook workbook,Map<String,Class<? extends BaseExcelSheet>> excelHeaderBeanMap,boolean hasCustomHeader,boolean pivotEnabled) throws IOException, JSONException, NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.debug("ExcelProcessorUtil>>validateExcelBeanMap(MultiSheets)>>begins..");
		Map<String,Map<String,Object>> excelMultiSheetValidationBeanMap = new HashMap<>();							
		Class<? extends BaseExcelSheet> excelHeaderBeanClass = null;
		if(workbook==null) {
			FileInputStream inputS = new FileInputStream(excelfile);
			workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		}
		Set<String> sheetNameSet = excelHeaderBeanMap.keySet();
		Set<String> unknownSheetNames = new LinkedHashSet<>();
		for (String sheetName:sheetNameSet){
			Sheet currentSheet =  workbook.getSheet(sheetName);
			if(workbook.getSheetIndex(currentSheet)==-1) {
				continue;
			}
			Map<String,Object> excelValidationBeanMap = new HashMap<>();
			if (workbook.isSheetHidden(workbook.getSheetIndex(currentSheet))) {
				continue;
			}		
			if(excelHeaderBeanMap.containsKey(sheetName.trim())){
				excelHeaderBeanClass=excelHeaderBeanMap.get(sheetName.trim());
			}
			else {
				unknownSheetNames.add(sheetName);
				continue;
			}
			Class<?> clazz = excelHeaderBeanClass;
			BaseExcelSheet baseExcelSheet=excelHeaderBeanClass.newInstance();
			processSheetAnnotation(baseExcelSheet);
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			boolean ignoreUnknown = false;
			if(excelSheet!=null) {
				pivotEnabled = excelSheet.isVertical();
				ignoreUnknown = excelSheet.ignoreUnknown();
			}
			int headerRowNum=0;
			if(getHeaderRowNumber()!=0){
				headerRowNum=getHeaderRowNumber()-1;
			}
			int columnHeader = toIndentNumber(this.headerColumn) - 1;
			List<String> excelHeaderNameList = new ArrayList<>();
			Map<String,ExcelHeader> fieldExcelHeaderMap = new HashMap<>();
			//commenting below to get only the fields which are annotated with ExcelHeader
			//making ExcelHeader mandatory for this utility to recognize the header name
			//Field[] fieldArray = clazz.getDeclaredFields();
			Set<Field> excelHeaderFields = AnnotationUtil.getAnnotatedFields(clazz, ExcelHeader.class);
			for (Field field : excelHeaderFields) {
				excelHeaderNameList.add(field.getName());
				prepareFieldExcelHeaderMap(field,fieldExcelHeaderMap);
			}
			int rowCounter = currentSheet.getPhysicalNumberOfRows();
			Row headerRow = currentSheet.getRow(headerRowNum);
			if(headerRow==null) {
				excelValidationBeanMap.put("headerRowMismatch", "Mismatch between ExcelSheet annotated headerRow==>"+headerRowNum+" and the imported sheet's header row");
				excelMultiSheetValidationBeanMap.put(sheetName, excelValidationBeanMap);
				continue;
			}
			int cellCounter=currentSheet.getRow(headerRowNum).getPhysicalNumberOfCells();
			Set<String> unknownHeaderColumns = new LinkedHashSet<>();
			Set<String> unknownHeaderColumnDetail = new LinkedHashSet<>();
			if(pivotEnabled){
				for(int j=headerRowNum;j<rowCounter;j++){
					if(currentSheet.getRow(j)==null){
						continue;
					}
					if(currentSheet.getRow(j).getCell(columnHeader)==null){
						continue;
					}
					String jsonKey=currentSheet.getRow(j).getCell(columnHeader).getStringCellValue();
					if(jsonKey==null || ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)){
						continue;
					}					
					jsonKey = cleanJsonKey(jsonKey);
					if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
						continue;
					}
					if(hasCustomHeader){
						String modifiedJsonKey = jsonKey;
						modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,columnHeader,j);
						if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
							jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
					}
					else{
						//trim the spaces so that Gson can parse value to the respective objects.
						jsonKey=jsonKey.trim();
					}
					if(!excelHeaderNameList.contains(jsonKey.trim())){
						unknownHeaderColumns.add(jsonKey.trim());
						unknownHeaderColumnDetail.add("Invalid column name '"+jsonKey.trim() +"' at row["+(j+1)+"],column["+toIndentName(columnHeader+1)+"]");
					}
					else {
						excelHeaderNameList.remove(jsonKey.trim());
					}
				}
			}
			else{
				for(int j=headerRowNum;j<headerRowNum+1;j++){
					for(int i=0;i<cellCounter;i++){
						if(currentSheet.getRow(headerRowNum)==null){
							continue;
						}
						if(currentSheet.getRow(headerRowNum).getCell(i)==null){
							continue;
						}
						String jsonKey=currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue();
						if(jsonKey==null || ExcelValidatorConstant.EMPTY_STRING.equals(jsonKey)){
							continue;
						}						
						jsonKey = cleanJsonKey(jsonKey);
						if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
							continue;
						}
						if(hasCustomHeader){
							String modifiedJsonKey = jsonKey;
							modifiedJsonKey = processSimilarKey(jsonKey,baseExcelSheet,i,j);
							if(getCustomHeader().containsKey(modifiedJsonKey.trim())){
								jsonKey=getCustomHeader().get(modifiedJsonKey.trim());
							}
							else{
								//trim the spaces so that Gson can parse value to the respective objects.
								jsonKey=jsonKey.trim();
							}
						}
						else{
							//trim the spaces so that Gson can parse value to the respective objects.
							jsonKey=jsonKey.trim();
						}
						if(!excelHeaderNameList.contains(jsonKey.trim())){
							unknownHeaderColumns.add(jsonKey.trim());
							unknownHeaderColumnDetail.add("Invalid column name '"+jsonKey.trim() +"' at row["+(j+1)+"],column["+toIndentName(i+1)+"]");
						}
						else {
							excelHeaderNameList.remove(jsonKey.trim());
						}
					}
				}
			}
			if(!unknownHeaderColumns.isEmpty()){	
				excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_HAS_UNKNOWN_HEADER_COLUMNS, true);
				excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_UNKNOWN_HEADER_COLUMNS, unknownHeaderColumns);
				excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_UNKNOWN_HEADER_COLUMN_DETAIL, unknownHeaderColumnDetail);
				
				excelMultiSheetValidationBeanMap.put(sheetName, excelValidationBeanMap);
			}
			if(!excelHeaderNameList.isEmpty() && !ignoreUnknown) {
				List<String> excelHeaderMissingList = new ArrayList<>();
				for(String jsonKey:excelHeaderNameList) {
					Map<String,String> headerJsonKeyMap = flipMap(getCustomHeader());
					String excelHeader = headerJsonKeyMap.get(jsonKey);
					excelHeaderMissingList.add(excelHeader);
				}
				excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_HAS_MISSING_HEADER_COLUMNS, true);
				excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_MISSING_HEADER_COLUMNS, excelHeaderMissingList);
				
				excelMultiSheetValidationBeanMap.put(sheetName, excelValidationBeanMap);
			}
		}
		if(!unknownSheetNames.isEmpty()) {
			Map<String,Object> excelValidationBeanMap = new HashMap<>();
			excelValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_UNKNOWN_SHEETS, unknownSheetNames);
			excelMultiSheetValidationBeanMap.put(ExcelValidatorConstant.EXCEL_VALIDATOR_UNKNOWN_SHEETS, excelValidationBeanMap);
		}
		logger.debug("ExcelProcessorUtil>>validateExcelBeanMap(MultiSheets)>>ends..");
		return excelMultiSheetValidationBeanMap;
	}
	
	public Sheet deleteRowRange(Sheet sheet, int fromRow, int toRow) {
		Iterator<Row> rowIterator = sheet.rowIterator();
		int rowCounter = 0;
		while(rowIterator.hasNext()) {
			//Row row = rowIterator.next();
			if(rowCounter>=fromRow && rowCounter<=toRow) {
				rowIterator.remove();
			}
			rowCounter++;
		}
		return sheet;
	}
	
	public Row deleteCellRange(Row row, int fromCell, int toCell) {
		Iterator<Cell> cellIterator = row.cellIterator();
		int cellCounter = 0;
		while(cellIterator.hasNext()) {
			if(cellCounter>=fromCell && cellCounter<=toCell) {
				cellIterator.remove();
			}
			cellCounter++;
		}
		return row;
	}

	private String processSimilarKey(String jsonKey, BaseExcelSheet baseExcelSheet, int columnIndex, int rowIndex) {
		if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)) {
			ExcelSheet excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
			if(excelSheet.hasDuplicateHeaders()) {
				Set<Field> fields = AnnotationUtil.getAnnotatedFields(baseExcelSheet.getClass(), ExcelHeader.class);
				for(Field field:fields) {
					ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
					String columnName = toIndentName(columnIndex+1);
					if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeader.value())
							&& excelHeader.value().equals(jsonKey)) {
						if(excelSheet.isVertical()) {
							if((excelHeader.row()-1) == rowIndex) {						
								return jsonKey+APPEND_UNDERSCORE+excelHeader.row();
							}						
						}
						else {
							if(excelHeader.column().equals(columnName)) {	
								return jsonKey+APPEND_UNDERSCORE+excelHeader.column();
							}
						}	
					}					
				}
			}
		}
		return jsonKey;
	}
	
	private void prepareFieldExcelHeaderMap(Field field, Map<String, ExcelHeader> fieldExcelHeaderMap) {
		if(field.isAnnotationPresent(ExcelHeader.class)) {
			ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
			fieldExcelHeaderMap.put(field.getName(), excelHeader);
		}
	}
	
	private String processNumericString(ExcelHeader excelHeader, String jsonStringValue, Long jsonLongValue, Double jsonDoubleValue,
			Boolean jsonBooleanValue, Integer jsonIntegerValue, Date jsonDateValue) {
		if(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_DOUBLE.equals(excelHeader.numberFormat())){
			if(jsonDoubleValue!=null) {
				jsonStringValue = jsonDoubleValue.toString();
			}
		}
		else if(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_INTEGER.equals(excelHeader.numberFormat())){
			if(jsonIntegerValue!=null) {
				jsonStringValue = jsonIntegerValue.toString();
			}
		}
		else if(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_LONG.equals(excelHeader.numberFormat())){
			if(jsonLongValue!=null) {
				jsonStringValue = jsonLongValue.toString();
			}
		}
		else if(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_DATE.equals(excelHeader.numberFormat())){
			if(jsonDateValue!=null) {
				jsonStringValue = jsonDateValue.toString();
			}
		}
		else if(ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_DEFAULT.equals(excelHeader.numberFormat())){
			if(jsonLongValue!=null) {
				jsonStringValue = jsonLongValue.toString();
			}
		}
		return jsonStringValue;
	}
	
	private void processSheetAnnotation(BaseExcelSheet baseExcelSheet) {
		if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)){
			ExcelSheet excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
			setHeaderRowNumber(excelSheet.headerRowAt());
			setHeaderColumn(excelSheet.headerColumnAt());
			customHeader.clear();
			prepareHeaderMapFromHeaderBean(baseExcelSheet, customHeader);
			String[] ignoreHeaders = excelSheet.ignoreHeaders();
			List<String> ignoreHeaderList = null;
			if(ignoreHeaders.length>0) {
				ignoreHeaderList = Arrays.asList(ignoreHeaders);
			}
			else {
				String ignoreHeaderKey = excelSheet.ignoreHeaderKey();
				if(excelValidatorContext!=null){
					if(excelValidatorContext.getPredefinedDatasetMap()!=null){
						if(excelValidatorContext.getPredefinedDatasetMap().containsKey(ignoreHeaderKey)){
							ignoreHeaderList = excelValidatorContext.getPredefinedDatasetMap().get(ignoreHeaderKey);
						}
					}
				}
			}
			
			setIgnoreHeaderList(ignoreHeaderList);
		}
	}
	
	private void prepareHeaderMapFromHeaderBean(BaseExcelSheet baseExcelSheet,Map<String,String> customHeaderMap){
		Set<Field> fields = AnnotationUtil.getAnnotatedFields(baseExcelSheet.getClass(), ExcelHeader.class);
		for(Field field:fields){
			ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
			String headerValue = excelHeader.value();
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			String appendOnDuplicate = null;
			if(!excelSheet.isVertical()) {
				String column = excelHeader.column();
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(column)) {
					appendOnDuplicate = column;
				}
			}
			else {
				int row = excelHeader.row();
				if(row!=-1) {
					appendOnDuplicate = row+"";
				}
			}
			if(appendOnDuplicate!=null) {
				headerValue = headerValue+ExcelProcessorUtil.APPEND_UNDERSCORE+appendOnDuplicate;
			}

			String beanValue = field.getName();
			
			if(headerValue!=null && !"".equals(headerValue)){
				customHeaderMap.put(headerValue, beanValue);
			}
		}
	}
	
	private boolean processExcelSheet(ValidatorContext validatorContext,BaseExcelValidationSheet baseExcelValidationSheet, List<String> errorList){
		ExcelSheet excelSheet = baseExcelValidationSheet.getClass().getAnnotation(ExcelSheet.class);
		validatorContext.setExcelSheet(excelSheet);
		validatorContext.setBaseExcelValidationSheet(baseExcelValidationSheet);
		if(!(excelSheet.customTaskValidator().getName()).equals(AbstractCustomValidatorTask.class.getName())) {
			ICustomValidatorTask customValidatorTask = null;
			try {
				customValidatorTask = excelSheet.customTaskValidator().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("ExcelProcessorUtil>>processExcelSheet>>caught exception:"+e);
			}
			validatorContext.setValidatorTask(customValidatorTask);
		}
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelSheet.customTask())){			
			validatorContext.setSheetCustomValidation(true);
			validatorContext.setCustomSheetTask(excelSheet.customTask());
			processSheetValidation(baseExcelValidationSheet,validatorContext,errorList);
			validatorContext.setSheetCustomValidation(false);
		}
		if(excelSheet.customTasks().length>0){			
			for(String customTask:excelSheet.customTasks()) {
				validatorContext.setCustomSheetTask(customTask);
				validatorContext.setSheetCustomValidation(true);
				processSheetValidation(baseExcelValidationSheet,validatorContext,errorList);
				validatorContext.setSheetCustomValidation(false);
			}			
		}
		return excelSheet.hasValidation();
	}
	
	private void processSheetValidation(BaseExcelSheet baseExcelSheet, ValidatorContext validatorContext,
			List<String> errorList) {
		IExcelValidator iexcelValidator = ValidatorFactory.getValidator(ExcelValidatorConstant.EXCEL_CUSTOM_METHOD_VALIDATOR);
		processValidator(iexcelValidator, validatorContext, errorList);
	}
	
	private void processExcelHeaderValidator(ValidatorContext validatorContext,Field field,BaseExcelSheet baseExcelSheet, List<String> errorList){
		ExcelHeaderValidator excelHeaderValidator = field.getAnnotation(ExcelHeaderValidator.class);
		validatorContext.setExcelHeaderValidator(excelHeaderValidator);
		if(field.isAnnotationPresent(ExcelHeader.class)) {
			ExcelHeader excelHeader= field.getAnnotation(ExcelHeader.class);
			validatorContext.setExcelHeader(excelHeader);
		}
		List<IExcelValidator> iexcelValidatorList = ValidatorFactory.getValidator(excelHeaderValidator);
		if(iexcelValidatorList.size()>0){
			Object columnValue = ReflectionUtil.getFieldValue(baseExcelSheet, field);
			String jsonKey = field.getName();
			String headerKey = jsonKey;
			if(customHeader!=null){
				Map<String,String> flippedHeaderMap = flipMap(customHeader);
				if(flippedHeaderMap.containsKey(jsonKey)){
					headerKey = flippedHeaderMap.get(jsonKey);
				}						
				validatorContext.setFieldNameHeaderNameMap(flippedHeaderMap);
			}										
			validatorContext.setJsonKey(jsonKey);					
			validatorContext.setHeaderKey(headerKey);					
			validatorContext.setColumnValue(columnValue);											
		}
		processValidators(iexcelValidatorList, validatorContext, baseExcelSheet, errorList);
	}
	
	private void processValidators(List<IExcelValidator> excelHeaderBeanValidatorList,
			ValidatorContext validatorContext,BaseExcelSheet baseExcelSheet, List<String> errorList){
		if(excelHeaderBeanValidatorList.size()>0){										
			for(IExcelValidator iexcelValidator:excelHeaderBeanValidatorList){
				processValidator(iexcelValidator, validatorContext, errorList);
			}
		}
	}
	
	private void processValidator(IExcelValidator iexcelValidator,ValidatorContext validatorContext,List<String> errorList){
		String errorMessage = iexcelValidator.validate(validatorContext);
		if(errorMessage!=null){
			errorList.add(errorMessage);
		}
	}
	
	private List<String> processExcelValidation(List<BaseExcelSheet> excelHeaderBeanList, Map<String, Object> excelValidationMetaDataMap) throws JSONException {
		List<String> fullSheetErrorList = new ArrayList<>();		
		ValidatorContext validatorContext = new ValidatorContext();		
		validatorContext.setExcelValidationMetaDataMap(excelValidationMetaDataMap);
		validatorContext.setExcelValidatorContext(excelValidatorContext);
		for(BaseExcelSheet baseExcelSheet:excelHeaderBeanList){
			List<String> errorList = new ArrayList<>();
			if(baseExcelSheet.getClass().getSuperclass().equals(BaseExcelValidationSheet.class)){
				BaseExcelValidationSheet baseExcelValidationSheet = (BaseExcelValidationSheet) baseExcelSheet;
				boolean hasValidation = processBaseExcelSheetClassAnnotations(validatorContext, baseExcelValidationSheet,errorList);
				if(hasValidation){
					Set<Field> baseExcelValidationSheetFieldSet = new LinkedHashSet<>();
					Set<Field> excelHeaderValidatorFieldSet = AnnotationUtil.getAnnotatedFields(baseExcelValidationSheet.getClass(),ExcelHeaderValidator.class );
					Set<Field> predefinedFieldSet = AnnotationUtil.getAnnotatedFields(baseExcelValidationSheet.getClass(),Predefined.class );
					baseExcelValidationSheetFieldSet.addAll(predefinedFieldSet);
					baseExcelValidationSheetFieldSet.addAll(excelHeaderValidatorFieldSet);
					int rowNum = ReflectionUtil.getFieldValue(baseExcelValidationSheet, ExcelValidatorConstant.EXCEL_VALIDATOR_ROW_NUM);			
					String columnName = ReflectionUtil.getFieldValue(baseExcelValidationSheet, ExcelValidatorConstant.EXCEL_VALIDATOR_COLUMN_NAME);
					validatorContext.setRowNum(rowNum);	
					validatorContext.setColumnName(columnName);
					for(Field field:baseExcelValidationSheetFieldSet){
						processBaseExcelSheetFieldAnnotations(validatorContext, field,baseExcelValidationSheet,errorList);	
					}
					//set the errorList in each excelBean Object
					baseExcelValidationSheet.setErrorList(errorList);
					fullSheetErrorList.addAll(errorList);
				}
			}
		}
		return fullSheetErrorList;
	}
	
	private boolean processBaseExcelSheetClassAnnotations(ValidatorContext validatorContext,
			BaseExcelValidationSheet baseExcelValidationSheet, List<String> errorList) {
		return processExcelSheet(validatorContext, baseExcelValidationSheet,errorList);
		
	}
	
	private void processBaseExcelSheetFieldAnnotations(ValidatorContext validatorContext, Field field,
			BaseExcelValidationSheet excelHeaderBean, List<String> errorList) {
		if(field.isAnnotationPresent(ExcelHeaderValidator.class)){
			processExcelHeaderValidator(validatorContext, field,excelHeaderBean,errorList);
		}
		if(field.isAnnotationPresent(Predefined.class)){
			processPredefined(validatorContext, field,excelHeaderBean,errorList);
		}
	}
	
	private void processPredefined(ValidatorContext validatorContext, Field field,
			BaseExcelValidationSheet excelHeaderBean, List<String> errorList) {
		Predefined predefined = field.getAnnotation(Predefined.class);
		validatorContext.setPredefined(predefined);
		Object columnValue = ReflectionUtil.getFieldValue(excelHeaderBean, field);
		String jsonKey = field.getName();
		String headerKey = jsonKey;
		if(customHeader!=null){
			Map<String,String> flippedHeaderMap = flipMap(customHeader);
			if(flippedHeaderMap.containsKey(jsonKey)){
				headerKey = flippedHeaderMap.get(jsonKey);
			}						
			validatorContext.setFieldNameHeaderNameMap(flippedHeaderMap);
		}
		String[] predefinedValues = predefined.predefinedValues();
		List<String> predefinedValueList = null;
		if(predefinedValues.length>0){
			 predefinedValueList = Arrays.asList(predefinedValues);
		}
		else{
			String predefinedDatasetName = predefined.predefinedDatasetKey();
			if(excelValidatorContext.getPredefinedDatasetMap()!=null){
				if(excelValidatorContext.getPredefinedDatasetMap().containsKey(predefinedDatasetName)) {
					predefinedValueList = (List<String>) excelValidatorContext.getPredefinedDatasetMap().get(predefinedDatasetName);
				}
			}
		}
		String predefinedMessage = !ExcelValidatorConstant.EMPTY_STRING.equals(predefined.messageDescription()) ? predefined.messageDescription():headerKey+" value doesn’t match with the dropdown list provided in the sheet at row["+ExcelHeaderConstant.ROW_NUM_PLACEHOLDER+"]";
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(predefined.messageDescription())) {
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(excelHeaderBean);
			if(excelSheet!=null) {
				if(excelSheet.isVertical()){
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, validatorContext.getColumnName()+"");
				}
				else {
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, validatorContext.getRowNum()+"");
				}
				if(predefinedValueList!=null && !predefinedValueList.isEmpty() && predefined.messageDescription().contains(ExcelHeaderConstant.PREDEFINED_HEADER_DATA_PLACEHOLDER)) {
					StringBuilder predefinedValueSb = new StringBuilder("");
					for(String predefinedValue:predefinedValueList) {
						predefinedValueSb.append(predefinedValue);
						predefinedValueSb.append(",");
					}
					String finalPredefinedValues = predefinedValueSb.toString().replaceAll(",$", "");;
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.PREDEFINED_HEADER_DATA_PLACEHOLDER,finalPredefinedValues);
				}	
			}		
		}		
		if(columnValue!=null){
			if(columnValue instanceof String) {
				columnValue = ((String) columnValue).trim();
			}
			if(ExcelValidatorConstant.EMPTY_STRING.equals(columnValue)) {
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(predefined.messageDescription()) && predefined.messageDescription().contains(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER)) {
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER,"Empty");
				}
				if(!predefined.allowEmpty()){
					errorList.add(predefinedMessage);
				}
			}
			else if(!predefinedValueList.contains(columnValue)){
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(predefined.messageDescription()) && predefined.messageDescription().contains(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER)) {
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER,columnValue+"");
				}
				errorList.add(predefinedMessage);
			}
		}
		else{
			if(columnValue==null) {
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(predefined.messageDescription()) && predefined.messageDescription().contains(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER)) {
					predefinedMessage = predefinedMessage.replace(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER,"Null");
				}
				if(!predefined.allowEmpty() && !predefined.allowNull()){
					errorList.add(predefinedMessage);
				}
			}
		}
	}
	
	private ExcelSheet getExcelSheetFromBaseExcelSheet(BaseExcelSheet baseExcelSheet) {
		ExcelSheet excelSheet = null;
		if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)){
			excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
		}
		return excelSheet;
	}
	
	private void mergeCells(Workbook workbook,Sheet sheet,int colLen,int rowLen,boolean hasFirst,boolean hasLast){
		 if(hasFirst && !hasLast){
			Row row = null;
			if(hasExcelTemplate) {
				row = sheet.getRow((short) 0);
			}
			else {
				row = sheet.createRow((short) 0);
			}
			if(getRowHeight()!=0){
				row=setDoubleRowHeight(row);
			}
		    Cell cell = null;
		    if(hasExcelTemplate) {
		    	cell = row.getCell((short) 0);
		    }
		    else {
		    	cell = row.createCell((short) 0);
		    }
		    cell.setCellValue(formatCellValue(workbook,getFirstRowValue()));
		    sheet.autoSizeColumn(0);
		    sheet.addMergedRegion(new CellRangeAddress(
	                 0, // mention first row here
	                 0, //mention last row here
	                 0, //mention first column of merging
	                 colLen  //mention last column to include in merge
	                 ));
		}
		else if (hasLast && !hasFirst){
			Row row = null;
			if(hasExcelTemplate) {
				row = sheet.getRow((short) rowLen);
			}
			else {
				row = sheet.createRow((short) rowLen);
			}			
			if(getRowHeight()!=0){
				row=setDoubleRowHeight(row);
			}
		    Cell cell = row.createCell((short) 0);	    
		    cell.setCellValue(formatCellValue(workbook,getLastRowValue()));
		    sheet.addMergedRegion(new CellRangeAddress(
					 rowLen, // mention first row here
					 rowLen, //mention last row here
	                 0, //mention first column of merging
	                 colLen  //mention last column to include in merge
	                 ));
		}
		else if (hasLast && hasFirst){
			Row rowF = sheet.createRow((short) 0);
			if(getRowHeight()!=0){
				rowF=setDoubleRowHeight(rowF);
			}
		    Cell cellF = rowF.createCell((short) 0);
		    cellF.setCellValue(formatCellValue(workbook,getFirstRowValue()));
		    sheet.addMergedRegion(new CellRangeAddress(
	                 0, // mention first row here
	                 0, //mention last row here
	                 0, //mention first column of merging
	                 colLen  //mention last column to include in merge
	                 ));
			
			Row rowL = sheet.createRow((short) rowLen+1);
			if(getRowHeight()!=0){
				rowL=setDoubleRowHeight(rowL);
			}
		    Cell cellL = rowL.createCell((short) 0);
		    cellL.setCellValue(formatCellValue(workbook,getLastRowValue()));
		    sheet.addMergedRegion(new CellRangeAddress(
					 rowLen+1, // mention first row here
					 rowLen+1, //mention last row here
	                 0, //mention first column of merging
	                 colLen  //mention last column to include in merge
	                 ));
		}
	}	
	
	public RichTextString formatCellValue(Workbook workbook,String cellString){
		RichTextString string = applyFont(workbook, cellString);
		return string;
	}
	
	public RichTextString applyFont(Workbook workbook,String htmlString) {
		RichTextString string = null;
	    Map<String,ArrayList<Integer>> tagIndexMap = new HashMap<>();
	    String finalString = processHtmlTags(htmlString,tagIndexMap);
		if(workbook instanceof XSSFWorkbook){
			 string = new XSSFRichTextString(finalString);
		}
		else if(workbook instanceof HSSFWorkbook){
			 string = new HSSFRichTextString(finalString);
		}

		
	    for(String htmlTag :tagIndexMap.keySet()) {
	    	List<Integer> applyIndexList = tagIndexMap.get(htmlTag);
	    	ListIterator<Integer>  litr = applyIndexList.listIterator();
	    	while(litr.hasNext()) {
	    		int start = litr.next();
	    		int end = litr.next();
	    		string.applyFont(start,end,getFontByHtmlTag(workbook, htmlTag));
	    	}	    	
	    }
	    
	    return string;
	}
	
	public Font getFontByHtmlTag(Workbook workbook,String htmlTag) {
		if(htmlTag.equals("BOLD")) {
			Font boldFont = workbook.createFont();
			boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			return boldFont;
		}
		else if(htmlTag.equals("STRIKEOUT")) {
			Font strikeOutFont = workbook.createFont();
			strikeOutFont.setStrikeout(true);
			return strikeOutFont;
		}
		else if(htmlTag.equals("ITALIC")) {
			Font italicFont = workbook.createFont();
			italicFont.setItalic(true);
			return italicFont;
					
		}
		else if(htmlTag.equals("UNDERLINE")) {
			Font underlineFont = workbook.createFont();
			underlineFont.setUnderline(Font.U_SINGLE);
			return underlineFont;
		}
		else if(htmlTag.contains("COLOR")) {
			Font colorFont = workbook.createFont();
			colorFont.setColor(getColorIndex(htmlTag));
			return colorFont;
		}
		return null;
	}
	
	public short getColorIndex(String colorName) {
		Map<String,Short> htmlColorIndexMap = new HashMap<>();
		htmlColorIndexMap.put("COLOR_AQUA",IndexedColors.AQUA.getIndex());
		htmlColorIndexMap.put("COLOR_BLACK",IndexedColors.BLACK.getIndex());
		htmlColorIndexMap.put("COLOR_BLUE",IndexedColors.BLUE.getIndex());
		htmlColorIndexMap.put("COLOR_BLUE_GREY",IndexedColors.BLUE_GREY.getIndex());
		htmlColorIndexMap.put("COLOR_BRIGHT_GREEN",IndexedColors.BRIGHT_GREEN.getIndex());
		htmlColorIndexMap.put("COLOR_BROWN",IndexedColors.BROWN.getIndex());
		htmlColorIndexMap.put("COLOR_CORAL",IndexedColors.CORAL.getIndex());	
		htmlColorIndexMap.put("COLOR_CORNFLOWER_BLUE",IndexedColors.CORNFLOWER_BLUE.getIndex());
		htmlColorIndexMap.put("COLOR_DARK_RED",IndexedColors.DARK_RED.getIndex());
		htmlColorIndexMap.put("COLOR_DARK_BLUE",IndexedColors.DARK_BLUE.getIndex());
		htmlColorIndexMap.put("COLOR_DARK_GREEN",IndexedColors.DARK_GREEN.getIndex());
		htmlColorIndexMap.put("COLOR_DARK_TEAL",IndexedColors.DARK_TEAL.getIndex());
		htmlColorIndexMap.put("COLOR_DARK_YELLOW",IndexedColors.DARK_YELLOW.getIndex());
		htmlColorIndexMap.put("COLOR_GOLD",IndexedColors.GOLD.getIndex());
		htmlColorIndexMap.put("COLOR_GREEN",IndexedColors.GREEN.getIndex());
		htmlColorIndexMap.put("COLOR_INDIGO",IndexedColors.INDIGO.getIndex());
		htmlColorIndexMap.put("COLOR_LAVENDER",IndexedColors.LAVENDER.getIndex());
		htmlColorIndexMap.put("COLOR_LIME",IndexedColors.LIME.getIndex());
		htmlColorIndexMap.put("COLOR_MAROON",IndexedColors.MAROON.getIndex());
		htmlColorIndexMap.put("COLOR_ORANGE",IndexedColors.ORANGE.getIndex());
		htmlColorIndexMap.put("COLOR_PINK",IndexedColors.PINK.getIndex());		
		htmlColorIndexMap.put("COLOR_RED",IndexedColors.RED.getIndex());
		htmlColorIndexMap.put("COLOR_SKY_BLUE",IndexedColors.SKY_BLUE.getIndex());
		htmlColorIndexMap.put("COLOR_SEA_GREEN",IndexedColors.SEA_GREEN.getIndex());
		htmlColorIndexMap.put("COLOR_VIOLET",IndexedColors.VIOLET.getIndex());
		htmlColorIndexMap.put("COLOR_WHITE",IndexedColors.WHITE.getIndex());		
		htmlColorIndexMap.put("COLOR_YELLOW",IndexedColors.YELLOW.getIndex());
		if(htmlColorIndexMap.containsKey(colorName)) {
			return htmlColorIndexMap.get(colorName);
		}
		return IndexedColors.BLACK.getIndex();
	}
	
	public String processHtmlTags(String htmlString, Map<String,ArrayList<Integer>> tagIndexMap) {
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");   
	    matcher = pattern.matcher(htmlString);
	    int aboveES = 0;
	    Map<String,String> htmlTagRefernceMap = new HashMap<>();
	    htmlTagRefernceMap.put("</b>", "BOLD");
	    htmlTagRefernceMap.put("</s>", "STRIKEOUT");
	    htmlTagRefernceMap.put("</i>", "ITALIC");
	    htmlTagRefernceMap.put("</u>", "UNDERLINE");
	    htmlTagRefernceMap.put("</aqua>","COLOR_AQUA");
	    htmlTagRefernceMap.put("</black>","COLOR_BLACK");
	    htmlTagRefernceMap.put("</blue>","COLOR_BLUE");
	    htmlTagRefernceMap.put("</bluegrey>","COLOR_BLUE_GREY");
	    htmlTagRefernceMap.put("</brightgreen>","COLOR_BRIGHT_GREEN");
	    htmlTagRefernceMap.put("<brown>","COLOR_BROWN");
	    htmlTagRefernceMap.put("</corol>","COLOR_CORAL");	
	    htmlTagRefernceMap.put("</cornblue>","COLOR_CORNFLOWER_BLUE");
	    htmlTagRefernceMap.put("</darkred>","COLOR_DARK_RED");
	    htmlTagRefernceMap.put("</darkblue>","COLOR_DARK_BLUE");
	    htmlTagRefernceMap.put("</darkgreen>","COLOR_DARK_GREEN");
	    htmlTagRefernceMap.put("</darkteal>","COLOR_DARK_TEAL");
	    htmlTagRefernceMap.put("</darkyellow>","COLOR_DARK_YELLOW");
	    htmlTagRefernceMap.put("</gold>","COLOR_GOLD");
	    htmlTagRefernceMap.put("</green>","COLOR_GREEN");
	    htmlTagRefernceMap.put("</indigo>","COLOR_INDIGO");
	    htmlTagRefernceMap.put("</lavender>","COLOR_LAVENDER");
	    htmlTagRefernceMap.put("</lime>","COLOR_LIME");
	    htmlTagRefernceMap.put("</maroon>","COLOR_MAROON");
	    htmlTagRefernceMap.put("</orange>","COLOR_ORANGE");
	    htmlTagRefernceMap.put("</pink>","COLOR_PINK");		
	    htmlTagRefernceMap.put("</red>","COLOR_RED");
	    htmlTagRefernceMap.put("</skyblue>","COLOR_SKY_BLUE");
	    htmlTagRefernceMap.put("</seagreen>","COLOR_SEA_GREEN");
	    htmlTagRefernceMap.put("</violet>","COLOR_VIOLET");
		htmlTagRefernceMap.put("</white>","COLOR_WHITE");		
		htmlTagRefernceMap.put("</yellow>","COLOR_YELLOW");
	    ArrayList<Integer> tagList = new ArrayList<Integer>(5);
	    if (matcher.find()) {
	        do {
	        	if(matcher.start()!=0) {
	            	int indexs = (matcher.start()) - aboveES;	            	
	            	tagList.add(indexs);	            	
	            }
	            else {
	            	tagList.add(0);
	            }
	        	if(tagList.size()==2) {
	        		ArrayList<Integer> tagList1 = new ArrayList<Integer>(tagList);
	        		if(tagIndexMap.containsKey(htmlTagRefernceMap.get(matcher.group()))) {
	        			tagIndexMap.get(htmlTagRefernceMap.get(matcher.group())).addAll(tagList1);
	        			tagList1 = tagIndexMap.get(htmlTagRefernceMap.get(matcher.group()));
	        		}
	        		tagIndexMap.put(htmlTagRefernceMap.get(matcher.group()), tagList1);	        		
	        		tagList.clear();
	        	}
	            aboveES = aboveES + (matcher.end())- (matcher.start());	           
	        } while(matcher.find());	        
	        return htmlString.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
	    }
	    return htmlString;
	}

	private void makeRowBold(Workbook wb, Row row){
	    CellStyle style =  wb.createCellStyle();//Create style
	    Font font = wb.createFont();//Create font
	    font.setBoldweight(Font.BOLDWEIGHT_BOLD);//Make font bold	    
	    for(int i = 0; i < row.getLastCellNum(); i++){//For each cell in the row 
		    style.setFont(font);//set it to bold
	        row.getCell(i).setCellStyle(style);//Set the style
	    }
	}
	
	private void makeVerticalHeaderBold(Workbook wb, Row row){
		CellStyle style =  wb.createCellStyle();//Create style
	    Font font = wb.createFont();//Create font
	    font.setBoldweight(Font.BOLDWEIGHT_BOLD);//Make font bold	
	    style.setFont(font);//set it to bold
        row.getCell(0).setCellStyle(style);//Set the style
	}
	
	public void setFontWeight(Workbook workbook, Row row,short fontType,boolean isPivot) {
	    Font font = workbook.createFont();//Create font
	    font.setBoldweight(fontType);	
	    setFont(workbook, row, font, isPivot);
	}
	
	private void setFont(Workbook wb, Row row,Font font,boolean isPivot) {
	    CellStyle style = wb.createCellStyle();//Create style  
	    if(isPivot) {
	    	style.setFont(font);//set it to bold
		    row.getCell(0).setCellStyle(style);//Set the style
	    }
	    else {
	    	for(int i = 0; i < row.getLastCellNum(); i++){//For each cell in the row 
		    	style = row.getCell(i).getCellStyle();
			    if(style==null) {
					style = wb.createCellStyle();
				}
			    style.setFont(font);//set it to bold
		        row.getCell(i).setCellStyle(style);//Set the style
		    }
	    }
	}
	
	private void autoSizeColumn(Row row,Sheet sheet){
		for(int colNum = 0; colNum<row.getLastCellNum();colNum++){
			sheet.autoSizeColumn(colNum);			
		}	   
	}
		
	public void autoSizeColumn(Sheet sheet,int colNum){
		sheet.autoSizeColumn(colNum);   
	}

	private Row setDoubleRowHeight(Row row){
		
		row.setHeight((short)(int)(getRowHeight() * 20.0F));
		return row;
	}

	private boolean isDate(final String date) throws ParseException {
		if (date == null) {
			return false;
		}

		SimpleDateFormat format =  new SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy");
		try {
			format.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public <K,V> HashMap<V,K> flipMap(Map<K,V> map) {
	    HashMap<V,K> flip = new HashMap<V, K>();
	    for(Map.Entry<K,V> entry : map.entrySet())
	    	flip.put(entry.getValue(), entry.getKey());
	    return flip;
	}

	public void wrapText(Workbook wb,Sheet sheet) {
		int rowCounter=0;
		CellStyle style = null;//Create style
		Iterator<Row> rowIterator = sheet.iterator();		
		while (rowIterator.hasNext()) {
			rowIterator.next();
			Row row=sheet.getRow(rowCounter);
			if(rowCounter==0){
				rowCounter++;
				continue;
			}
			for(int i = 0; i < row.getLastCellNum(); i++){//For each cell in the row 
				style = row.getCell(i).getCellStyle();
				if(style==null) {
					style = wb.createCellStyle();
				}
				style.setWrapText(true);//wrap the text of the cell
		        row.getCell(i).setCellStyle(style);//Set the style
		    }
			rowCounter++;
		}
	}
		
	public void wrapTextUsingPredicate(Workbook wb,Sheet sheet) {
		int rowCounter=0;
		CellStyle style = null;//Create style
		Iterator<Row> rowIterator = sheet.iterator();
		Iterator<Row> filterIterator = rowIterator;
		@SuppressWarnings("unchecked")
		Iterator<Row> nonEmptyRowIterator = IteratorUtils.filteredIterator(filterIterator , new ValidRowPredicate<Row>());
		while (nonEmptyRowIterator.hasNext()) {
			nonEmptyRowIterator.next();
			Row row=sheet.getRow(rowCounter);
			if(rowCounter==0){
				rowCounter++;
				continue;
			}
			for(int i = 0; i < row.getLastCellNum(); i++){//For each cell in the row 
				style = row.getCell(i).getCellStyle();
				if(style==null) {
					style = wb.createCellStyle();
				}
				style.setWrapText(true);//wrap the text of the cell
		        row.getCell(i).setCellStyle(style);//Set the style
		    }
			rowCounter++;
		}
	}

	public String getFirstRowValue() {
		return firstRowValue;
	}
	
	public void setFirstRowValue(String firstRowValue) {
		this.firstRowValue = firstRowValue;
	}
	
	public String getLastRowValue() {
		return lastRowValue;
	}
	
	public void setLastRowValue(String lastRowValue) {
		this.lastRowValue = lastRowValue;
	}
	
	public String generateExcelSheetMappingBeanFromExcel(File excelfile,String sheetName,boolean isPivotEnabled,List<String> ignoreHeaderList, boolean hasValidation) throws IOException, ClassNotFoundException {
		int headerRowNum=0;		
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		StringBuilder excelBeanBuilder = new StringBuilder("");
		int valueRowNum=headerRowNum+1;
		FileInputStream inputS = new FileInputStream(excelfile);
		Workbook workbook = getWorkbook(inputS,excelfile.getAbsolutePath());
		Sheet currentSheet =  workbook.getSheet(sheetName);
		if(currentSheet==null){
			return "Sheet name is not present in the excel workbook";
		}
		int cellCounter=0;
		int rowCounter=0;
		Iterator<Row> rowIterator = currentSheet.iterator();
		while (rowIterator.hasNext()) {
			rowIterator.next();
			rowCounter++;
		}
		if(rowCounter>0) {
			Iterator<Cell> cellKeyIterator=currentSheet.getRow(headerRowNum).iterator();
			while (cellKeyIterator.hasNext()) {
				Cell cell = cellKeyIterator.next();
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			          continue;
			       } else if (cell.getCellType() == Cell.CELL_TYPE_STRING && 
			                  cell.getStringCellValue().isEmpty()) {
			    	   continue;
			      }
				cellCounter++;
			}
			List<String> jsonHeaderList = new ArrayList<>();
			List<String> jsonHeaderTypeList = new ArrayList<>();
			List<String> jsonHeaderValidJavaVariableList = new ArrayList<>();
			String jsonKey = null;				
				if(isPivotEnabled) {
					generateExcelBeanAndCustomHeaderPivotFromExcel(currentSheet, cellCounter, jsonKey, jsonHeaderList, jsonHeaderTypeList, jsonHeaderValidJavaVariableList,ignoreHeaderList);									
				}	
				else {
					generateExcelBeanAndCustomHeaderFromExcel(currentSheet, cellCounter, headerRowNum, valueRowNum, jsonKey, jsonHeaderList, jsonHeaderTypeList, jsonHeaderValidJavaVariableList,ignoreHeaderList);					 
				}
			excelBeanBuilder.append("=====================================================================================================================================================");
			excelBeanBuilder.append("\n\t\t\t\t\t\t    "+sheetName+" Sheet BEGINS    \n");				
			excelBeanBuilder.append("=====================================================================================================================================================");
			excelBeanBuilder.append("\n");			
			if(isEnableHBMGenerator()) {
				excelBeanBuilder.append("===================================================   Excel HBM XML   =============================================================================");
				excelBeanBuilder.append("\n");
				List<String> tableColumnNameList = generateTableColumnNameFromJavaVariable(jsonHeaderValidJavaVariableList);
				excelBeanBuilder.append(generateHBMXml(sheetName, jsonHeaderTypeList, jsonHeaderValidJavaVariableList, tableColumnNameList));
			}
			else if(isEnableTableGenerator()) {
				excelBeanBuilder.append("===================================================   Excel ORACLE DDL TABLE  =============================================================================");
				excelBeanBuilder.append("\n");
				List<String> tableColumnNameList = generateTableColumnNameFromJavaVariable(jsonHeaderValidJavaVariableList);
				excelBeanBuilder.append(generateOracleSQLCreateTable(jsonHeaderTypeList, tableColumnNameList));
			}
			else {
				excelBeanBuilder.append("===================================================   Excel Bean Pojo   =============================================================================");
				excelBeanBuilder.append("\n");
				excelBeanBuilder.append(generatePojoFromExcel(sheetName,isPivotEnabled,jsonHeaderList,jsonHeaderTypeList,jsonHeaderValidJavaVariableList,hasValidation));
			}			
			excelBeanBuilder.append("\n");
			excelBeanBuilder.append("======================================================================================================================================================");
			excelBeanBuilder.append("\n================================================================ENDS==================================================================================");
			excelBeanBuilder.append("\n======================================================================================================================================================");
		}
		return excelBeanBuilder.toString();
	}
	
	public String generateCustomHeaderMap(String mapVariable,List<String> jsonHeaderList,List<String> jsonHeaderValidJavaVariableList,boolean swap) {
		StringBuilder sb  = new StringBuilder("");
		int index = 0;
		if(swap) {
			for(String field:jsonHeaderValidJavaVariableList) {
				sb.append("\t"+mapVariable+".put(\""+field.trim()+"\",\""+jsonHeaderList.get(index)+"\");");
				sb.append("\n");
				index++;
			}
		}
		else {
			for(String field:jsonHeaderList) {
				sb.append("\t"+mapVariable+".put(\""+field.trim()+"\",\""+jsonHeaderValidJavaVariableList.get(index)+"\");");
				sb.append("\t\n");
				index++;
			}
		}

		return sb.toString();
	}
	private List<String> generateTableColumnNameFromJavaVariable(List<String> jsonHeaderValidJavaVariableList) {
		List<String> tableColumnNameList = new ArrayList<>();
		for(String javaVariable:jsonHeaderValidJavaVariableList) {
			String dbColumnName = convertToTableColumnName(javaVariable);
			tableColumnNameList.add(dbColumnName);
		}
		return tableColumnNameList;
	}
	
	private String convertToTableColumnName(String javaVariableName) {
		Map<Integer,Integer> upperIndexes = new LinkedHashMap<>();
		int temp = 0;
		for (int i = 0; i < javaVariableName.length(); i++) {	
	        if(Character.isUpperCase(javaVariableName.charAt(i))){    
	        	upperIndexes.put(temp,i);
	        	temp = i;
	        }
	        if(i==javaVariableName.length()-1) {
	        	upperIndexes.put(temp,(i+1));
	        }
		}
		StringBuilder sb = new StringBuilder("");
		String columnName = null;
		for(int i :upperIndexes.keySet()) {       	
        	sb.append(javaVariableName.substring(i, upperIndexes.get(i)).toUpperCase());
        	String currentColumnName = sb.toString();
        	if(currentColumnName.length()>=30) {
        		currentColumnName = currentColumnName.substring(0,30);
        		columnName = currentColumnName;
        		break;
        	}
        	sb.append(APPEND_UNDERSCORE);        	
		}
		if(columnName!=null) {
			return columnName.replaceAll(APPEND_UNDERSCORE+"$", "");
		}
		return sb.toString().replaceAll(APPEND_UNDERSCORE+"$", "");
	}
	
	private String generateHBMXml(String sheetName,List<String> jsonHeaderTypeList, List<String> jsonHeaderValidJavaVariableList, List<String> jsonJavaVariableTableColumnList) {
		StringBuilder sb  = new StringBuilder("");
		sb.append("<class name=\"give_fully_qualified_class_name\" table=\"give_the_tablename\">");
		sb.append("\n");
		sb.append("\t<!--<id name=\"id_field_variable\" type=\"java.lang.Long\" >");
		sb.append("\n");
		sb.append("\t<column name=\"mapped_db_column_name\" />");
		sb.append("\n");
		sb.append("\t<generator class=\"sequence\">");
		sb.append("\n");
		sb.append("\t\t<param name=\"sequence\">GENERATED_SEQUENCE_NAME</param>");
		sb.append("\n");
		sb.append("\t</generator>");
		sb.append("\n");
		sb.append("\t</id>-->");
		sb.append("\n");
		for(int i=0;i<jsonHeaderValidJavaVariableList.size();i++) {
			sb.append("\t<property name=\""+jsonHeaderValidJavaVariableList.get(i)+"\" type=\"java.lang."+jsonHeaderTypeList.get(i)+"\" >");
			sb.append("\n\t   <column name=\""+jsonJavaVariableTableColumnList.get(i)+"\" />\n\t</property>\n");
		}
		sb.append("\n");
		sb.append("</class>");
		return sb.toString();
	}
	
	private String generateOracleSQLCreateTable(List<String> jsonHeaderTypeList, List<String> jsonJavaVariableTableColumnList) {
		StringBuilder sbTable  = new StringBuilder("");
		sbTable.append("CREATE TABLE TABLE_NAME(");
		StringBuilder sbColumns  = new StringBuilder("");
		for(int i=0;i<jsonJavaVariableTableColumnList.size();i++) {
			String columnName = jsonJavaVariableTableColumnList.get(i);
			String javaType = jsonHeaderTypeList.get(i);
			String columnType = "";
			if(EXCEL_COLUMN_FIELD_TYPE_DATE.equals(javaType)) {
				columnType = "DATE";
			}
			else if(EXCEL_COLUMN_FIELD_TYPE_INTEGER.equals(javaType)||EXCEL_COLUMN_FIELD_TYPE_LONG.equals(javaType)) {
				columnType = "NUMBER(11)";
			}
			else if(EXCEL_COLUMN_FIELD_TYPE_STRING.equals(javaType)) {
				columnType = "VARCHAR2(200)";
			}
			sbColumns.append("\n");
			sbColumns.append(String.format("%-30s %s", columnName, columnType));
			sbColumns.append(",");
		}
		sbTable.append(sbColumns.toString().replaceAll(",$", ""));
		sbTable.append("\n)");
		return sbTable.toString();
	}
	
	private String generatePojoFromExcel(String sheetName, boolean isPivotEnabled, List<String> jsonHeaderList,List<String> jsonHeaderTypeList, List<String> jsonHeaderValidJavaVariableList, boolean hasValidation) {
		StringBuilder sb  = new StringBuilder("");
		int index = 0;
		Set<String> duplicateItemList = findDuplicates(jsonHeaderValidJavaVariableList);
		String className = toCamelCase(sheetName);
		className = deleteJavaInValidVariables(className);
		String hasDuplicateHeaderString = "";
		if(!duplicateItemList.isEmpty()) {
			hasDuplicateHeaderString=", hasDuplicateHeaders=true";
		}
		String hasValidationBool = "";
		if(hasValidation) {
			hasValidationBool=", hasValidation=true";
		}
		if(isPivotEnabled && !ignoreExcelAnnotation) {
			sb.append("@ExcelSheet(value=\""+sheetName+"\""+hasValidationBool+", isVertical=true"+hasDuplicateHeaderString+", headerRowAt="+headerRowNumber+", headerColumnAt=\""+headerColumn+"\")\n");		
		}
		else {
			if(!ignoreExcelAnnotation) {
				sb.append("@ExcelSheet(value=\""+sheetName+"\""+hasValidationBool+hasDuplicateHeaderString+", headerRowAt="+headerRowNumber+", headerColumnAt=\""+headerColumn+"\")\n");
			}
		}		
		String parentBaseSheet = "BaseExcelSheet";
		if(hasValidation) {
			parentBaseSheet = "BaseExcelValidationSheet";
		}
		if(!ignoreExcelAnnotation) {
			sb.append("public class "+className+"Sheet extends "+parentBaseSheet+"{\n");
		}
		else {
			sb.append("public class "+className+"Sheet {\n");
		}		
		for(String field:jsonHeaderValidJavaVariableList) {
			if(IGNORE_LIST_ITEM.equals(field)) {
				index++;
				continue;
			}
			if(duplicateItemList.contains(field)) {
				if(isPivotEnabled) {
					int pivotRowNum = headerRowNumber + index;
					if(!ignoreExcelAnnotation) {
						sb.append("    @ExcelHeader(value=\""+jsonHeaderList.get(index)+"\", row="+(pivotRowNum)+")\n");
					}
					sb.append("    private "+jsonHeaderTypeList.get(index)+ " "+field+APPEND_UNDERSCORE+(pivotRowNum)+";");
				}
				else {
					String column = toIndentName(index+1);
					if(!ignoreExcelAnnotation) {
						sb.append("    @ExcelHeader(value=\""+jsonHeaderList.get(index)+"\", column=\""+column+"\")\n");
					}
					sb.append("    private "+jsonHeaderTypeList.get(index)+ " "+field+APPEND_UNDERSCORE+column+";");
				}
			}
			else {
				if(!ignoreExcelAnnotation) {
					sb.append("    @ExcelHeader(\""+jsonHeaderList.get(index)+"\")\n");
				}
				sb.append("    private "+jsonHeaderTypeList.get(index)+ " "+field+";");
			}
			sb.append("\n");
			index++;
		}
		index = 0;
		sb.append("\n");
		sb.append("  //getters and setters");
		sb.append("\n");
		for(String field:jsonHeaderValidJavaVariableList) {
			if(IGNORE_LIST_ITEM.equals(field)) {
				index++;
				continue;
			}
			String toggleCasefield = field.substring(0,1).toUpperCase()+field.substring(1);
			int pivotRowNum = headerRowNumber + index;
			if(duplicateItemList.contains(field)) {
				if(isPivotEnabled) {
					sb.append("    public "+jsonHeaderTypeList.get(index)+ " get"+toggleCasefield+APPEND_UNDERSCORE+(pivotRowNum)+"() {");
					sb.append("\n       return this."+field+APPEND_UNDERSCORE+(pivotRowNum)+";");
					sb.append("\n    }\n");
					sb.append("    public void set"+toggleCasefield+APPEND_UNDERSCORE+(pivotRowNum)+"("+jsonHeaderTypeList.get(index)+" "+field+APPEND_UNDERSCORE+(pivotRowNum)+") {");
					sb.append("\n        this."+field+APPEND_UNDERSCORE+(pivotRowNum)+" = "+field+APPEND_UNDERSCORE+(pivotRowNum)+";");
				}
				else {
					String column = toIndentName(index+1);
					sb.append("    public "+jsonHeaderTypeList.get(index)+ " get"+toggleCasefield+APPEND_UNDERSCORE+column+"() {");
					sb.append("\n       return this."+field+APPEND_UNDERSCORE+column+";");
					sb.append("\n    }\n");
					sb.append("    public void set"+toggleCasefield+APPEND_UNDERSCORE+column+"("+jsonHeaderTypeList.get(index)+" "+field+APPEND_UNDERSCORE+column+") {");
					sb.append("\n        this."+field+APPEND_UNDERSCORE+column+" = "+field+APPEND_UNDERSCORE+column+";");
				}
			}
			else {
				sb.append("    public "+jsonHeaderTypeList.get(index)+ " get"+toggleCasefield+"() {");
				sb.append("\n       return this."+field+";");
				sb.append("\n    }\n");
				sb.append("    public void set"+toggleCasefield+"("+jsonHeaderTypeList.get(index)+" "+field+") {");
				sb.append("\n        this."+field+" = "+field+";");
			}
			sb.append("\n    }\n");
			index++;
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	public String deleteJavaInValidVariables(String before) {
        String javaInValidVariables = "[^0-9a-zA-Z]";
        String after = before.replaceAll(javaInValidVariables, "");
        return after;
    }   
	
	public String toCamelCase(String s) {
		if(s.length()>1){
			s = s.substring(0, 1).toUpperCase()+s.substring(1);
		}
		return s;
	}
	
	public String toPascalCase(String s) {
		String [] spacedString = s.split(" ");
		String finalString = s;
		if(spacedString.length>0) {
			StringBuilder camelCaseBuilder = new StringBuilder();
			for(int i=0;i<spacedString.length;i++) {
				if(spacedString[i].length()>0){
					if(i==0) {
						camelCaseBuilder.append(spacedString[i].toLowerCase());
					}
					else {
						camelCaseBuilder.append(spacedString[i].substring(0, 1).toUpperCase()+spacedString[i].substring(1).toLowerCase());
					}
				}
			}
			finalString = camelCaseBuilder.toString();
			String hasVariable = "#";
			finalString = finalString.replace(hasVariable, "No");
			finalString = finalString.substring(0, 1).toLowerCase()+finalString.substring(1);
		}
	    return finalString;
	}
	
	public String cleanJsonKey(String jsonKey) {
		jsonKey = jsonKey.replaceAll("\\r\\n|\\r|\\n", " ");
		jsonKey = jsonKey.trim();
		return jsonKey;
	}
	
	private void generateExcelBeanAndCustomHeaderFromExcel(Sheet currentSheet,int cellCounter,int headerRowNum,int valueRowNum,String jsonKey,List<String> jsonHeaderList,List<String> jsonHeaderTypeList,List<String> jsonHeaderValidJavaVariableList,List<String> ignoreHeaderList) {
		for(int i=0;i<cellCounter;i++){
			 jsonKey=currentSheet.getRow(headerRowNum).getCell(i).getStringCellValue();
			 jsonKey = cleanJsonKey(jsonKey);
			 if(ignoreHeaderList!=null && ignoreHeaderList.contains(jsonKey)) {
				continue;
			 }
			 jsonKey = jsonKey.trim();
			 jsonHeaderList.add(jsonKey);
			 jsonKey = toPascalCase(jsonKey);
			 jsonKey = deleteJavaInValidVariables(jsonKey);
			 jsonHeaderValidJavaVariableList.add(jsonKey);
			 
			 String jsonStringValue = null;
			 if(currentSheet.getRow(valueRowNum)==null) {
				 jsonStringValue = EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING;
				 jsonHeaderTypeList.add(jsonStringValue);
				 continue;
			 }
			 Cell cellValue=currentSheet.getRow(valueRowNum).getCell(i);
			 if(cellValue!=null){
				switch (cellValue.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_STRING;
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_BOOLEAN;
						break;
					case Cell.CELL_TYPE_NUMERIC:
						//String numericValue = cellValue.getNumericCellValue()+"";
						DataFormatter formatter = new DataFormatter();
						String numericValue = formatter.formatCellValue(cellValue);
						if(isParsableAsInteger(numericValue)) {
							jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_INTEGER;
						}
						else if(isParsableAsLong(numericValue)) {
							jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_LONG;
						}
						else if(isParsableAsDouble(numericValue)){
							jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DOUBLE;
						}
						else if(isDateValid(numericValue, "MM/dd/yyyy")) {
							jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DATE;
						}
						break;
					case Cell.CELL_TYPE_BLANK:
						jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING;
						break;
						// for Formula
					case Cell.CELL_TYPE_FORMULA:
						switch (cellValue.getCachedFormulaResultType()) {
						case Cell.CELL_TYPE_NUMERIC:
							//String numericValue = cellValue.getNumericCellValue()+"";
							formatter = new DataFormatter();
							numericValue = formatter.formatCellValue(cellValue);
							if(isParsableAsInteger(numericValue)) {
								jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_INTEGER;
							}
							else if(isParsableAsLong(numericValue)) {
								jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_LONG;
							}
							else if(isParsableAsDouble(numericValue)){
								jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DOUBLE;
							}
							else if(isDateValid(numericValue, "MM/dd/yyyy")) {
								jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DATE;
							}
							break;
						case Cell.CELL_TYPE_STRING:							
							jsonStringValue = EXCEL_COLUMN_FIELD_TYPE_STRING;
							break;
						}
						break;	
				 }
				jsonHeaderTypeList.add(jsonStringValue);
			 }
			 else {
				 jsonStringValue = EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING;
				 jsonHeaderTypeList.add(jsonStringValue);
			 }
		}
	}
	
	private void generateExcelBeanAndCustomHeaderPivotFromExcel(Sheet currentSheet,int cellCounter,String jsonKey,List<String> jsonHeaderList,List<String> jsonHeaderTypeList,List<String> jsonHeaderValidJavaVariableList, List<String> ignoreHeaderList) {
		Iterator<Row> rowIterator = currentSheet.iterator();
		int rowCounter = 0;
		int headerRowNum=0;		
		if(getHeaderRowNumber()!=0){
			headerRowNum=getHeaderRowNumber()-1;
		}
		rowCounter = headerRowNum;
		while (rowIterator.hasNext()) {
			boolean isKey = true;
			boolean isValue = false;
			rowIterator.next();
			int columnHeader = toIndentNumber(this.headerColumn) - 1;
			for(int i=columnHeader;i<=columnHeader+1;i++){
				if(currentSheet.getRow(rowCounter)==null) {
					continue;
				}
				if (currentSheet.getRow(rowCounter).getCell(i) == null) {
					if(isValue){
						 String jsonStringValue = EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING;
						 jsonHeaderTypeList.add(jsonStringValue);
					}
					continue;
				}
				if (isKey) {											
					if (currentSheet.getRow(rowCounter).getCell(i) == null) {
						continue;
					}
					jsonKey = currentSheet.getRow(rowCounter).getCell(i).getStringCellValue();
					jsonKey = cleanJsonKey(jsonKey);
					jsonKey = jsonKey.trim();
					if(ignoreHeaderList!=null && !ignoreHeaderList.isEmpty() && ignoreHeaderList.contains(jsonKey)) {
						jsonHeaderValidJavaVariableList.add(IGNORE_LIST_ITEM);
						jsonHeaderList.add(IGNORE_LIST_ITEM);
						jsonHeaderTypeList.add(IGNORE_LIST_ITEM);
						break;						
					}
					if ("".equals(jsonKey)) {
						jsonHeaderValidJavaVariableList.add(IGNORE_LIST_ITEM);
						jsonHeaderList.add(IGNORE_LIST_ITEM);
						jsonHeaderTypeList.add(IGNORE_LIST_ITEM);
						break;
					}
					else {
						isKey = false;
						isValue = true;
					}
					
					jsonKey = cleanJsonKey(jsonKey);
					jsonKey = jsonKey.trim();
					jsonHeaderList.add(jsonKey);
					jsonKey = toPascalCase(jsonKey);
					jsonKey = deleteJavaInValidVariables(jsonKey);					
					jsonHeaderValidJavaVariableList.add(jsonKey);

				} else if (isValue) {
					isValue = false;
					isKey = true;
					 String jsonStringValue = null;
					 Cell cellValue=currentSheet.getRow(rowCounter).getCell(i);
						if(cellValue!=null){
							switch (cellValue.getCellType()) {
								case Cell.CELL_TYPE_STRING:
									jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_STRING;
									break;
								case Cell.CELL_TYPE_BOOLEAN:
									jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_BOOLEAN;
									break;
								case Cell.CELL_TYPE_NUMERIC:
									//String numericValue = cellValue.getNumericCellValue()+"";
									DataFormatter formatter = new DataFormatter();
									String numericValue = formatter.formatCellValue(cellValue);
									if(isParsableAsInteger(numericValue)) {
										jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_INTEGER;
									}
									else if(isParsableAsLong(numericValue)) {
										jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_LONG;
									}
									else if(isParsableAsDouble(numericValue)){
										jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DOUBLE;
									}
									else if(isDateValid(numericValue, "MM/dd/yyyy")) {
										jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DATE;
									}
									break;
								case Cell.CELL_TYPE_BLANK:
									jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_NOT_AVAILABLE_DEFAULT_STRING;
									break;
									// for Formula
								case Cell.CELL_TYPE_FORMULA:
									switch (cellValue.getCachedFormulaResultType()) {
									case Cell.CELL_TYPE_NUMERIC:
										//String numericValue = cellValue.getNumericCellValue()+"";
										formatter = new DataFormatter();
										numericValue = formatter.formatCellValue(cellValue);
										if(isParsableAsInteger(numericValue)) {
											jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_INTEGER;
										}
										else if(isParsableAsLong(numericValue)) {
											jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_LONG;
										}
										else if(isParsableAsDouble(numericValue)){
											jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DOUBLE;
										}
										else if(isDateValid(numericValue, "MM/dd/yyyy")) {
											jsonStringValue=EXCEL_COLUMN_FIELD_TYPE_DATE;
										}
										break;
									case Cell.CELL_TYPE_STRING:							
										jsonStringValue = EXCEL_COLUMN_FIELD_TYPE_STRING;
										break;
									}
									break;
							 }
							jsonHeaderTypeList.add(jsonStringValue);
				}
			  }
			}							
			rowCounter++;
		}	
	}
	
	public void copySheetStyle(Workbook newWorkbook, String sheetName, Sheet newSheet, Cell cell, int rownum, int cellnum) {
		try {
			Workbook workbookTemplate = getWorkbookTemplate(newWorkbook);
			Sheet oldSheet = workbookTemplate.getSheet(sheetName);
			copyRowStyle(newWorkbook,oldSheet, newSheet, rownum, rownum, cellnum, cellnum);
		} catch (IOException ioe) {
			logger.error("ExcelProcessorUtil>>copySheetStyle>>caught IOException:"+ioe);
		}
	}
	
	public Sheet getExcelTemplateSheet(String sheetName,Workbook newWorkbook) throws FileNotFoundException, IOException {
		newWorkbook = getWorkbookTemplate(newWorkbook);
		Sheet oldSheet = newWorkbook.getSheet(sheetName);
		return oldSheet;
	}
		
	public void copyRowStyle(Workbook workbook,Sheet oldSheet,Sheet newSheet, int oldRowNum, int newRowNum,int oldCellNum,int newCellNum) {
		Row newRow = newSheet.getRow(newRowNum);
		if(getCopyStyleFromRow()!=-1) {
			oldRowNum = getCopyStyleFromRow();
		}
		Row oldRow = oldSheet.getRow(oldRowNum);
		if(newRow!=null && oldRow!=null) {
			newRow.setHeight(oldRow.getHeight());
			Cell oldCell = oldRow.getCell(oldCellNum);
			Cell newCell = newRow.getCell(newCellNum);
			copyCellStyle(oldCell, newCell);
		}
	}
	
	public void copyCellStyle(Cell oldCell, Cell newCell){
	   if(oldCell!=null && newCell!=null) {
	       CellStyle newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
	       newCellStyle.cloneStyleFrom(oldCell.getCellStyle());       
	       newCell.setCellStyle(newCellStyle);

	       // If there is a cell comment, copy
	       if (oldCell.getCellComment() != null) {
	           newCell.setCellComment(oldCell.getCellComment());
	       }

	       // If there is a cell hyperlink, copy
	       if (oldCell.getHyperlink() != null) {
	           newCell.setHyperlink(oldCell.getHyperlink());
	       } 
	   }	      
	}

	public void copySheets(Sheet newSheet, Sheet oldSheet){
        copySheets(newSheet, oldSheet, true);
    }
     
    public void copySheets(Sheet newSheet, Sheet oldSheet, boolean copyStyle){
        int maxColumnNum = 0;
        for (int i = oldSheet.getFirstRowNum(); i <= oldSheet.getLastRowNum(); i++) {
            Row srcRow = oldSheet.getRow(i);
            Row destRow = newSheet.createRow(i);
            if (srcRow != null) {
                copyRow(oldSheet, newSheet, srcRow, destRow);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            newSheet.setColumnWidth(i, oldSheet.getColumnWidth(i));
        }
    }
 
    public void copyRow(Sheet srcSheet, Sheet destSheet, Row srcRow, Row destRow) {
        Set<CellRangeAddress> mergedRegions = new TreeSet<CellRangeAddress>();
        destRow.setHeight(srcRow.getHeight());
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
            Cell oldCell = srcRow.getCell(j);
            Cell newCell = destRow.getCell(j);
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                copyCell(oldCell, newCell);
                CellRangeAddress mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(), (short)oldCell.getColumnIndex());
                if (mergedRegion != null) {
                    CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastRow(), mergedRegion.getLastColumn());
                    if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
                        mergedRegions.add(newMergedRegion);
                        destSheet.addMergedRegion(newMergedRegion);
                    }
                }
            }
        }
         
    }
     
    public void copyCell(Cell oldCell, Cell newCell) {
        copyCellStyle(oldCell, newCell);
        switch(oldCell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                newCell.setCellType(Cell.CELL_TYPE_BLANK);
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        } 
    }
    
    private void setUserDefinedCellStyle(Cell cell, Workbook workbook, String jsonKey, Map<String, ExcelHeader> fieldExcelHeaderMap) {
    	if(jsonKey!=null && fieldExcelHeaderMap.containsKey(jsonKey)) {
    		ExcelHeader excelHeader = fieldExcelHeaderMap.get(jsonKey);
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_TOP, excelHeader.borderStyle());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_RIGHT, excelHeader.borderStyle());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_BOTTOM, excelHeader.borderStyle());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BORDER_LEFT, excelHeader.borderStyle());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.TOP_BORDER_COLOR, excelHeader.borderColor().getIndex());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.RIGHT_BORDER_COLOR, excelHeader.borderColor().getIndex());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.BOTTOM_BORDER_COLOR, excelHeader.borderColor().getIndex());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.LEFT_BORDER_COLOR, excelHeader.borderColor().getIndex());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.FILL_FOREGROUND_COLOR, excelHeader.foregroundColor().getIndex());
    		CellUtil.setCellStyleProperty(cell, workbook, CellUtil.FILL_PATTERN, excelHeader.fillPattern());
    	}
    }
    
    public static  String advancedTrim(String string){
    	if(string==null) {
    		return null;
    	}
    	char[] value = string.toCharArray();
    	int i = value.length;
    	int j = 0;
        char[] arrayOfChar = value;
        while ((j < i) && (arrayOfChar[j] <= ' '||arrayOfChar[j] == '\u00A0')) {
        	j++;
        }
        while ((j < i) && (arrayOfChar[(i - 1)] <= ' ' || arrayOfChar[(i - 1)] == '\u00A0')) {
        	i--;
        }
        return (j > 0) || (i < value.length) ? string.substring(j, i) : string;
    }
    
	public Workbook getWorkbookTemplate(Workbook newWorkbook) throws FileNotFoundException, IOException {
		Workbook workbookTemplate = null;
		String existingExcelTemplate = getExcelTemplate().getName();
		if(existingExcelTemplate.endsWith(EXCEL_FILE_TYPE_XLSX)){
			workbookTemplate = new XSSFWorkbook(new FileInputStream(getExcelTemplate()));
		}
		else if(existingExcelTemplate.endsWith(EXCEL_FILE_TYPE_XLS)) {
			workbookTemplate = new HSSFWorkbook(new FileInputStream(getExcelTemplate()));
		}
		return workbookTemplate;
	}
	
	private Set<String> findDuplicates(List<String> listContainingDuplicates) { 
	  final Set<String> setToReturn = new HashSet<>(); 
	  final Set<String> tempSet = new HashSet<>();
	  for (String duplicateListDataItr : listContainingDuplicates) {
	   if (!tempSet.add(duplicateListDataItr)) {
	    setToReturn.add(duplicateListDataItr);
	   }
	  }
	  return setToReturn;
	}
		
	private boolean isParsableAsInteger(final String s) {
	    try {
	    	Integer.valueOf(s);
	        return true;
	    } catch (NumberFormatException numberFormatException) {
	        return false;
	    }
	}
	
	private boolean isParsableAsLong(final String s) {
	    try {
	        Long.valueOf(s);
	        return true;
	    } catch (NumberFormatException numberFormatException) {
	        return false;
	    }
	}

	private boolean isParsableAsDouble(final String s) {
	    try {
	        Double.valueOf(s);
	        return true;
	    } catch (NumberFormatException numberFormatException) {
	        return false;
	    }
	}
	
	private boolean isDateValid(String dateToValidate, String dateFromat){
		
		if(dateToValidate == null){
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);
		
		try {			
			//if not valid, it will throw ParseException
			sdf.parse(dateToValidate);
			//System.out.println(date);		
		} catch (ParseException e) {			
			//e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
    public CellRangeAddress getMergedRegion(Sheet sheet, int rowNum, short cellNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);
            if (merged.isInRange(rowNum, cellNum)) {
                return merged;
            }
        }
        return null;
    }
 
    private boolean isNewMergedRegion(CellRangeAddress newMergedRegion, Collection<CellRangeAddress> mergedRegions) {
        return !mergedRegions.contains(newMergedRegion);
    }
    
    public  int toIndentNumber(String name) {
        int number = 0;
        for (int i = 0; i < name.length(); i++) {
            number = number * 26 + (name.charAt(i) - ('A' - 1));
        }
        return number;
    }

    public  String toIndentName(int number) {
        StringBuilder sb = new StringBuilder();
        while (number-- > 0) {
            sb.append((char)('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }
	
	public double getRowHeight() {
		return rowHeight;
	}
	
	public void setRowHeight(double rowHeight) {
		this.rowHeight = rowHeight;
	}
	
	public Map<String,String> getCustomHeader() {
		return customHeader;
	}
	
	public void setCustomHeader(Map<String,String> customHeader) {
		this.customHeader = customHeader;
	}
	
	public short getFormatDateIndex() {
		return formatDateIndex;
	}

	public void setFormatDateIndex(short formatDateIndex) {
		this.formatDateIndex = formatDateIndex;
	}
	
	public int getHeaderRowNumber() {
		return headerRowNumber;
	}
	
	public void setHeaderRowNumber(int headerRowNumber) {
		this.headerRowNumber = headerRowNumber;
	}
					
    public boolean hasIgnoreFormatting() {
		return ignoreFormatting;
	}
	
    public void setIgnoreFormatting(boolean ignoreFormatting) {
		this.ignoreFormatting = ignoreFormatting;
	}
    
	public String getHeaderColumn() {
		return headerColumn;
	}
	
	public void setHeaderColumn(String headerColumn) {
		this.headerColumn = headerColumn;
	}
	public boolean isWrapTexting() {
		return wrapTexting;
	}
	public void setWrapTexting(boolean wrapTexting) {
		this.wrapTexting = wrapTexting;
	}
	public boolean isHasExcelTemplate() {
		return hasExcelTemplate;
	}
	public void setHasExcelTemplate(boolean hasExcelTemplate) {
		this.hasExcelTemplate = hasExcelTemplate;
	}
	public boolean isHasStyleTemplate() {
		return hasStyleTemplate;
	}
	public void setHasStyleTemplate(boolean hasStyleTemplate) {
		this.hasStyleTemplate = hasStyleTemplate;
	}
	public File getExcelTemplate() {
		return excelTemplate;
	}
	public void setExcelTemplate(File excelTemplate) {
		this.excelTemplate = excelTemplate;
	}
	public boolean hasForceAutoSizing() {
		return forceAutoSizing;
	}
	public void setForceAutoSizing(boolean forceAutoSizing) {
		this.forceAutoSizing = forceAutoSizing;
	}
	public ExcelValidatorContext getExcelValidatorContext() {
		return excelValidatorContext;
	}
	public void setExcelValidatorContext(ExcelValidatorContext excelValidatorContext) {
		this.excelValidatorContext = excelValidatorContext;
	}
	public List<String> getIgnoreHeaderList() {
		return ignoreHeaderList;
	}
	public void setIgnoreHeaderList(List<String> ignoreHeaderList) {
		this.ignoreHeaderList = ignoreHeaderList;
	}
	public boolean isIgnoreExcelAnnotation() {
		return ignoreExcelAnnotation;
	}
	public void setIgnoreExcelAnnotation(boolean ignoreExcelAnnotation) {
		this.ignoreExcelAnnotation = ignoreExcelAnnotation;
	}
	public boolean isEnableHBMGenerator() {
		return enableHBMGenerator;
	}
	public void setEnableHBMGenerator(boolean enableHBMGenerator) {
		this.enableHBMGenerator = enableHBMGenerator;
	}
	public boolean isEnableTableGenerator() {
		return enableTableGenerator;
	}
	public void setEnableTableGenerator(boolean enableTableGenerator) {
		this.enableTableGenerator = enableTableGenerator;
	}
	public List<String> getToExcelOrderedFieldNameList() {
		return toExcelOrderedFieldNameList;
	}
	public void setToExcelOrderedFieldNameList(List<String> toExcelOrderedFieldNameList) {
		this.toExcelOrderedFieldNameList = toExcelOrderedFieldNameList;
	}
	public Map<String,List<String>> getToExcelOrderedFieldNameMap() {
		return toExcelOrderedFieldNameMap;
	}
	public void setToExcelOrderedFieldNameMap(Map<String,List<String>> toExcelOrderedFieldNameMap) {
		this.toExcelOrderedFieldNameMap = toExcelOrderedFieldNameMap;
	}

	public int getCopyStyleFromRow() {
		return copyStyleFromRow;
	}

	public void setCopyStyleFromRow(int copyStyleFromRow) {
		this.copyStyleFromRow = copyStyleFromRow;
	}

	public boolean isCopyHeaderStyle() {
		return copyHeaderStyle;
	}

	public void setCopyHeaderStyle(boolean copyHeaderStyle) {
		this.copyHeaderStyle = copyHeaderStyle;
	}

	public Map<String,String> getDynamicFieldHeaderMap() {
		return dynamicFieldHeaderMap;
	}

	public void setDynamicFieldHeaderMap(Map<String,String> dynamicFieldHeaderMap) {
		this.dynamicFieldHeaderMap = dynamicFieldHeaderMap;
	}

	public Set<String> getDynamicFields() {
		return dynamicFields;
	}

	public void setDynamicFields(Set<String> dynamicFields) {
		this.dynamicFields = dynamicFields;
	}

	public Map<String,Set<String>> getDynamicFieldMap() {
		return dynamicFieldMap;
	}

	public void setDynamicFieldMap(Map<String,Set<String>> dynamicFieldMap) {
		this.dynamicFieldMap = dynamicFieldMap;
	}
	
	public List<? extends Object> getMultiOrientedExcelList() {
		return multiOrientedExcelList;
	}

	public void setMultiOrientedExcelList(List<? extends Object> multiOrientedExcelList) {
		this.multiOrientedExcelList = multiOrientedExcelList;
	}
	
}
