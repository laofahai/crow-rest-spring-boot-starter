package org.teamswift.crow.rest.exception.impl;

import org.teamswift.crow.rest.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MethodNotAllowException extends BusinessException {

    public MethodNotAllowException(String message) {
        super(message);
    }
}