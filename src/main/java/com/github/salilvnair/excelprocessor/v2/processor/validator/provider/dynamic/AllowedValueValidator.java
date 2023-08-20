package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.AllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.model.ConditionallyAllowedValuesInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Salil V Nair
 */
public class AllowedValueValidator extends BaseDynamicCellValidator {

    private List<String> allowedValueList;
    private boolean showValuesInMessage;

    public AllowedValueValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }


    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        AllowedValuesInfo allowedValues = allowedValuesInfo();
        if(allowNullOrAllowEmptyCheck(fieldValue, allowedValues)) {
            return false;
        }
        if(allowedValues.getConditionallyAllowedValues().length > 0 ) {
            ConditionallyAllowedValuesInfo[] conditionallyAllowedValuesData = allowedValues.getConditionallyAllowedValues();
            List<ConditionallyAllowedValuesInfo> conditionallyAllowedValueList = Arrays.stream(conditionallyAllowedValuesData).collect(Collectors.toList());

            for(ConditionallyAllowedValuesInfo conditionallyAllowedValues: conditionallyAllowedValueList) {
                if(allowNullOrAllowEmptyCheck(fieldValue, conditionallyAllowedValues)) {
                    return false;
                }
                showValuesInMessage = conditionallyAllowedValues.isShowValuesInMessage();
                boolean satisfiesValidValueCondition = satisfiesValidValueCondition(conditionallyAllowedValues.getCondition(), validatorContext);
                if(satisfiesValidValueCondition) {
                    return violatesAllowedValues(conditionallyAllowedValues.getValue(), conditionallyAllowedValues.getCondition(), conditionallyAllowedValues.getRange(), validatorContext, fieldValue+"");
                }
            }
        }
        else {
            showValuesInMessage = allowedValues.isShowValuesInMessage();
            if(allowedValues.isConditional() && (!StringUtils.isNotEmpty(allowedValues.getCondition())|| allowedValues.getConditions().length > 0)) {
                if(allowedValues.getConditions().length > 0) {
                    for(String condition: allowedValues.getConditions()) {
                        boolean  satisfiesValidValueCondition = satisfiesValidValueCondition(condition, validatorContext);
                        if(satisfiesValidValueCondition) {
                            return violatesAllowedValues(allowedValues.getValue(), allowedValues.getDataSetKey(), allowedValues.getRange(), validatorContext, fieldValue+"");
                        }
                    }
                }
                else {
                    boolean  satisfiesValidValueCondition = satisfiesValidValueCondition(allowedValues.getCondition(), validatorContext);
                    if(satisfiesValidValueCondition) {
                        return violatesAllowedValues(allowedValues.getValue(), allowedValues.getDataSetKey(), allowedValues.getRange(), validatorContext, fieldValue+"");
                    }
                }
            }
            else {
                return violatesAllowedValues(allowedValues.getValue(), allowedValues.getDataSetKey(), allowedValues.getRange(), validatorContext, fieldValue+"");
            }
        }
        return false;
    }

    private boolean satisfiesValidValueCondition(String condition, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        Object object = ExcelValidatorTaskExecutor.execute(condition, sheet.excelTaskValidator(), validatorContext);
        return ObjectUtil.isBoolean(object) && (Boolean) object;
    }

    private boolean violatesAllowedValues(String[] allowedValues, String dataSetKey, int[] range, CellValidatorContext validatorContext, String fieldValue) {
        if(allowedValues.length > 0) {
            allowedValueList = new ArrayList<>(Arrays.asList(allowedValues));
        }
        if(StringUtils.isNotEmpty(dataSetKey) && validatorContext.validValuesDataSet().containsKey(dataSetKey)) {
            List<String> validValueDataSet = validatorContext.validValuesDataSet().get(dataSetKey);
            if(allowedValueList == null) {
                allowedValueList = validValueDataSet;
            }
            else {
                allowedValueList.addAll(validValueDataSet);
            }
        }
        if(range.length > 1) {
            List<String> rangeList = IntStream
                                        .range(range[0], range[1])
                                        .boxed()
                                        .map(i -> i + "")
                                        .collect(Collectors.toList());
            if(allowedValueList == null) {
                allowedValueList = rangeList;
            }
            else {
                allowedValueList.addAll(rangeList);
            }
        }
        return CollectionUtils.isNotEmpty(allowedValueList) && !allowedValueList.contains(fieldValue);
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String header = header();
        StringBuilder msgBuilder = new StringBuilder("Current value of "+header+":"+fieldValue+" does not match with any of the allowed values");
        if(showValuesInMessage) {
            msgBuilder.append(" ").append(allowedValueList);
        }
        else {
            msgBuilder.append(".");
        }
        return msgBuilder.toString();
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.ALLOWED_VALUES;
    }
}
