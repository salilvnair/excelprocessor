package com.github.salilvnair.excelprocessor.v2.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelValue;
import org.apache.poi.ss.usermodel.DateUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Last step of reading a cell into a field: turns the value the reader produced (String, Double,
 * Boolean or Date) into the declared field type when it is not already assignable. Covers boxed and
 * primitive numbers, BigDecimal/BigInteger, Boolean, Character, enums (by name or
 * {@link ExcelValue} label), {@code java.time} types and {@link Date}. A value that cannot be
 * converted yields null, so the field keeps its default, as before.
 */
public final class ValueCoercer {
    private ValueCoercer() {}

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            int.class, Integer.class, long.class, Long.class, double.class, Double.class,
            float.class, Float.class, short.class, Short.class, byte.class, Byte.class,
            boolean.class, Boolean.class, char.class, Character.class);

    private static final Map<Class<?>, Map<String, Enum<?>>> ENUM_LOOKUP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<Enum<?>, String>> ENUM_LABELS = new ConcurrentHashMap<>();
    private static final Map<String, DateTimeFormatter> FORMATTERS = new ConcurrentHashMap<>();

    public static Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? WRAPPERS.get(type) : type;
    }

    public static Object coerce(Object value, Type destinationType, Field field) {
        return coerce(value, destinationType, field == null ? null : field.getAnnotation(Cell.class));
    }

    public static Object coerce(Object value, Type destinationType, Cell cell) {
        if (value == null || !(destinationType instanceof Class)) {
            return value;
        }
        Class<?> target = wrap((Class<?>) destinationType);
        if (target == Object.class || target.isInstance(value)) {
            return value;
        }
        try {
            if (value instanceof String && ((String) value).trim().isEmpty()) {
                return null;
            }
            if (target == String.class) {
                return toText(value);
            }
            if (Number.class.isAssignableFrom(target)) {
                return toNumber(value, target);
            }
            if (target == Boolean.class) {
                return toBoolean(value);
            }
            if (target == Character.class) {
                String text = String.valueOf(value);
                return text.isEmpty() ? null : text.charAt(0);
            }
            if (target.isEnum()) {
                return toEnum(value, target);
            }
            if (target == LocalDate.class) {
                LocalDateTime dateTime = toLocalDateTime(value, cell);
                return dateTime == null ? null : dateTime.toLocalDate();
            }
            if (target == LocalDateTime.class) {
                return toLocalDateTime(value, cell);
            }
            if (target == LocalTime.class) {
                if (value instanceof String) {
                    return LocalTime.parse(((String) value).trim());
                }
                LocalDateTime dateTime = toLocalDateTime(value, cell);
                return dateTime == null ? null : dateTime.toLocalTime();
            }
            if (target == Instant.class || target == OffsetDateTime.class || target == ZonedDateTime.class) {
                return toZoned(value, target, cell);
            }
            if (target == Date.class) {
                LocalDateTime dateTime = toLocalDateTime(value, cell);
                return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        }
        catch (RuntimeException e) {
            return null;
        }
        return value;
    }

    /** Text written to a cell for an enum: its first {@link ExcelValue} label, else its name. */
    public static String enumLabel(Enum<?> value) {
        Map<Enum<?>, String> labels = ENUM_LABELS.computeIfAbsent(value.getDeclaringClass(), ValueCoercer::labels);
        return labels.getOrDefault(value, value.name());
    }

    /** Every label an enum type accepts on read, in declaration order (first label, else the name). */
    public static List<String> enumLabels(Class<?> enumType) {
        List<String> labels = new ArrayList<>();
        for (Object constant : enumType.getEnumConstants()) {
            labels.add(enumLabel((Enum<?>) constant));
        }
        return labels;
    }

    private static String toText(Object value) {
        if (value instanceof Enum) {
            return enumLabel((Enum<?>) value);
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return String.valueOf(d);
            }
            BigDecimal decimal = BigDecimal.valueOf(d).stripTrailingZeros();
            return decimal.scale() <= 0 ? decimal.toBigInteger().toString() : decimal.toPlainString();
        }
        return String.valueOf(value);
    }

    private static Object toNumber(Object value, Class<?> target) {
        BigDecimal decimal;
        if (value instanceof Number) {
            decimal = value instanceof BigDecimal ? (BigDecimal) value
                    : value instanceof BigInteger ? new BigDecimal((BigInteger) value)
                    : (value instanceof Double || value instanceof Float) ? BigDecimal.valueOf(((Number) value).doubleValue())
                    : BigDecimal.valueOf(((Number) value).longValue());
        }
        else if (value instanceof Boolean) {
            decimal = ((Boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        else if (value instanceof String) {
            String text = ((String) value).trim().replace(",", "");
            decimal = text.endsWith("%")
                    ? new BigDecimal(text.substring(0, text.length() - 1).trim()).movePointLeft(2)
                    : new BigDecimal(text);
        }
        else {
            return null;
        }
        if (target == Integer.class) return decimal.intValue();
        if (target == Long.class) return decimal.longValue();
        if (target == Double.class) return decimal.doubleValue();
        if (target == Float.class) return decimal.floatValue();
        if (target == Short.class) return decimal.shortValue();
        if (target == Byte.class) return decimal.byteValue();
        if (target == BigInteger.class) return decimal.toBigInteger();
        if (target == BigDecimal.class) return decimal;
        return null;
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        switch (text) {
            case "true": case "yes": case "y": case "1": case "x": case "on":
                return true;
            case "false": case "no": case "n": case "0": case "off":
                return false;
            default:
                return null;
        }
    }

    private static Object toEnum(Object value, Class<?> target) {
        Map<String, Enum<?>> lookup = ENUM_LOOKUP.computeIfAbsent(target, ValueCoercer::lookup);
        return lookup.get(normalize(toText(value)));
    }

    private static Map<String, Enum<?>> lookup(Class<?> enumType) {
        Map<String, Enum<?>> lookup = new HashMap<>();
        for (Object constant : enumType.getEnumConstants()) {
            Enum<?> e = (Enum<?>) constant;
            lookup.putIfAbsent(normalize(e.name()), e);
        }
        // labels win over names
        for (Object constant : enumType.getEnumConstants()) {
            Enum<?> e = (Enum<?>) constant;
            ExcelValue excelValue = annotation(e);
            if (excelValue != null) {
                for (String label : excelValue.value()) {
                    lookup.put(normalize(label), e);
                }
            }
        }
        return lookup;
    }

    private static Map<Enum<?>, String> labels(Class<?> enumType) {
        Map<Enum<?>, String> labels = new HashMap<>();
        for (Object constant : enumType.getEnumConstants()) {
            Enum<?> e = (Enum<?>) constant;
            ExcelValue excelValue = annotation(e);
            if (excelValue != null && excelValue.value().length > 0) {
                labels.put(e, excelValue.value()[0]);
            }
        }
        return labels;
    }

    private static ExcelValue annotation(Enum<?> e) {
        try {
            return e.getDeclaringClass().getField(e.name()).getAnnotation(ExcelValue.class);
        }
        catch (NoSuchFieldException ex) {
            return null;
        }
    }

    private static String normalize(String text) {
        return text.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static Object toZoned(Object value, Class<?> target, Cell cell) {
        if (value instanceof String) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(((String) value).trim());
                return target == Instant.class ? odt.toInstant() : target == OffsetDateTime.class ? odt : odt.toZonedDateTime();
            }
            catch (DateTimeParseException ignored) {
                // no offset: read as a local date-time in the system zone
            }
        }
        LocalDateTime dateTime = toLocalDateTime(value, cell);
        if (dateTime == null) {
            return null;
        }
        ZonedDateTime zoned = dateTime.atZone(ZoneId.systemDefault());
        return target == Instant.class ? zoned.toInstant() : target == OffsetDateTime.class ? zoned.toOffsetDateTime() : zoned;
    }

    private static LocalDateTime toLocalDateTime(Object value, Cell cell) {
        if (value instanceof Date) {
            return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Number) {
            return DateUtil.getLocalDateTime(((Number) value).doubleValue());
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (!(value instanceof String)) {
            return null;
        }
        String text = ((String) value).trim();
        try {
            return LocalDateTime.parse(text);
        }
        catch (DateTimeParseException ignored) {
            // not an ISO date-time
        }
        try {
            return LocalDate.parse(text).atStartOfDay();
        }
        catch (DateTimeParseException ignored) {
            // not an ISO date
        }
        if (cell == null) {
            return null;
        }
        for (String pattern : new String[] {cell.dateTimeFormat().value(), cell.dateFormat().value()}) {
            DateTimeFormatter formatter = FORMATTERS.computeIfAbsent(pattern, p -> DateTimeFormatter.ofPattern(p, Locale.ENGLISH));
            try {
                TemporalAccessor parsed = formatter.parse(text);
                LocalDate date = LocalDate.from(parsed);
                boolean hasTime = parsed.isSupported(ChronoField.HOUR_OF_DAY) || parsed.isSupported(ChronoField.CLOCK_HOUR_OF_AMPM);
                return LocalDateTime.of(date, hasTime ? LocalTime.from(parsed) : LocalTime.MIDNIGHT);
            }
            catch (DateTimeException ignored) {
                // try the next pattern
            }
        }
        return null;
    }
}
