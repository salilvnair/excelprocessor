package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CellValidationInfo {
    private boolean required= false;
    private boolean unique= false;
    private boolean conditional= false;
    private String condition= "";
    private int minLength= -1;
    private int maxLength= -1;
    private int length= -1;
    private boolean date= false;
    private DateParsingUtil.DateFormat datePattern= DateParsingUtil.DateFormat.SLASH_MM_DD_YYYY;
    private boolean email= false;
    private boolean allowNull= false;
    private boolean allowEmpty= false;
    private String pattern= "";
    private boolean matchPattern= false;
    private boolean findPattern= true;
    private boolean alphaNumeric= false;
    private boolean numeric= false;
    private String customTask= "";
    private String[] customTasks= {};
    private String userDefinedMessage= "";
    private String messageId= "";
    private String messageType = MessageType.ERROR.name();
    private UserDefinedMessageInfo[] userDefinedMessages= {};
    private AllowedValuesInfo allowedValuesInfo;

    public static CellValidationInfo.CellValidationInfoBuilder defaultValueBuilder() {
        return new CellValidationInfo().toBuilder();
    }
}
