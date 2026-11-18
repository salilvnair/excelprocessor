package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

@Sheet("Orders")
public class OrderRow extends BaseSheet {
    @CellValidation(required = true, unique = true)
    @Cell("Order Id")
    private String orderId;

    @CellValidation(required = true)
    @Cell("Customer")
    private String customer;

    @Cell("Amount")
    private Double amount;

    public OrderRow() {
    }

    public OrderRow(String orderId, String customer, Double amount) {
        this.orderId = orderId;
        this.customer = customer;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomer() {
        return customer;
    }

    public Double getAmount() {
        return amount;
    }
}
