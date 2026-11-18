# Released Excel Processor v2

## Highlights:
```
* Easy annotation based excel read and write operation.

* Predefined validations right out of the box i.e. required, minItems, maxItems, email, numeric etc.

* Supports dynamic validations using conditional, customTask(s).

* Supports AllowedValues annotation for set of valid values.

* Can be used with any framework of Java like Spring etc.

* A user validator map, user defined message set, valid value data set can be configured using builder pattern.

*  Output of the validation contains sheet object with row and column details to rightly identify the issues.~~~~
```

## Integration steps:
> 1. Add Maven dependency

```java
<dependency>
    <groupId>com.github.salilvnair</groupId>
    <artifactId>excelprocessor</artifactId>
    <version>4.1.0</version>
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


```java
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.test.sheet.CollegeSheet;

import java.io.File;
import java.util.List;

public class ExcelProcessorTestSuite {
     public void read() {
          ExcelSheetReader reader = ExcelSheetReaderFactory.generate();

          //the basic reader.read() expects 2 arguments 
          // 1st one is the User defined Sheet bean class which has to extend BaseSheet. 
          // 2nd argument is the SheetContext

          //SheetContext can be build using the ExcelSheetContext.ExcelSheetContextBuilder

          ExcelSheetContext sheetContext = ExcelSheetContext.builder()
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

> For detailed usage check the file [ExcelProcessorTestSuite.java](src/test/java/com/github/salilvnair/excelprocessor/v2/test/archived/sheet/ExcelProcessorTestSuite.java)

## Writing large files

### Row-at-a-time streaming (recommended for big exports)

`ExcelSheetWriterFactory.openStreaming(...)` writes XLSX rows as they are produced. Rows are never
retained, so heap use is bounded by the SXSSF row window (`@Sheet(streamingRowWindowSize = 100)`)
no matter how many rows are written. Always use try-with-resources: SXSSF temp files are released
on success and on failure.

```java
try (StreamingExcelSheetWriter writer = ExcelSheetWriterFactory.openStreaming(ExcelSheetContext.builder().build())) {
    writer.beginSheet(ReportSheet.class, orderedHeaders);  // dynamic headers, frozen for the sheet
    // or: writer.beginSheet(FixedHeaderSheet.class);       // headers from @Cell fields
    ReportSheet row;
    while ((row = source.next()) != null) {
        writer.writeRow(row);
    }
    writer.finish(outputStream);                            // or writer.finish(path)
}
```

Supported: horizontal sheets with fixed or dynamic headers, header/data cell styles, style tasks,
hyperlinks and pictures. Not supported: vertical, sectional, merged-header and
user-defined-template sheets (an exception explains this).

Write straight to a file or a file-backed stream. Avoid `ByteArrayOutputStream#toByteArray()`
for large files: it puts the whole file on the heap.

### List based API

| `@Sheet` attributes (horizontal, XLSX)              | Writer used                                  |
|-----------------------------------------------------|----------------------------------------------|
| `dynamicHeaders` + `userDefinedTemplate`            | `UserDefinedTemplateDynamicHorizontalSheetWriter` |
| `dynamicHeaders` + `streamingWorkbook`              | `StreamingDynamicHorizontalSheetWriter` (SXSSF) |
| `dynamicHeaders`                                    | `DynamicHorizontalSheetWriter`               |
| `userDefinedTemplate`                               | `UserDefinedTemplateHorizontalSheetWriter`   |
| `streamingWorkbook`                                 | `StreamingHorizontalSheetWriter` (SXSSF)     |
| none                                                | `HorizontalSheetWriter`                      |

* `writer.write(list, context)` / `writer.write(list, context, outputStream)` own the workbook and
  always dispose SXSSF temp files.
* `writer.workbook(list, context)` hands the workbook to you: write it, then call
  `ExcelSheetWriter.dispose(workbook)` in a `finally` block.
* With the list API the list itself stays on the heap; use the streaming API above when that matters.

### Cell styles

Styles are cached per workbook: every distinct style is created once and shared by all cells that
use it, so the workbook style count no longer grows with the number of cells. (XLSX allows at most
64,000 styles.) Shared styles must not be mutated. Style tasks that change
`context.rowCell().getCellStyle()` are given a private copy, which is then deduplicated. Tasks
that build their own style should cache it with `context.addDataCellStyle(key, style)` rather
than calling `createCellStyle()` per cell.

## Reading large files

`ExcelSheetReaderFactory.openStreaming(file, context)` pulls the sheet XML with StAX straight from
the file. POI's in-memory model is never built, so memory stays flat however many rows the sheet
has. It also runs about 2.5–3.5x faster than `ExcelSheetReader.read(...)`. Mapped objects
(fields, `cells()`, `sheetHeaders()`, `extract()` for `DynamicHeaderSheet`) are the same as with
the in-memory reader.

```java
try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file, context);
     Stream<OrderSheet> rows = reader.stream(OrderSheet.class)) {   // or DynamicHeaderSheet subclasses
    rows.forEach(this::process);
}
// or: long n = reader.forEach(OrderSheet.class, this::process);
```

Supported: horizontal fixed-header and dynamic-header XLSX sheets, header/value row and column
ranges, ignored headers/columns, duplicate headers and fill colours. Formula cells return their
cached result (as Excel saved it). Not supported: XLS, vertical, sectional and merged-header sheets.
Validation (`ExcelSheetReader.validate`) still needs the rows in a list; use `reader.readAll(...)`
for that.

Approximate timings for 50,000 rows × 10 columns on one machine:

| Operation | 4.0.6 | Now: list API | Now: streaming API |
|-----------|-------|---------------|--------------------|
| Write | 4.4 s | 3.5 s | 1.3 s |
| Read, fixed headers | 2.5 s | 2.0 s | 0.9 s |
| Read, dynamic headers | 2.3 s | 1.8 s | 0.7 s |

`ExcelSheetReaderFactory.generate(true, batchSize)` (concurrent read) shares one POI workbook
between threads and is not faster than a normal read. Use the streaming reader for speed.

## New in 4.2.0 (not released yet)

The development version is `4.2.0-SNAPSHOT`; 4.1.0 is the current release.

### Validation while streaming

`StreamingExcelSheetReader.validate(...)` runs the `@CellValidation` / `@AllowedValues` rules (or
`headerKeyedCellValidationInfo` for dynamic sheets) on each row as it is read. Rows are never
collected; only the values of `unique` columns are kept, to find duplicates. The handler
receives every row with its messages, so valid rows can be saved straight away.

```java
try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openStreaming(file, context)) {
    ValidationReport report = reader.validate(OrderSheet.class, (row, messages) -> {
        if (messages.isEmpty()) repository.save(row);
    });
    report.rowsRead(); report.rowsWithErrors(); report.messages();   // row + column per message
}
```

`unique` is checked against the rows read so far, so the second and later occurrences of a value
are reported. Custom validators see the current row only; `getCurrentSheet()` is null.

### Annotated error workbook

```java
ExcelErrorReport.of(uploadedFile, report)          // or a List<CellValidationMessage>
        .highlight(IndexedColors.ROSE)              // null: no fill
        .comments(true)                             // a cell comment with the messages
        .errorsSheet("Errors")                      // summary sheet with links; null leaves it out
        .writeTo(outputStream);
```

The uploaded file is not changed. Each distinct original style gets one highlighted copy, so the
style count stays small however many cells fail.

### Sheet options when writing

```java
@Sheet(value = "Orders", freezeHeader = true, autoFilter = true, autoSizeColumns = true,
       headerRowHeight = 24, allowedValuesDropdown = true)
```

* `allowedValuesDropdown` turns `@AllowedValues` values, a `dataSetKey` from the context's
  `validValuesDataSet`, an int `range` and enum fields into Excel dropdowns (or number limits) on
  the data rows. A blank template therefore already offers the valid choices. Lists over 255
  characters, or values containing commas, are stored on a hidden `ExcelProcessorLists` sheet.
  Dynamic headers use `headerKeyedCellValidationInfo`.
* All options work with the list writers and the streaming writer; `autoSizeColumns` measures
  every row written, including rows SXSSF has already flushed.

### Native conditional formatting

```java
@ConditionalFormat(operator = Operator.LESS_THAN, value = "0", fontColor = IndexedColors.RED, bold = true)
@ConditionalFormat(formula = "AND({cell}<>\"\",{cell}<TODAY())", backgroundColor = IndexedColors.ROSE)
@Cell("Due date")
private Date dueDate;
```

Each annotation becomes one Excel rule for the whole column. Excel evaluates it, so it costs
nothing per row, works with streaming and stays correct when values are edited. (`@DataCellStyle`
conditional styles run Java code for every cell instead.)

### More types on read, enums and records

Fields can now be `LocalDate`, `LocalDateTime`, `LocalTime`, `Instant`, `BigDecimal`,
`BigInteger`, primitives, `Boolean` (`yes/no`, `y/n`, `1/0`, `true/false`), `Character` and enums.
Text cells are parsed too: `"1,200"` into an `int`, or `"2026-12-31"` into a `LocalDate`. Enums
match by name (ignoring case, with spaces treated as `_`) or by an `@ExcelValue` label, and are
written with their first label:

```java
enum Status { @ExcelValue("In progress") IN_PROGRESS, @ExcelValue({"Done", "Closed"}) DONE }
```

Records can be read with the streaming reader:

```java
@Sheet("Orders")
record OrderRow(@Cell("Order Id") String id, @Cell("Amount") BigDecimal amount, @Cell("Qty") int qty) {}

try (Stream<OrderRow> rows = reader.streamRecords(OrderRow.class)) { ... }
```

### Workbook outline

```java
WorkbookOutline outline = ExcelSheetReaderFactory.outline(file);   // XLSX, XLS, CSV, TSV
SheetOutline orders = outline.sheet("Orders");
orders.headers();                             // [Order Id, Customer, Amount, ...]
orders.dataRows();                            // all non-empty rows
orders.column("Amount").get().type();         // INTEGER, DECIMAL, DATE, BOOLEAN, TEXT or BLANK
```

No annotated class is needed, so upload screens can show a column-mapping step first. XLSX and
CSV are streamed. Types are inferred from the first 1,000 rows (`OutlineOptions.sampleRows`).

### CSV and TSV with the same classes

```java
try (StreamingExcelSheetReader reader = ExcelSheetReaderFactory.openCsv(file, CsvFormat.csv(), context)) {
    reader.stream(OrderSheet.class) ... ;     // stream, readAll, validate, streamRecords
}
try (CsvSheetWriter writer = ExcelSheetWriterFactory.openCsv(outputStream, CsvFormat.tsv())) {
    writer.begin(OrderSheet.class);           // or begin(DynamicSheet.class, headers)
    rows.forEach(writer::writeRow);
}
```

RFC 4180 quoting, a UTF-8 BOM and CR/LF line endings are handled. By default values are text and
are converted to the field type; `withInferTypes(true)` makes numbers and booleans typed, as XLSX
cells are. The writer prefixes text starting with `= + - @` with `'` so spreadsheet programs do not
run it as a formula, and the reader removes the prefix again (`withEscapeFormulas(false)` turns
both off).

### Password-protected workbooks

```java
ExcelSheetContext.builder().password("secret")
```

On read, encrypted XLSX and XLS files open in the list reader, the streaming reader and the
outline. On write, the list and streaming writers encrypt the output: XLSX with agile (AES)
encryption, which is what Excel's "Encrypt with Password" uses, and XLS with RC4. The streaming
reader decrypts to a temporary file, which it deletes on `close()`. An encrypted XLSX is assembled
in memory once before it is written.

### New artifacts

**`excelprocessor-spring-boot-starter`** (Spring Boot 4.x):

```xml
<dependency>
    <groupId>com.github.salilvnair</groupId>
    <artifactId>excelprocessor-spring-boot-starter</artifactId>
    <version>4.2.0-SNAPSHOT</version>
</dependency>
```

* Auto-configures `ExcelSheetReader`, `ExcelSheetWriter` and `ExcelProcessorTemplate`. Its
  contexts resolve `excelTaskBeanName` / `excelTaskValidatorBeanName` and bean-named tasks from
  the Spring context.
* A controller can return `ExcelExport`. Rows are written straight to the response, XLSX or CSV,
  so neither the rows nor the file are held in memory. This also works in `@RestController`s.

  ```java
  @GetMapping("/orders.xlsx")
  ExcelExport<OrderSheet> export() {
      return ExcelExport.of(OrderSheet.class, orderRepository::streamAll).fileName("orders.xlsx");
  }
  ```
* `template.importRows(upload.getInputStream(), upload.getOriginalFilename(), OrderSheet.class, handler)`
  validates an upload row by row and returns a `ValidationReport`. `template.outline(...)`
  describes an upload.
* Properties: `excelprocessor.resolve-beans`, `excelprocessor.fail-on-task-exceptions`,
  `excelprocessor.web-exports`.

**`excelprocessor-maven-plugin`** generates `@Sheet` classes from sample files in
`src/main/excel` (XLSX, XLS, CSV, TSV) during `generate-sources`:

```xml
<plugin>
    <groupId>com.github.salilvnair</groupId>
    <artifactId>excelprocessor-maven-plugin</artifactId>
    <version>4.2.0-SNAPSHOT</version>
    <executions><execution><goals><goal>generate-sheets</goal></goals></execution></executions>
    <configuration>
        <packageName>com.acme.sheets</packageName>
        <!-- optional: <lombok>true</lombok> <headerRow>3</headerRow> <sheets><sheet>Orders</sheet></sheets> -->
    </configuration>
</plugin>
```

Field names come from the headers, and field types are inferred from the sampled rows. Repeated
headers get `@Cell(column = ...)`. Unchanged files are not rewritten.

### Upgrading from 4.1.0

* **`unique` validation is fixed.** 4.0.x and 4.1.0 compared each row with itself, so every
  non-empty value was reported as a duplicate. Now only values that occur more than once are
  reported.
* **Dependencies:** `org.json` and `commons-logging` were removed, because the library never used
  them. Add them yourself if your code relied on getting them through excelprocessor. `gson` is
  now optional: `CellValidationMessage.toString()` builds the same JSON without it, and only
  `ReflectionUtil.toEntity` / `toGenericListEntity` still need Gson on your classpath.
* **More values reach your fields.** Text cells in number, date, boolean and enum fields are now
  converted; 4.1.0 left those fields unset. Enum values are written with their `@ExcelValue`
  label.
* The vertical, sectional and merged-header readers are still part of the core jar. Moving them
  into a separate module would break the 4.x API, so it waits for a 5.0 release.

### Building all modules

```
mvn -f pom-all.xml verify          # core, starter and plugin in one reactor
```

The starter and the plugin can also be built on their own once the core is installed
(`mvn install`). Publishing signs with GPG; add `-Dgpg.skip` for local installs.

## Upgrading from 4.0.6

* **Apache POI 6.0.0.** The library still runs on POI 5.5.1, and the test suite passes on both.
  If your project declares POI itself, keep `poi` and `poi-ooxml` on the same version: mixing
  5.x and 6.x jars fails at runtime with errors such as `NoSuchMethodError`.
* **Java 17**, unchanged from 4.0.6.
* **Blank template rows are skipped.** A data row whose formulas all evaluate to blank (for
  example `=IF(B2="","",...)` filled down an empty template) now counts as empty and is not read.
  4.0.6 returned these rows as data rows full of empty values.
* **Values are written for more types.** Boolean, `LocalDate`, `LocalDateTime` and other
  `Number` field values are now written; 4.0.6 left those cells blank.
* **Styles are per field.** Fixed-column sheets now apply each field's own `@DataCellStyle`. In
  4.0.6 all columns shared one style.
* **If your code calls POI directly,** check the
  [POI 6.0.0 changes](https://poi.apache.org/changes.html):
  * Some deprecated methods were removed: `AddDimensionedImage`, `hasOOXMLHeader`, `getCodepoints`.
  * `getCachedResultType` returns an enum instead of an `int`.
  * `DataFormatter` now formats formula cells by their cached value.
  * Formula evaluation is stricter: several functions now return error values instead of throwing, and COUNTIF/SUMIF handle `<>` and array criteria differently.
  * ZIP safety limits are tighter, so unusually compressed files can be rejected when opened (tune them with `ZipSecureFile`).
  * On the Java module path, digital-signature support needs `jakarta.xml.bind-api` and `jakarta.activation-api`.

## Security notes

* Dependencies are checked against [OSV](https://osv.dev). The update to POI 6.0.0
  (log4j-api 2.26.1) and org.json 20260814 fixed GHSA-qv9r-c865-cp47 and GHSA-ghc9-756g-vrcv.
* The library no longer depends on `org.json` or `commons-logging` (neither was used), and `gson`
  is optional, so fewer transitive jars reach your application.
* CSV output escapes text that starts with `= + - @` (CSV/formula injection).
* Multi-hyperlink cells (`@Cell(hyperLink = true, multiHyperLink = true)`) escape quotes, so cell
  values cannot inject other functions into the generated `HYPERLINK` formula.
* The streaming reader uses POI's hardened StAX factory (no DTDs or external entities) and opens
  files read-only.
* `@Cell(picture = true, pictureSource = FILE_PATH)` embeds whatever local file the field value
  names. Never fill such fields from untrusted input.

### Tests

`mvn test` runs the regression suite. Large exports, concurrent exports and the read/write
benchmark are tagged `load`: `mvn test -Dsurefire.excludedGroups= -Dgroups=load`.
