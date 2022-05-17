package org.teamswift.crow.rest.handler.requestParams;

import lombok.Getter;

@Getter
public enum QueryOperator {

    EQ("equals"), // equal
    NEQ("notEquals"), // not equal
    GT("greaterThan"), // greater than
    LT("lessThan"), // less than
    EGT("greaterThanOrEqualTo"), // equals and greater than
    ELT("lessThanOrEqualTo"), // equals and less than
    IN("in"), // in
    NIN("notIn"), // not in
    BTW("between"), // between
    LIKE("like"), // like
    ;

    private final String operatorName;

    QueryOperator(String operatorName) {
        this.operatorName = operatorName;
    }
}
