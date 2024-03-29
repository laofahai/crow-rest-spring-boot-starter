package org.teamswift.crow.rest.exception;

import org.teamswift.crow.rest.exception.impl.*;
import org.teamswift.crow.rest.result.CrowResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.teamswift.crow.rest.result.ICrowResultCode;

@Getter
public enum BusinessExceptionEnum {

	/**
	 * 无效参数
	 */
	PARAMETER_INVALID(ParameterInvalidException.class, HttpStatus.BAD_REQUEST, CrowResultCode.PARAM_IS_INVALID),

	/**
	 * 数据未找到
	 */
	NOT_FOUND(DataNotFoundException.class, HttpStatus.NOT_FOUND, CrowResultCode.DATA_NOT_FOUND),

	/**
	 * 数据已存在
	 */
	CONFLICT(DataConflictException.class, HttpStatus.CONFLICT, CrowResultCode.DATA_ALREADY_EXISTED),

	/**
	 * 用户未登录
	 */
	UNAUTHORIZED(UserNotLoginException.class, HttpStatus.UNAUTHORIZED, CrowResultCode.USER_NOT_LOGGED_IN),

	/**
	 * 无访问权限
	 */
	FORBIDDEN(PermissionForbiddenException.class, HttpStatus.FORBIDDEN, CrowResultCode.PERMISSION_DENIED),

	/**
	 * 系统内部错误
	 */
	INTERNAL_SERVER_ERROR(InternalServerException.class, HttpStatus.INTERNAL_SERVER_ERROR, CrowResultCode.SYSTEM_INNER_ERROR);

	private final Class<? extends BusinessException> eClass;

	private final HttpStatus httpStatus;

	private final ICrowResultCode resultCode;

	BusinessExceptionEnum(Class<? extends BusinessException> eClass, HttpStatus httpStatus, ICrowResultCode resultCode) {
		this.eClass = eClass;
		this.httpStatus = httpStatus;
		this.resultCode = resultCode;
	}

	public static boolean isSupportHttpStatus(int httpStatus) {
		for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
			if (exceptionEnum.httpStatus.value() == httpStatus) {
				return true;
			}
		}

		return false;
	}

	public static boolean isSupportException(Class<?> z) {
		for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
			if (exceptionEnum.eClass.equals(z)) {
				return true;
			}
		}

		return false;
	}

	public static BusinessExceptionEnum getByHttpStatus(HttpStatus httpStatus) {
		if (httpStatus == null) {
			return null;
		}

		for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
			if (httpStatus.equals(exceptionEnum.httpStatus)) {
				return exceptionEnum;
			}
		}

		return null;
	}

	public static BusinessExceptionEnum getByEClass(Class<? extends BusinessException> eClass) {
		if (eClass == null) {
			return null;
		}

		for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
			if (eClass.equals(exceptionEnum.eClass)) {
				return exceptionEnum;
			}
		}

		return null;
	}

	public static BusinessExceptionEnum getByCode(ICrowResultCode resultCode) {

		if(resultCode == null) {
			return null;
		}

		for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
			if (resultCode.equals(exceptionEnum.resultCode)) {
				return exceptionEnum;
			}
		}

		return null;
	}
}