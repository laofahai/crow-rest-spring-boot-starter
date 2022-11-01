package org.teamswift.crow.rest.result.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.teamswift.crow.rest.result.CrowResultCode;
import org.teamswift.crow.rest.result.ICrowResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.teamswift.crow.rest.result.ICrowResultCode;

@Data
@NoArgsConstructor
public class CrowDefaultResult<T> implements ICrowResult<T> {

    private boolean success = true;

    private T data;

    private int status;

    private int code;

    private ICrowResultCode resultCode = CrowResultCode.SUCCESS;

    @JsonIgnore
    private final int httpStatusCode = HttpStatus.OK.value();

    public CrowDefaultResult(T data) {
        this.data = data;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public int getStatus() {
        return getResultCode() == null ? 200 : getResultCode().getCode();
    }

    public int getCode() {
        return getStatus();
    }
}
