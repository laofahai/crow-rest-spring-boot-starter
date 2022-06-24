package org.teamswift.crow.rest.result;

import java.io.Serializable;

public interface ICrowResult<T> extends Serializable {

    boolean isSuccess();

    int getHttpStatusCode();

    CrowResultCode getResultCode();

    T getData();

    void setData(T data);

    default int getStatus() {
        return getResultCode() == null ? 200 : getResultCode().getCode();
    }

}
