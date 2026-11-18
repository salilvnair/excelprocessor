package com.github.salilvnair.excelprocessor.v2.processor.provider.reader;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.helper.TypeConvertor;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.ExcelInfo;
import com.github.salilvnair.excelprocessor.v2.model.ValidationReport;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.processor.service.DynamicHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.service.StaticHeaderSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelSheetValidatorUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.StreamingExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Format independent part of the streaming readers: header detection, duplicate-header keys, row
 * mapping (sheet classes and records) and validation while streaming. Subclasses only supply the
 * raw rows of a sheet through {@link #openSource(String)}.
 *
 * @author Salil V Nair
 */
public abstract class AbstractStreamingSheetReader extends BaseExcelSheetReader implements StreamingExcelSheetReader {
    protected final ExcelSheetContext sheetContext;
    private boolean closed;

    protected AbstractStreamingSheetReader(ExcelSheetContext sheetContext) {
        this.sheetContext = sheetContext == null ? ExcelSheetContext.builder().build() : sheetContext;
    }

    /** Raw rows of the named sheet, or null when the sheet does not exist. */
    abstract RowSource openSource(String sheetName);

    /** Releases the file; called once by {@link #close()}. */
    protected abstract void release();

    // BaseExcelSheetReader contract (list based API) is not used by the streaming readers
    @Override
    void read(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        throw new UnsupportedOperationException("Use stream(sheetClass)");
    }

    @Override
    ExcelInfo excelInfo(Class<? extends BaseSheet> clazz, ExcelSheetReaderContext context) {
        throw new UnsupportedOperationException("Use stream(sheetClass)");
    }

    @Override
    public <T extends BaseSheet> Stream<T> stream(Class<T> sheetClass) {
        return rows(sheetClass, sheetBinder(sheetClass));
    }

    @Override
    public <R extends Record> Stream<R> streamRecords(Class<R> recordClass) {
        Sheet sheet = sheetAnnotation(recordClass);
        if (sheet.dynamicHeaders()) {
            throw new ExcelSheetReaderException("Record " + recordClass.getName() + ": dynamicHeaders needs a DynamicHeaderSheet class");
        }
        return rows(recordClass, new RecordBinder<>(recordClass));
    }

    @Override
    public <T extends BaseSheet> ValidationReport validate(Class<T> sheetClass, BiConsumer<? super T, List<CellValidationMessage>> rowHandler) {
        Sheet sheet = sheetAnnotation(sheetClass);
        ExcelSheetReaderContext readerContext = new ExcelSheetReaderContext();
        readerContext.setSheet(sheet);
        CellValidatorContext validatorContext = new CellValidatorContext();
        validatorContext.setSheetContext(sheetContext);
        validatorContext.setReaderContext(readerContext);
        validatorContext.setUserValidatorMap(sheetContext.userValidatorMap());
        validatorContext.setValidValuesDataSet(sheetContext.validValuesDataSet());
        validatorContext.setUserDefinedMessageDataSet(sheetContext.userDefinedMessageDataSet());
        validatorContext.setHeaderKeyedCellValidationInfo(sheetContext.headerKeyedCellValidationInfo());
        validatorContext.setTaskValidatorBean(sheetContext.taskValidatorBean());
        validatorContext.setBeanResolver(sheetContext.beanFunction());
        validatorContext.setTaskMetadata(sheetContext.taskMetadata());
        validatorContext.setUniqueValues(new HashMap<>());
        ValidationReport report = new ValidationReport();
        Map<Integer, Map<String, CellInfo>> currentRowCells = readerContext.rowIndexKeyedHeaderKeyCellInfoMap();
        try (Stream<T> rows = stream(sheetClass)) {
            rows.forEach(row -> {
                currentRowCells.clear();
                currentRowCells.put(row.getRowIndex(), row.cells());
                List<CellValidationMessage> messages = new ExcelSheetValidatorUtil(validatorContext).validate(row);
                report.addRow(messages);
                rowHandler.accept(row, messages);
            });
        }
        return report;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        release();
    }

    protected boolean isClosed() {
        return closed;
    }

    private static Sheet sheetAnnotation(Class<?> sheetClass) {
        Sheet sheet = sheetClass.getAnnotation(Sheet.class);
        if (sheet == null) {
            throw new ExcelSheetReaderException(sheetClass.getName() + " is not annotated with @Sheet");
        }
        if (sheet.vertical() || sheet.sectional() || sheet.mergedHeaders()) {
            throw new ExcelSheetReaderException("Sheet '" + sheet.value() + "': vertical, sectional and mergedHeaders sheets are not supported by the streaming reader");
        }
        return sheet;
    }

    private <T> Stream<T> rows(Class<T> clazz, RowBinder<T> binder) {
        if (closed) {
            throw new IllegalStateException("Reader is closed");
        }
        Sheet sheet = sheetAnnotation(clazz);
        String sheetName = sheetContext.sheetName() != null ? sheetContext.sheetName() : sheet.value();
        RowSource source = openSource(sheetName);
        if (source == null) {
            if (!sheetContext.suppressExceptions()) {
                throw new ExcelSheetReaderException("Sheet '" + sheetName + "' is not present in the excel.");
            }
            return Stream.empty();
        }
        RowIterator<T> iterator = new RowIterator<>(clazz, sheet, source, binder);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false).onClose(iterator::close);
    }

    /** Turns the mapped cells of one row into a row object. */
    interface RowBinder<T> {
        T bind(int rowIndex, Map<String, CellInfo> cells, List<String> headerKeys, List<String> sheetHeaders) throws Exception;
    }

    private <T extends BaseSheet> RowBinder<T> sheetBinder(Class<T> clazz) {
        Sheet sheet = sheetAnnotation(clazz);
        if (sheet.dynamicHeaders()) {
            Field dynamicCellField = DynamicHeaderSheetReader.dynamicCellField(clazz);
            ExcelSheetReaderContext readerContext = new ExcelSheetReaderContext();
            readerContext.setSheet(sheet);
            readerContext.setHeaderFieldInfoMap(sheetContext.headerFieldInfo());
            readerContext.setSuppressExceptions(sheetContext.suppressExceptions());
            return (rowIndex, cells, headerKeys, sheetHeaders) -> {
                BaseSheet row = DynamicHeaderSheetReader.dynamicCellValueResolver(clazz, headerKeys, cells, rowIndex, dynamicCellField, readerContext);
                row.setSheetHeaders(sheetHeaders);
                row.setCells(cells);
                return clazz.cast(row);
            };
        }
        Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
        Map<Cell, Field> headerCellFieldMap = StaticHeaderSheetReader.headerCellFieldMap(null, sheet, cellFields);
        return (rowIndex, cells, headerKeys, sheetHeaders) -> {
            BaseSheet row = StaticHeaderSheetReader.cellValueResolver(clazz, cells, rowIndex, headerCellFieldMap);
            row.setSheetHeaders(sheetHeaders);
            row.setCells(cells);
            return clazz.cast(row);
        };
    }

    /** Binds a row to a record's canonical constructor. */
    private static final class RecordBinder<R extends Record> implements RowBinder<R> {
        private final Class<R> recordClass;
        private final Constructor<R> constructor;
        private final RecordComponent[] components;
        private final Field[] fields;
        private final String[] headerKeys;

        RecordBinder(Class<R> recordClass) {
            this.recordClass = recordClass;
            this.components = recordClass.getRecordComponents();
            Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            try {
                this.constructor = recordClass.getDeclaredConstructor(types);
                this.constructor.setAccessible(true);
            }
            catch (ReflectiveOperationException | RuntimeException e) {
                throw new ExcelSheetReaderException("Record " + recordClass.getName() + " has no accessible canonical constructor: " + e.getMessage());
            }
            this.fields = new Field[components.length];
            this.headerKeys = new String[components.length];
            for (int i = 0; i < components.length; i++) {
                try {
                    fields[i] = recordClass.getDeclaredField(components[i].getName());
                }
                catch (NoSuchFieldException e) {
                    throw new ExcelSheetReaderException(e);
                }
                Cell cell = fields[i].getAnnotation(Cell.class);
                headerKeys[i] = cell == null ? null : StaticHeaderSheetReader.headerKey(recordClass, cell);
            }
        }

        @Override
        public R bind(int rowIndex, Map<String, CellInfo> cells, List<String> headerKeyList, List<String> sheetHeaders) throws Exception {
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                Class<?> type = components[i].getType();
                CellInfo cellInfo = headerKeys[i] == null ? null : cells.get(headerKeys[i]);
                Object value = cellInfo == null ? null : TypeConvertor.convert(cellInfo.value(), cellInfo.cellType(), type, fields[i]);
                args[i] = value == null && type.isPrimitive() ? primitiveDefault(type) : value;
            }
            return constructor.newInstance(args);
        }

        private static Object primitiveDefault(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == long.class) return 0L;
            if (type == double.class) return 0d;
            if (type == float.class) return 0f;
            if (type == short.class) return (short) 0;
            if (type == byte.class) return (byte) 0;
            return 0;
        }

        @Override
        public String toString() {
            return "RecordBinder[" + recordClass.getName() + "]";
        }
    }

    /** Pulls raw rows from a {@link RowSource} and turns data rows into objects. */
    private final class RowIterator<T> implements Iterator<T> {
        private final Class<T> clazz;
        private final Sheet sheet;
        private final RowSource source;
        private final RowBinder<T> binder;

        private final int headerRowIndex;
        private final int headerColumnIndex;
        private final int headerColumnEndsAt;
        private final int valueRowIndex;
        private final int valueRowEndsAt;
        private final Set<Integer> ignoreHeaderColumns;
        private final List<String> ignoreHeaders;
        private final List<String> ignoreHeaderPatterns;
        private final Set<String> classCellHeaders;

        private final Map<Integer, String> columnHeaders = new HashMap<>();
        private final Map<String, String> originalHeaders = new HashMap<>();
        private final List<String> headerStringList;
        private final List<String> sheetHeaders;
        private int lastHeaderColumn = -1;
        private boolean headersRead;

        private T next;
        private boolean finished;

        RowIterator(Class<T> clazz, Sheet sheet, RowSource source, RowBinder<T> binder) {
            this.clazz = clazz;
            this.sheet = sheet;
            this.source = source;
            this.binder = binder;
            headerRowIndex = sheet.headerRowAt() - 1;
            headerColumnIndex = ExcelSheetReader.toIndentNumber(sheet.headerColumnAt()) - 1;
            headerColumnEndsAt = StringUtils.isNotEmpty(sheet.headerColumnEndsAt()) ? ExcelSheetReader.toIndentNumber(sheet.headerColumnEndsAt()) : Integer.MAX_VALUE;
            int valueRowBeginsAt = sheet.valueRowBeginsAt() > -1 ? sheet.valueRowBeginsAt() - 1 : -1;
            valueRowIndex = valueRowBeginsAt > -1 ? valueRowBeginsAt : sheet.valueRowAt() > -1 ? sheet.valueRowAt() - 1 : headerRowIndex + 1;
            valueRowEndsAt = sheet.valueRowEndsAt() > -1 ? sheet.valueRowEndsAt() - 1 : Integer.MAX_VALUE;
            List<String> contextIgnoreColumns = sheetContext.ignoreHeaderColumns() == null ? Collections.emptyList() : sheetContext.ignoreHeaderColumns();
            ignoreHeaderColumns = contextIgnoreColumns.stream().map(col -> ExcelSheetReader.toIndentNumber(col) - 1).collect(Collectors.toCollection(HashSet::new));
            List<String> contextIgnoreHeaders = sheetContext.ignoreHeaders() == null ? Collections.emptyList() : sheetContext.ignoreHeaders();
            ignoreHeaders = sheet.ignoreHeaders().length > 0 ? Arrays.asList(sheet.ignoreHeaders()) : contextIgnoreHeaders;
            ignoreHeaderPatterns = sheet.ignoreHeaderPatterns().length > 0 ? Arrays.asList(sheet.ignoreHeaderPatterns()) : Collections.emptyList();
            Set<Field> cellFields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
            classCellHeaders = cellFields.stream().map(f -> f.getAnnotation(Cell.class).value()).collect(Collectors.toCollection(HashSet::new));
            headerStringList = BaseExcelSheetReader.orderedOrUnorderedList(sheet);
            sheetHeaders = BaseExcelSheetReader.orderedOrUnorderedList(sheet);
        }

        @Override
        public boolean hasNext() {
            if (next == null && !finished) {
                next = advance();
            }
            return next != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T row = next;
            next = null;
            return row;
        }

        void close() {
            finished = true;
            source.close();
        }

        /** Reads rows until a data row is produced or the sheet ends. */
        private T advance() {
            try {
                while (source.nextRow()) {
                    int rowIndex = source.rowIndex();
                    if (rowIndex == headerRowIndex) {
                        readHeaders(source.cells());
                    }
                    else if (rowIndex > valueRowEndsAt) {
                        break;
                    }
                    else if (headersRead && rowIndex >= valueRowIndex) {
                        T row = toRow(rowIndex, source.cells());
                        if (row != null) {
                            return row;
                        }
                    }
                }
            }
            catch (RuntimeException e) {
                close();
                throw e instanceof ExcelSheetReaderException ? e : new ExcelSheetReaderException(e);
            }
            close();
            return null;
        }

        private void readHeaders(List<RawCell> cells) {
            for (RawCell raw : cells) {
                int c = raw.column;
                if (c < headerColumnIndex || c >= headerColumnEndsAt) {
                    continue;
                }
                Object value = source.value(raw, new CellInfo());
                String headerString = value == null ? "" : ExcelSheetReaderUtil.cleanHeaderString(
                        value instanceof Double && ((Double) value) == Math.rint((Double) value) && !Double.isInfinite((Double) value)
                                ? String.valueOf(((Double) value).longValue()) : String.valueOf(value));
                if (headerString.isEmpty()
                        || (!classCellHeaders.contains(headerString) && !sheet.dynamicHeaders())
                        || ignoreHeaderPatternMatchFound(headerString, ignoreHeaderPatterns)
                        || ignoreHeaders.contains(headerString)) {
                    ignoreHeaderColumns.add(c);
                    continue;
                }
                sheetHeaders.add(headerString);
                String key = ExcelSheetReaderUtil.processSimilarHeaderString(headerString, clazz, c, headerRowIndex, headerStringList);
                columnHeaders.put(c, key);
                originalHeaders.put(key, headerString);
                headerStringList.add(key);
                lastHeaderColumn = Math.max(lastHeaderColumn, c);
            }
            headersRead = true;
        }

        private T toRow(int rowIndex, List<RawCell> rawCells) {
            Map<Integer, RawCell> byColumn = new HashMap<>();
            boolean empty = true;
            for (RawCell raw : rawCells) {
                byColumn.put(raw.column, raw);
                if (raw.column >= headerColumnIndex && raw.column <= lastHeaderColumn && StringUtils.isNotBlank(raw.value)) {
                    empty = false;
                }
            }
            if (empty) {
                return null;
            }
            Map<String, CellInfo> cellInfoMap = BaseExcelSheetReader.orderedOrUnorderedMap(sheet);
            for (int c = headerColumnIndex; c <= lastHeaderColumn; c++) {
                String header = columnHeaders.get(c);
                if (header == null || ignoreHeaderColumns.contains(c)) {
                    continue;
                }
                CellInfo cellInfo = new CellInfo();
                cellInfo.setRowIndex(rowIndex);
                cellInfo.setRow(rowIndex + 1);
                cellInfo.setColumnIndex(c);
                cellInfo.setColumn(ExcelSheetReader.toIndentName(c + 1));
                cellInfo.setHeader(header);
                cellInfo.setOriginalHeader(originalHeaders.get(header));
                RawCell raw = byColumn.get(c);
                if (raw != null) {
                    cellInfo.setValue(source.value(raw, cellInfo));
                    source.decorate(raw, cellInfo);
                }
                cellInfoMap.put(header, cellInfo);
            }
            try {
                return binder.bind(rowIndex, cellInfoMap, headerStringList, sheetHeaders);
            }
            catch (Exception e) {
                if (!sheetContext.suppressExceptions()) {
                    throw new ExcelSheetReaderException(e);
                }
                return null;
            }
        }
    }
}
