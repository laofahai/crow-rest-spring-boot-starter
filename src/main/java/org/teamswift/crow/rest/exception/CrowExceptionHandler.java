package org.teamswift.crow.rest.exception;

import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.impl.CrowErrorResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class CrowExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ResponseEntity<CrowErrorResult> businessException(BusinessException exception) {
        return new ResponseEntity<>(CrowResult.ofError(exception), exception.getHttpStatus());
    }

}
