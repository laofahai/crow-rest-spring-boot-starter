package org.teamswift.crow.rest.exception.impl;

import org.teamswift.crow.rest.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.utils.CrowMessageUtil;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DataNotFoundException extends BusinessException {

    public DataNotFoundException() {
        super(
                CrowMessageUtil.error(CrowErrorMessage.NotFound)
        );
    }

    public DataNotFoundException(String message) {
        super(message);
    }
}