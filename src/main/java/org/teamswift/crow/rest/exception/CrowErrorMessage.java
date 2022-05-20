package org.teamswift.crow.rest.exception;

import lombok.Getter;

public enum CrowErrorMessage {

    NotFound,
    NotFoundByID,
    ErrorWhenInstance,
    EntityMustManagedByCrow,
    CustomResultClass,
    ConvertIDStringToGeneric,
    EntityNotBeenSoftDeleted,

    BetweenQueryNeedTwoParams,
    BetweenQueryNeedParamForArray,

    ;

}
