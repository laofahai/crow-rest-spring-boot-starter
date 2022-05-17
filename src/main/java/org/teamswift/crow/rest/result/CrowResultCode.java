package org.teamswift.crow.rest.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CrowResultCode {
    SUCCESS(200, "成功"),
    CONFIRM_CONTINUE(2001, "确认继续操作"),
    PARAM_IS_INVALID(10001, "参数无效"),
    PARAM_IS_BLANK(10002, "参数为空"),
    PARAM_TYPE_BIND_ERROR(10003, "参数类型错误"),
    PARAM_NOT_COMPLETE(10004, "参数缺失"),
    USER_NOT_LOGGED_IN(20001, "用户未登录"),
    USER_LOGIN_ERROR(20002, "账号不存在或密码错误"),
    USER_ACCOUNT_FORBIDDEN(20003, "账号已被禁用"),
    USER_NOT_EXIST(20004, "用户不存在"),
    USER_HAS_EXISTED(20005, "用户已存在"),
    LOGIN_CREDENTIAL_EXISTED(20006, "凭证已存在"),
    SPECIFIED_QUESTIONED_USER_NOT_EXIST(30001, "请求异常"),
    SYSTEM_INNER_ERROR(40001, "系统错误"),
    RESULT_DATA_NONE(50001, "数据未找到"),
    DATA_IS_WRONG(50002, "数据有误"),
    DATA_ALREADY_EXISTED(50003, "数据已存在"),
    BILL_STATUS_ERROR(50004, "单据状态错误"),
    QUANTITY_OVERFLOW(50005, "数量溢出"),
    INTERFACE_INNER_INVOKE_ERROR(60001, "内部系统接口调用异常"),
    INTERFACE_OUTER_INVOKE_ERROR(60002, "外部系统接口调用异常"),
    INTERFACE_FORBID_VISIT(60003, "该接口禁止访问"),
    INTERFACE_ADDRESS_INVALID(60004, "接口地址无效"),
    INTERFACE_REQUEST_TIMEOUT(60005, "接口请求超时"),
    INTERFACE_EXCEED_LOAD(60006, "接口负载过高"),
    PERMISSION_NO_ACCESS(70001, "无访问权限"),
    RESOURCE_EXISTED(70002, "资源已存在"),
    RESOURCE_NOT_EXISTED(70003, "资源不存在"),
    API_EXECUTE_FAILURE(80000, "接口请求失败"),
    API_SIGN_CHECK_FAILURE(80001, "接口签名校验失败"),
    API_SIGN_EXPIRED(80002, "接口签名已过期"),
    API_SIGN_PARSE_FAILURE(80003, "签名对象验证失败"),
    API_SERVICE_NOT_REGISTER(80004, "请求的服务未注册"),
    API_PARAMS_MISS(80005, "请求参数错误");

    private final Integer code;
    private final String message;

}
