package com.github.salilvnair.excelprocessor.v2.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Dialect of a delimited text file. Immutable; use {@link #csv()}, {@link #tsv()} or the
 * {@code with*} methods.
 *
 * <ul>
 *   <li>{@code delimiter}: field separator (default {@code ,})</li>
 *   <li>{@code quote}: quote character (default {@code "}); a quote inside a quoted field is doubled</li>
 *   <li>{@code charset}: default UTF-8; a leading byte order mark is skipped on read</li>
 *   <li>{@code inferTypes}: on read, turn plain numbers into Double and true/false into Boolean,
 *       like XLSX cells (default false: values stay text and are converted to the field type)</li>
 *   <li>{@code escapeFormulas}: on write, prefix text starting with {@code = + - @} (or a tab or
 *       carriage return) with {@code '} so spreadsheet programs do not run it as a formula
 *       (default true)</li>
 *   <li>{@code lineSeparator}: on write (default CRLF, as RFC 4180 specifies)</li>
 * </ul>
 */
public final class CsvFormat {
    private final char delimiter;
    private final char quote;
    private final Charset charset;
    private final boolean inferTypes;
    private final boolean escapeFormulas;
    private final String lineSeparator;

    private CsvFormat(char delimiter, char quote, Charset charset, boolean inferTypes, boolean escapeFormulas, String lineSeparator) {
        this.delimiter = delimiter;
        this.quote = quote;
        this.charset = charset;
        this.inferTypes = inferTypes;
        this.escapeFormulas = escapeFormulas;
        this.lineSeparator = lineSeparator;
    }

    public static CsvFormat csv() {
        return new CsvFormat(',', '"', StandardCharsets.UTF_8, false, true, "\r\n");
    }

    public static CsvFormat tsv() {
        return csv().withDelimiter('\t');
    }

    public CsvFormat withDelimiter(char delimiter) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public CsvFormat withQuote(char quote) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public CsvFormat withCharset(Charset charset) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public CsvFormat withInferTypes(boolean inferTypes) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public CsvFormat withEscapeFormulas(boolean escapeFormulas) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public CsvFormat withLineSeparator(String lineSeparator) {
        return new CsvFormat(delimiter, quote, charset, inferTypes, escapeFormulas, lineSeparator);
    }

    public char delimiter() {
        return delimiter;
    }

    public char quote() {
        return quote;
    }

    public Charset charset() {
        return charset;
    }

    public boolean inferTypes() {
        return inferTypes;
    }

    public boolean escapeFormulas() {
        return escapeFormulas;
    }

    public String lineSeparator() {
        return lineSeparator;
    }
}
