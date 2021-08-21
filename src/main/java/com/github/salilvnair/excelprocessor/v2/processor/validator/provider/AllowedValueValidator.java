package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import com.github.salilvnair.excelprocessor.v2.annotation.AllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.ConditionallyAllowedValues;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.ObjectUtils;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.helper.ExcelValidatorTaskExecutor;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Salil V Nair
 */
public class AllowedValueValidator extends BaseCellValidator {
    private final Field field;
    private List<String> allowedValueList;
    private boolean showValuesInMessage;
    public AllowedValueValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        AllowedValues allowedValues = field.getAnnotation(AllowedValues.class);
        if(allowNullOrAllowEmptyCheck(fieldValue, allowedValues)) {
            return false;
        }
        if(allowedValues.conditionallyAllowedValues().length > 0 ) {
            ConditionallyAllowedValues[] conditionallyAllowedValuesData = allowedValues.conditionallyAllowedValues();
            List<ConditionallyAllowedValues> conditionallyAllowedValueList = Arrays.stream(conditionallyAllowedValuesData).collect(Collectors.toList());

            for(ConditionallyAllowedValues conditionallyAllowedValues: conditionallyAllowedValueList) {
                if(allowNullOrAllowEmptyCheck(fieldValue, conditionallyAllowedValues)) {
                    return false;
                }
                showValuesInMessage = conditionallyAllowedValues.showValuesInMessage();
                boolean satisfiesValidValueCondition = satisfiesValidValueCondition(conditionallyAllowedValues.condition(), validatorContext);
                if(satisfiesValidValueCondition) {
                    return violatesAllowedValues(conditionallyAllowedValues.value(), conditionallyAllowedValues.dataSetKey(), conditionallyAllowedValues.range(), validatorContext, fieldValue+"");
                }
            }
        }
        else {
            showValuesInMessage = allowedValues.showValuesInMessage();
            if(allowedValues.conditional() && (!StringUtils.isNotEmpty(allowedValues.condition())|| allowedValues.conditions().length > 0)) {
                if(allowedValues.conditions().length > 0) {
                    for(String condition: allowedValues.conditions()) {
                        boolean  satisfiesValidValueCondition = satisfiesValidValueCondition(condition, validatorContext);
                        if(satisfiesValidValueCondition) {
                            return violatesAllowedValues(allowedValues.value(), allowedValues.dataSetKey(), allowedValues.range(), validatorContext, fieldValue+"");
                        }
                    }
                }
                else {
                    boolean  satisfiesValidValueCondition = satisfiesValidValueCondition(allowedValues.condition(), validatorContext);
                    if(satisfiesValidValueCondition) {
                        return violatesAllowedValues(allowedValues.value(), allowedValues.dataSetKey(), allowedValues.range(), validatorContext, fieldValue+"");
                    }
                }
            }
            else {
                return violatesAllowedValues(allowedValues.value(), allowedValues.dataSetKey(), allowedValues.range(), validatorContext, fieldValue+"");
            }
        }
        return false;
    }

    private boolean satisfiesValidValueCondition(String condition, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        Object object = ExcelValidatorTaskExecutor.execute(condition, sheet.excelTaskValidator(), validatorContext);
        return ObjectUtils.isBoolean(object) && (Boolean) object;
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
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        StringBuilder msgBuilder = new StringBuilder("Current value of "+headerKey+" does not match with any of the allowed values");
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
