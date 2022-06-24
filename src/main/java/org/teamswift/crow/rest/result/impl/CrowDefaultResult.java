package org.teamswift.crow.rest.result.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.teamswift.crow.rest.result.CrowResultCode;
import org.teamswift.crow.rest.result.ICrowResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class CrowDefaultResult<T> implements ICrowResult<T> {

    private boolean success = true;

    private T data;

    private int status;

    private CrowResultCode resultCode = CrowResultCode.SUCCESS;

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

}
