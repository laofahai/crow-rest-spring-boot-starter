package org.teamswift.crow.rest.result.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.result.CrowResultCode;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.utils.CrowMessageUtil;

@Data
@NoArgsConstructor
public class CrowErrorResult implements ICrowResult<String> {

    private boolean success = false;

    private String data;

    private String title;

    private int status;

    private CrowResultCode resultCode;

    @JsonIgnore
    private int httpStatusCode;

    public CrowErrorResult(BusinessException e) {
        this.data = e.getBody();
        this.title = e.getTitle();
        this.httpStatusCode = e.getHttpStatus().value();
    }

    public String getTitle() {
        return title == null ? CrowMessageUtil.get("crow.titles.errorOccurred") : title;
    }

    @Override
    public int getStatus() {
        return getResultCode() == null ? 500 : getResultCode().getCode();
    }
}
