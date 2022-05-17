package org.teamswift.crow.rest.exception.impl;

import org.teamswift.crow.rest.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ParameterInvalidException extends BusinessException {

    public ParameterInvalidException(String message) {
        super(message);
    }
}