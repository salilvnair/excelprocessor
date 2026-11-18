package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CellValidationMessage {
    private String header;
    private String originalHeader;
    private String mappedFieldName;
    private String message;
    private String messageId;
    private String  messageType;
    private int row;
    private String column;
    private String sheet;
    private ValidatorType validatorType;

    /** Cell reference such as {@code B7}, or null when the column is unknown. */
    public String cellReference() {
        return column == null || row < 1 ? null : column + row;
    }

    /** JSON form of the message (the shape 4.0.x produced with Gson; null fields are omitted). */
    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("\n{");
        append(json, "header", header);
        append(json, "originalHeader", originalHeader);
        append(json, "mappedFieldName", mappedFieldName);
        append(json, "message", message);
        append(json, "messageId", messageId);
        append(json, "messageType", messageType);
        separator(json);
        json.append("\"row\":").append(row);
        append(json, "column", column);
        append(json, "sheet", sheet);
        append(json, "validatorType", validatorType == null ? null : validatorType.name());
        return json.append('}').toString();
    }

    private static void separator(StringBuilder json) {
        if (json.charAt(json.length() - 1) != '{') {
            json.append(',');
        }
    }

    private static void append(StringBuilder json, String name, String value) {
        if (value == null) {
            return;
        }
        separator(json);
        json.append('"').append(name).append("\":\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                json.append('\\').append(c);
            }
            else if (c == '\n') {
                json.append("\\n");
            }
            else if (c == '\r') {
                json.append("\\r");
            }
            else if (c == '\t') {
                json.append("\\t");
            }
            else if (c < 0x20) {
                json.append(String.format("\\u%04x", (int) c));
            }
            else {
                json.append(c);
            }
        }
        json.append('"');
    }
}
