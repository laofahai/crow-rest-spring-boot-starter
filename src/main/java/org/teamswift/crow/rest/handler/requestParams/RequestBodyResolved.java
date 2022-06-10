package org.teamswift.crow.rest.handler.requestParams;

import lombok.Data;
import org.springframework.data.domain.Sort;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.handler.RequestBodyResolveHandler;

import java.util.List;

@Data
public class RequestBodyResolved {

    private List<FilterItem> filterItems;

    private Sort sortOrders = RequestBodyResolveHandler.handleSortItem("-id");

    private int pageSize = 50;

    private int page = 1;

    private boolean pageable;

    private boolean onlyDeleted;

    private boolean onlyCount;

    private Class<? extends ICrowEntity<?, ?>> targetEntity;

}
