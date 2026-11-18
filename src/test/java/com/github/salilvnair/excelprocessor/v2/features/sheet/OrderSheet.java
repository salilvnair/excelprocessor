package com.github.salilvnair.excelprocessor.v2.features.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.AllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.CellValidation;
import com.github.salilvnair.excelprocessor.v2.annotation.ConditionalFormat;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.IndexedColors;

@Getter
@Setter
@Sheet(value = "Orders", freezeHeader = true, autoFilter = true, autoSizeColumns = true, headerRowHeight = 24, allowedValuesDropdown = true)
public class OrderSheet extends BaseSheet {
    @CellValidation(required = true, unique = true)
    @Cell("Order Id")
    private String orderId;

    @CellValidation(required = true)
    @Cell("Customer")
    private String customer;

    /** the library checks @AllowedValues together with required */
    @CellValidation(required = true)
    @AllowedValues({"Open", "Shipped", "Closed"})
    @Cell("Status")
    private String status;

    @ConditionalFormat(operator = ConditionalFormat.Operator.LESS_THAN, value = "0", fontColor = IndexedColors.RED, bold = true)
    @ConditionalFormat(operator = ConditionalFormat.Operator.GREATER_THAN, value = "1000", backgroundColor = IndexedColors.LIGHT_GREEN)
    @Cell("Amount")
    private Double amount;

    @AllowedValues(range = {1, 10})
    @Cell("Priority")
    private Long priority;

    @AllowedValues(dataSetKey = "regions")
    @Cell("Region")
    private String region;

    @Cell("Channel")
    private Channel channel;

    public static OrderSheet of(String id, String customer, String status, Double amount) {
        OrderSheet row = new OrderSheet();
        row.setOrderId(id);
        row.setCustomer(customer);
        row.setStatus(status);
        row.setAmount(amount);
        return row;
    }
}
