package org.teamswift.crow.rest.result;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Collection;

public interface ICrowListResult<T> extends Serializable {

    boolean isSuccess();

    HttpStatus getHttpStatus();

    Collection<T> getData();

    long getTotalItems();

    int getTotalPages();

    int getPage();

    int getPageSize();

}
