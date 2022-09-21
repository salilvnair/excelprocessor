# Released Excel Processor v2

## Highlights:
```
* Easy annotation based excel read and write operation.

* Predefined validations right out of the box i.e. required, minItems, maxItems, email, numeric etc.

* Supports dynamic validations using conditional, customTask(s).

* Supports AllowedValues annotation for set of valid values.

* Can be used with any framework of Java or a Rest based Java API.

* A user validator map, user defined message set, valid value data set can be configured using builder pattern.

*  Output of the validation contains sheet object with row and column details to rightly identify the issues.~~~~
```

## Integration steps:
> 1. Add Maven dependency

```java
<dependency>
    <groupId>com.github.salilvnair</groupId>
    <artifactId>excelprocessor</artifactId>
    <version>2.0.2</version>
</dependency>
```

> 2. Annotate the user defined class using `@Sheet` and it should be extending abstract class named `BaselSheet`

```java
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

@Sheet("School")
public class SchoolSheet extends BaseSheet {
   .......
}
```

> 3. Set it as required, conditional, numeric, email etc.

```java
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;

@Sheet("School")
public class SchoolSheet extends BaseSheet {

     @CellValidation(required = true, numeric = true)
     @Cell("School Id") // this value should match with the Excel sheets header value in the sheet
     private String schoolId;

     @CellValidation(conditional = true, condition = "validateName")
     @Cell("Name")
     private String name;

     
     @CellValidation(email = true)
     @Cell("Email Id")
     private String email;
     
     .......
}
```

> 4. Call the read using **_ExcelSheetReader_** which can be generated using **_ExcelSheetReaderFactory_**.
> </br></br> **_ExcelSheetReaderFactory_** can generate a non thread based Sheet Reader 
     > or a thread based Sheet Reader by passing an optional flag into the generate method.

```java
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet;

import java.io.File;
import java.util.List;

public class ExcelProcessorTestSuite {
     public void read() {
          ExcelSheetReader reader = ExcelSheetReaderFactory.generate();// single threaded Sheet Reader
          ExcelSheetReader reader = ExcelSheetReaderFactory.generate(true);// multi threaded Sheet Reader

          //the basic reader.read() expects 2 arguments 
          // 1st one is the User defined Sheet bean class which has to extend BaseSheet. 
          // 2nd argument is the SheetContext

          //SheetContext can be build using the ExcelSheetContext.ExcelSheetContextBuilder

          ExcelSheetContext.ExcelSheetContextBuilder builder = ExcelSheetContext.builder();
          ExcelSheetContext sheetContext = builder
                                           .excelFile(new File("ExcelProcessorTest.xlsx"))
                                           .build();  
          
          //User defined map can be passed into the builder which can be later used in customTask(s) or in conditional validators.
          Map<String,Object> validatorMap  = new HashMap<>();
          validatorMap.put("key", "value");
          
          ExcelSheetContext sheetContext = builder
                                           .excelFile(new File("ExcelProcessorTest.xlsx"))
                                           .userValidatorMap(validatorMap)
                                           .build();
          
          List<CollegeSheet> collegeSheets = reader.read(CollegeSheet.class, sheetContext);

     }
}






```


## Complete Usage:
```java
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.task.CollegeSheetTaskValidator;

@Sheet(
        value="College",
        excelTaskValidator = CollegeSheetTaskValidator.class
)
public class CollegeSheet extends BaseSheet {
     @Cell("Name")
     private String name;
     
     @CellValidation(customTask = "defaultUniversity")
     @Cell("University")
     private String university;
     
     @Cell("State")
     private String state;
     
     @CellValidation(conditional = true, condition = "shouldBeGreaterThanZero")
     @Cell("No of students")
     private Long noOfStudents;

     //getters and setters
     ......

}
```

```java
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;

public class CollegeSheetTaskValidator extends AbstractExcelTaskValidator {
     public String shouldBeGreaterThanZero(CellValidatorContext context) {
          CollegeSheet sheet = context.sheet(CollegeSheet.class);
          long noOfS = noOfS = sheet.getNoOfStudents();
          if (noOfS <= 0) {
               return "Min Students should be greater than 0";
          }
          return null;
     }
}
```

> For detailed usage check the file [ExcelProcessorTestSuite.java](src/main/java/com/github/salilvnair/excelprocessor/v2/test/sheet/ExcelProcessorTestSuite.java)
