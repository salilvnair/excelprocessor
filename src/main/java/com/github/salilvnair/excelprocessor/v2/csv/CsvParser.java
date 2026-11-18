package com.github.salilvnair.excelprocessor.v2.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming RFC 4180 parser: quoted fields, doubled quotes, delimiters and line breaks inside
 * quotes, CRLF/LF/CR line endings and a leading byte order mark. One record at a time; nothing
 * else is buffered.
 */
public final class CsvParser implements Closeable {
    /** guards against unbounded memory on a malformed file (an unclosed quote) */
    private static final int MAX_FIELD_LENGTH = 32 * 1024 * 1024;

    private final Reader reader;
    private final char delimiter;
    private final char quote;
    private int pushedBack = -2;
    private boolean eof;
    private boolean first = true;

    public CsvParser(Reader reader, CsvFormat format) {
        this.reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader, 64 * 1024);
        this.delimiter = format.delimiter();
        this.quote = format.quote();
    }

    public CsvParser(InputStream inputStream, CsvFormat format) {
        this(new InputStreamReader(inputStream, format.charset()), format);
    }

    /** Next record, or null at the end of the input. A blank line is an empty list. */
    public List<String> next() throws IOException {
        if (eof) {
            return null;
        }
        int c = read();
        if (first) {
            first = false;
            if (c == 0xFEFF) {
                c = read();
            }
        }
        if (c == -1) {
            eof = true;
            return null;
        }
        List<String> fields = new ArrayList<>();
        if (c == '\r' || c == '\n') {
            consumeLineEnd(c);
            return fields;
        }
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean fieldStart = true;
        while (true) {
            if (c == -1) {
                fields.add(field.toString());
                eof = true;
                return fields;
            }
            if (quoted) {
                if (c == quote) {
                    int nextChar = read();
                    if (nextChar == quote) {
                        field.append(quote);
                    }
                    else {
                        quoted = false;
                        c = nextChar;
                        continue;
                    }
                }
                else {
                    append(field, (char) c);
                }
            }
            else if (c == quote && fieldStart) {
                quoted = true;
            }
            else if (c == delimiter) {
                fields.add(field.toString());
                field.setLength(0);
                fieldStart = true;
                c = read();
                continue;
            }
            else if (c == '\r' || c == '\n') {
                fields.add(field.toString());
                consumeLineEnd(c);
                return fields;
            }
            else {
                append(field, (char) c);
            }
            fieldStart = false;
            c = read();
        }
    }

    private static void append(StringBuilder field, char c) throws IOException {
        if (field.length() >= MAX_FIELD_LENGTH) {
            throw new IOException("CSV field longer than " + MAX_FIELD_LENGTH + " characters (unclosed quote?)");
        }
        field.append(c);
    }

    private void consumeLineEnd(int c) throws IOException {
        if (c == '\r') {
            int nextChar = read();
            if (nextChar != '\n') {
                pushedBack = nextChar;
            }
        }
    }

    private int read() throws IOException {
        if (pushedBack != -2) {
            int c = pushedBack;
            pushedBack = -2;
            return c;
        }
        return reader.read();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
