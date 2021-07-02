# Excel Processor
POI based library which supports below features:

* Easy annotation based excel read and write operation.

* Predefined validations right out of the box i.e. required, minItems, maxItems, email, numeric etc.

* Supports dynamic validations using conditional, customTask(s).

* Supports ValidValues annotation for set of valid values.

* Can be used with any framework of Java or a Rest based Java API.

* A user validator map, user defined message set, valid value data set can be configured using builder pattern.

*  Output of the validation contains sheet object with row and column details to rightly identify the issues.


## Steps:
> 1. Add Maven dependency

```java
<dependency>
    <groupId>com.github.salilvnair</groupId>
    <artifactId>excelprocessor</artifactId>
    <version>1.0.1</version>
</dependency>
```

> 2. Annotate a class or a field using  `@ExcelSheet ` , class should be either extending abstract class named `BaseExcelSheet ` or `BaseExcelValidationSheet`
```java
@ExcelSheet(value="School", hasValidation=true)
public class SchoolSheet extends BaseExcelValidationSheet {
   ....
}
```

> 3. Set it as required, conditional, numeric, email erc.

```java
@ExcelHeaderValidator(required=true, numeric=true)
@ExcelHeader("School Id") // this value should match with the excel header value in the sheet
private String schoolId;
```

```java
@ExcelHeader("Name")
@ExcelHeaderValidator(conditional=true, condition="validateName")
private String name;
```

```java
@ExcelHeader("Email Id")
@ExcelHeaderValidator(email=true)
private String email;
```

> 4. Call the processor using `ExcelProcessorBuilder`

   - Validating excel sheet

      ```java
            Map<String, Map<String, Object>>  validationMap = excelProcessorBuilder
                                                              .setExcelfile(file)
                                                              .setExcelMappingBeanClasses(SchoolSheet.class,
                                                                            CollegeSheet.class,
                                                                            EmployerSheet.class)
                                                              .validate();
     ```
                                                              
   - Read from excel sheet(s)

      ```java
      //below snippet shows how to extract data of single sheet
      
			List<SchoolSheet> sheetData = excelProcessorBuilder
                                    .setExcelfile(excelfile)
                                    .setExcelMappingBeanClasses(
                                        SchoolSheet.class,
                                        CollegeSheet.class,
                                        EmployerSheet.class
                                     )
                                    .fromExcel()
                                    .toSheetList(SchoolSheet.class);
     //below snippet shows how to extract data of multiple sheets, 
     //sheetMap will have the key as the sheet name given in the excel sheet                
     
     Map<String, List<? extends BaseExcelSheet>> sheetMap = excelProcessorBuilder
                                                            .setExcelfile(excelfile)
                                                            .setExcelMappingBeanClasses(
                                                                SchoolSheet.class,
                                                                CollegeSheet.class,
                                                                EmployerSheet.class
                                                             )
                                                            .fromExcel()                                  
                                                            .toSheetMap();

> 5. User defined map can be passed in the `JsonProcessorBuilder` which can be later used in customTask(s) or in Conditional validators.
		
```java
Map<String,Object> validatorMap  = new HashMap<>();
validatorMap.put("alumini", "Hogward");
Map<String, List<? extends BaseExcelSheet>> sheetMap = ......
                                                            .setUserValidatorMap(map)
                                                            .fromExcel()                                  
                                                            .toSheetMap();
```
  - `validateInDetail()` will provide each sheet object with validationMessage object containing sheet row and column details along with error.
```java
Map<String,Object> validatorMap  = new HashMap<>();
validatorMap.put("alumini", "Hogward");
Map<String, List<? extends BaseExcelSheet>> sheetMap = ......
                                                            .setUserValidatorMap(map)
                                                            .fromExcel() 
                                                            .validateInDetail()
                                                            .toSheetMap();
```

## Complete Usage:
```java
  @ExcelSheet(
    value="School", 
    hasValidation=true,
    customTaskValidator = SchoolSheetCustomValidatorTask.class
   )
  public class SchoolSheet extends BaseExcelValidationSheet{
      @ExcelHeaderValidator(conditional=true, condition="validateAlumini")
      @ExcelHeader("Name")
      private String name;
      @ExcelHeader(value="State")
      private String state;
      @ExcelHeader("No of students")
      private Integer noOfStudents;
      @ExcelHeader(
        value="Image",
        pictureResizeScale=-1,
        picture=true,
        pictureAnchorType = PictureAnchorType.DONT_MOVE_AND_RESIZE,
        pictureSource=PictureSourceType.BYTE_ARRAY
      )
      private Byte[] image;
     
     //getters and setters
     ........
  }
```
```java
public class SchoolSheetCustomValidatorTask extends AbstractCustomValidatorTask {
	public String validateAlumini(ValidatorContext validatorContext) {
		SchoolSheet school = (SchoolSheet) validatorContext.getBaseExcelValidationSheet();
		Map<String,Object> validatorMap = validatorContext.getUserValidatorMap();
		if(!validatorMap.isEmpty() && school.getName()!=null) {
			String alumini  = (String) validatorMap.get("alumini");
			if(!school.getName().contains(alumini)){
				return "Only "+alumini+" alumini schools are allowed";
			}
		}
		return null;
	}
}
```

> For detailed usage check the file [ExcelProcessorTestSuite.java](src/main/java/com/github/salilvnair/excelprocessor/test/ExcelProcessorTestSuite.java)
