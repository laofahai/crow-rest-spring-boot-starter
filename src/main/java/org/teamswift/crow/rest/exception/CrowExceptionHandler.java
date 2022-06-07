package org.teamswift.crow.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.impl.CrowErrorResult;

@ControllerAdvice
public class CrowExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ResponseEntity<CrowErrorResult> handleBusinessException(BusinessException exception) {
        return new ResponseEntity<>(CrowResult.ofError(exception), exception.getHttpStatus());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<CrowErrorResult> handleException(Exception exception) {
        CrowErrorResult result = CrowResult.ofError(exception);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
