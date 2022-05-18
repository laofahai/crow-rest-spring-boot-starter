package org.teamswift.crow.rest.exception;

import lombok.Getter;

@Getter
public enum ErrorMessages {

    NotFoundByID("Data can't be founded according to the provided ID"),
    ErrorWhenInstance("An error occurred when try to create a new instance: "),
    ;

    private final String message;

    ErrorMessages(String msg) {
        this.message = msg;
    }
}
