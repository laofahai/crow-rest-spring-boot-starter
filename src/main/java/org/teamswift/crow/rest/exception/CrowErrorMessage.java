package org.teamswift.crow.rest.exception;

public enum CrowErrorMessage implements ICrowErrorMessage{

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
