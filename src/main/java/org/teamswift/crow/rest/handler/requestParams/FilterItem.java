package org.teamswift.crow.rest.handler.requestParams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterItem {

    private String field;

    private QueryOperator operator = QueryOperator.EQ;

    private Object value;

    public FilterItem(String field, Object value) {
        this.field = field;
        this.value = value;
    }
}
