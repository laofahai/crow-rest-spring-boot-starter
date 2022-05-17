package org.teamswift.crow.rest.exception.impl;

import org.teamswift.crow.rest.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotLoginException extends BusinessException {

    public UserNotLoginException(String message) {
        super(message);
    }
}