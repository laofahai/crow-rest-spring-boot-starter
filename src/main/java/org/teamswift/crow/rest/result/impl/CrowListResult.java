package org.teamswift.crow.rest.result.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.teamswift.crow.rest.result.CrowResultCode;
import org.teamswift.crow.rest.result.ICrowListResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.teamswift.crow.rest.result.ICrowResultCode;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class CrowListResult<T> implements ICrowListResult<T> {

    private boolean success = true;

    private int status;

    @JsonIgnore
    private HttpStatus httpStatus = HttpStatus.OK;

    private ICrowResultCode resultCode;

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

    public CrowListResult(Collection<T> data) {
        this.data = data;
    }

    public int getStatus() {
        return getResultCode() == null ? 200 : getResultCode().getCode();
    }

}
