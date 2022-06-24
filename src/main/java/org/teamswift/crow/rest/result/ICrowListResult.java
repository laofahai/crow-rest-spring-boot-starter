package org.teamswift.crow.rest.result;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Collection;

public interface ICrowListResult<T> extends Serializable {

    ICrowResultCode getResultCode();

    void setResultCode(ICrowResultCode code);

    boolean isSuccess();

    void setSuccess(boolean success);

    HttpStatus getHttpStatus();

    void setHttpStatus(HttpStatus httpStatus);

    Collection<T> getData();

    void setData(Collection<T> data);

    long getTotalItems();

    void setTotalItems(long totalItems);

    int getTotalPages();

    void setTotalPages(int totalPages);

    int getPage();

    void setPage(int page);

    int getPageSize();

    void setPageSize(int pageSize);

}
