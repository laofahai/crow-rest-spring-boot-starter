package org.teamswift.crow.rest.handler.requestParams;

import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
public class RequestBodyResolved {

    private List<FilterItem> filterItems;

    private Sort sortOrders;

    private int pageSize;

    private int page;

    private boolean pageable;

    private boolean onlyDeleted;

    private boolean onlyCount;

}
