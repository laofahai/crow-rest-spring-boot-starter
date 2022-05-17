package org.teamswift.crow.rest.result.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.result.ICrowResult;

@Data
@NoArgsConstructor
public class CrowErrorResult implements ICrowResult<String> {

    private boolean success = false;

    private String data;

    private String title;

    private int httpStatusCode;

    public CrowErrorResult(BusinessException e) {
        this.data = e.getBody();
        this.title = e.getTitle();
        this.httpStatusCode = e.getHttpStatus().value();
    }
}
