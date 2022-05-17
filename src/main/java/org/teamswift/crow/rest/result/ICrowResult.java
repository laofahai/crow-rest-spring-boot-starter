package org.teamswift.crow.rest.result;

import java.io.Serializable;

public interface ICrowResult<T> extends Serializable {

    boolean isSuccess();

    int getHttpStatusCode();

    T getData();

    void setData(T data);

}
