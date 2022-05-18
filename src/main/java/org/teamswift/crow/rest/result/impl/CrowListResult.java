package org.teamswift.crow.rest.result.impl;

import org.teamswift.crow.rest.result.ICrowListResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class CrowListResult<T> implements ICrowListResult<T> {

    private boolean success = true;

    private HttpStatus httpStatus = HttpStatus.OK;

    private Collection<T> data = new ArrayList<>();

    private long totalItems;

    private int totalPages;

    private int page;

    private int pageSize;

    public CrowListResult(Collection<T> data, long totalItems, int page, int pageSize) {
        this.data = data;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
    }
}
