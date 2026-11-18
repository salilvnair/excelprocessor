package com.github.salilvnair.excelprocessor.v2.features.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;

import java.math.BigDecimal;

@Sheet("Orders")
public record OrderRecord(
        @Cell("Order Id") String orderId,
        @Cell("Customer") String customer,
        @Cell("Amount") BigDecimal amount,
        @Cell("Priority") int priority,
        @Cell("Channel") Channel channel) {
}
