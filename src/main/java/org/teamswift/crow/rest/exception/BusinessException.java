package org.teamswift.crow.rest.exception;

import org.teamswift.crow.rest.result.CrowResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@EqualsAndHashCode(callSuper = true)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@Data
public class BusinessException extends RuntimeException {

	protected HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

	protected String title;

	protected CrowResultCode resultCode;

	protected String body;

	protected Object rawBody;

	public BusinessException() {
		BusinessExceptionEnum exceptionEnum = BusinessExceptionEnum.getByEClass(this.getClass());
		if (exceptionEnum != null) {
			resultCode = exceptionEnum.getResultCode();
			httpStatus = exceptionEnum.getHttpStatus();
			body = title = exceptionEnum.getResultCode().getMessage();
		}
	}

	public BusinessException(String message) {
		this();
		this.body = message;
	}

	public BusinessException(CrowResultCode resultCode) {
		this.resultCode = resultCode;
		this.title = resultCode.getMessage();
		this.body = resultCode.getMessage();

		BusinessExceptionEnum exceptionEnum = BusinessExceptionEnum.getByCode(resultCode);
		if(exceptionEnum != null) {
			this.httpStatus = exceptionEnum.getHttpStatus();
		}
	}

	public BusinessException(CrowResultCode resultCode, Object data) {
		this(resultCode);
		this.rawBody = data;
		this.body = data.toString();
	}

	public CrowResultCode getResultCode() {
		return resultCode == null ? CrowResultCode.SYSTEM_INNER_ERROR : resultCode;
	}
}