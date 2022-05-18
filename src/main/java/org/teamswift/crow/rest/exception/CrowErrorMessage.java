package org.teamswift.crow.rest.exception;

import lombok.Getter;

public enum CrowErrorMessage {

    NotFoundByID,
    ErrorWhenInstance,
    EntityMustManagedByCrow,
    CustomResultClass,
    ConvertIDStringToGeneric,

    BetweenQueryNeedTwoParams,
    BetweenQueryNeedParamForArray,

    ;

}
