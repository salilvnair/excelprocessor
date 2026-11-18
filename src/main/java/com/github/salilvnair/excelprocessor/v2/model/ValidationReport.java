package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Outcome of validating a sheet while streaming it: how many rows were read, how many failed and
 * the messages (row and column included) of the failures.
 */
public final class ValidationReport {
    private final List<CellValidationMessage> messages = new ArrayList<>();
    private final int maxMessages;
    private long rowsRead;
    private long rowsWithErrors;
    private long messageCount;

    public ValidationReport() {
        this(Integer.MAX_VALUE);
    }

    /** Keeps at most {@code maxMessages} messages; {@link #messageCount()} still counts them all. */
    public ValidationReport(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /** Records one row and its messages (may be empty). */
    public void addRow(List<CellValidationMessage> rowMessages) {
        rowsRead++;
        if (rowMessages == null || rowMessages.isEmpty()) {
            return;
        }
        rowsWithErrors++;
        messageCount += rowMessages.size();
        for (CellValidationMessage message : rowMessages) {
            if (messages.size() >= maxMessages) {
                break;
            }
            messages.add(message);
        }
    }

    public List<CellValidationMessage> messages() {
        return Collections.unmodifiableList(messages);
    }

    public long rowsRead() {
        return rowsRead;
    }

    public long rowsWithErrors() {
        return rowsWithErrors;
    }

    public long validRows() {
        return rowsRead - rowsWithErrors;
    }

    /** Number of messages produced, including any beyond the kept maximum. */
    public long messageCount() {
        return messageCount;
    }

    public boolean truncated() {
        return messageCount > messages.size();
    }

    public boolean hasErrors() {
        return messageCount > 0;
    }

    @Override
    public String toString() {
        return "ValidationReport{rowsRead=" + rowsRead + ", rowsWithErrors=" + rowsWithErrors + ", messages=" + messageCount + '}';
    }
}
