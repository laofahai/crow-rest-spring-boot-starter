package org.teamswift.crow.rest.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.teamswift.crow.rest.utils.CrowMessageUtil;

@AllArgsConstructor
@Getter
public enum CrowResultCode {
    SUCCESS(200),
    CONFIRM_CONTINUE(2001),
    PARAM_IS_INVALID(10001),
    USER_NOT_LOGGED_IN(20001),
    INVALID_CREDENTIALS(20002),
    USER_ACCOUNT_FORBIDDEN(20003),
    USER_NOT_EXIST(20004),
    USER_HAS_EXISTED(20005),
    SYSTEM_INNER_ERROR(40001),
    DATA_NOT_FOUND(40004),
    DATA_IS_WRONG(50002),
    DATA_ALREADY_EXISTED(50003),
    BILL_STATUS_ERROR(50004),
    QUANTITY_OVERFLOW(50005),
    PERMISSION_DENIED(70001),
    RESOURCE_EXISTED(70002),
    RESOURCE_NOT_EXISTED(70003),
    ;

    private final Integer code;

    @Override
    public String toString() {
        return getMessage();
    }

    public String getMessage() {
        return CrowMessageUtil.resultCode(this);
    }
}
