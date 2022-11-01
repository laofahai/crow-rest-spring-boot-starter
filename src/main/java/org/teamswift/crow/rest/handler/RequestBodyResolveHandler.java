package org.teamswift.crow.rest.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.ParameterInvalidException;
import org.teamswift.crow.rest.handler.requestParams.FilterItem;
import org.teamswift.crow.rest.handler.requestParams.QueryOperator;
import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.service.CrowDataStructureService;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.NamingUtils;
import org.teamswift.crow.rest.utils.Scaffolds;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class RequestBodyResolveHandler {

    /**
     * To support the filter style like foo=@a,b means foo in (a, b)
     */
    private static final List<String> simpleOperators = new ArrayList<>(){{
        add("!"); // !=
        add(">"); // >
        add("<"); // <
        add("^"); // like
        add("@"); // in
        add("#"); // between
        add("("); // <=
        add(")"); // >=
    }};

    public static RequestBodyResolved handle(HttpServletRequest request, Class<? extends ICrowEntity<?, ?>> entityCls) {

        String filtersStr = "[]";
        String sortStr = "-id";
        int pageNumber = 1;
        int pageSize = 100;
        boolean onlyDeleted = false;

        List<FilterItem> filtersRaw = new ArrayList<>();

        Enumeration<String> params = request.getParameterNames();

        Map<String, Field> fields = Scaffolds.getAllDeclareFieldsMap(entityCls);
        List<String> existsFields = new ArrayList<>(fields.keySet());

        CrowDataStructureService crowDataStructureService = CrowBeanUtils.getBean(CrowDataStructureService.class);

        // 解析所有 get 请求参数
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);

            if (Strings.isNullOrEmpty(value)) {
                continue;
            }

            switch (name) {
                case "page":
                case "_page":
                case "_pageNo":
                case "_pn":
                case "_p":
                    pageNumber = Integer.parseInt(value);
                    break;
                case "_limit":
                case "limit":
                case "pageSize":
                case "_pageSize":
                case "_ps":
                    pageSize = Integer.parseInt(value);
                    break;
                case "_sort":
                case "_st":
                case "_s":
                    sortStr = value;
                    break;
                case "_filter":
                case "_ft":
                case "_f":
                    filtersStr = value;
                    break;
                case "_ot":
                    onlyDeleted = "1".equals(value);
                    filtersRaw.add(new FilterItem("deletedDate",
                            onlyDeleted ? QueryOperator.NEQ : QueryOperator.EQ, null));
                    break;
                default:
                    // 兼容 xx_begin + xx_end 形式
                    String beginName = "";
                    String endName = "";

                    if(name.length() > 6) {
                        beginName = NamingUtils.underlineToCamel(name.substring(0, name.length() - 6));
                    }
                    if(name.length() > 4) {
                        endName = NamingUtils.underlineToCamel(name.substring(0, name.length() - 4));
                    }

                    if(name.endsWith("_begin")) {
                        filtersRaw.add(new FilterItem(beginName, QueryOperator.EGT, value));
                    } else if(name.endsWith("_end")) {
                        filtersRaw.add(new FilterItem(endName, QueryOperator.ELT, value));
                    } else if (existsFields.contains(name) || name.contains(".") || name.contains("|")) {
                        Object obj = crowDataStructureService.tryTransValue(fields.get(name), QueryOperator.EQ, value);
                        QueryOperator operator = QueryOperator.EQ;
                        if(value.startsWith("%")) {
                            operator = QueryOperator.LIKE;
                        }
                        filtersRaw.add(new FilterItem(name, operator, obj));
                    }
            }
        }

        // handle filter string
        // the filter string is a JSON string seems like: [{property: xx, operation: =, value: bar}, {}]
        try {
            ObjectMapper objectMapper = CrowBeanUtils.getBean(ObjectMapper.class);
            List<Map<String, Object>> ofFilterStrings = objectMapper.readValue(filtersStr, List.class);
            ofFilterStrings.forEach(item -> {
                if(!item.containsKey("property") || !item.containsKey("value")) {
                    return;
                }

                QueryOperator operator = QueryOperator.EQ;
                String operatorRaw = String.valueOf(item.getOrDefault("operator", "="));
                switch(operatorRaw.toLowerCase(Locale.ROOT)) {
                    case "!=":
                    case "neq":
                        break;
                    case ">":
                        operator = QueryOperator.GT;
                        break;
                    case "<":
                        operator = QueryOperator.LT;
                        break;
                    case ">=":
                        operator = QueryOperator.EGT;
                        break;
                    case "<=":
                        operator = QueryOperator.ELT;
                        break;
                    case "in":
                        operator = QueryOperator.IN;
                        break;
                    case "btw":
                        operator = QueryOperator.BTW;
                        break;
                }

                Object value = crowDataStructureService.tryTransValue(fields.get(item.get("property")), operator, item.get("value"));
                filtersRaw.add(new FilterItem(String.valueOf(item.get("property")), operator, value));
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        filtersRaw.forEach(RequestBodyResolveHandler::handleFilterItem);

        RequestBodyResolved body = new RequestBodyResolved();
        body.setFilterItems(filtersRaw);
        body.setPage(pageNumber);
        body.setPageSize(pageSize);
        body.setOnlyDeleted(onlyDeleted);
        body.setTargetEntity(entityCls);
        body.setSortOrders(handleSortItem(sortStr));

        Map<String, IRequestBodyResolver> beans = CrowBeanUtils.getBeans(IRequestBodyResolver.class);
        beans.values().forEach(bean -> {
            bean.handle(body);
        });

        return body;
    }

    static private FilterItem handleFilterItem(FilterItem filter) {
        Object value = filter.getValue();

        if (value instanceof String && !Strings.isNullOrEmpty(String.valueOf(value))) {
            String strValue = (String) value;

            String simpleOperator = strValue.substring(0, 1);

            QueryOperator operator = filter.getOperator();
            if (simpleOperators.contains(simpleOperator)) {
                strValue = strValue.substring(1);

                switch (simpleOperator) {
                    case "!":
                        operator = QueryOperator.NEQ;
                        value = strValue;
                        break;
                    case "@":
                        operator = QueryOperator.IN;
                        strValue = strValue.replaceAll(",", "\\|");
                        value = Arrays.asList(strValue.split("\\|"));
                        break;
                    case "#":
                        operator = QueryOperator.BTW;
                        value = Arrays.asList(strValue.replaceAll(",", "\\|").split("\\|"));
                        if (((List<String>) value).size() < 2) {
                            throw new ParameterInvalidException(
                                    CrowMessageUtil.error(CrowErrorMessage.BetweenQueryNeedTwoParams)
                            );
                        }
                        break;
                    case ")":
                        operator = QueryOperator.EGT;
                        value = strValue;
                        break;
                    case "(":
                        operator = QueryOperator.ELT;
                        value = strValue;
                        break;
                    case ">":
                        operator = QueryOperator.GT;
                        value = strValue;
                        break;
                    case "<":
                        operator = QueryOperator.LT;
                        value = strValue;
                        break;
                    case "^":
                        operator = QueryOperator.LIKE;
                        value = strValue;
                        break;
                }

                filter.setOperator(operator);
                filter.setValue(value);
            }
        }

        return filter;
    }

    static public Sort handleSortItem(String sortStr) {
        List<Sort.Order> orders = new ArrayList<>();

        // sorter style like: [{property: foo, direction: ASC}]
        if(sortStr.startsWith("[")) {
            List<Map<String, ?>> sorters;
            try {
                ObjectMapper objectMapper = CrowBeanUtils.getBean(ObjectMapper.class);
                sorters = objectMapper.readValue(sortStr, List.class);
            } catch (IOException e) {
                sorters = new ArrayList<>();
            }

            for (Map<String, ?> sorter: sorters) {
                String property = (String) sorter.get("property");
                String direction = (String) sorter.get("direction");
                Sort.Order order = new Sort.Order(
                        "ASC".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC,
                        property
                );
                orders.add(order);
            }
        } else { // sorter style like: -id
            if(!sortStr.startsWith("+") && !sortStr.startsWith("-")) {
                sortStr = "+" + sortStr;
            }
            String direction = sortStr.substring(0, 1);
            String property = sortStr.substring(1);

            Sort.Order order = new Sort.Order(
                    "+".equals(direction) ? Sort.Direction.ASC : Sort.Direction.DESC,
                    property
            );
            orders.add(order);
        }

        return Sort.by(orders);
    }

}
