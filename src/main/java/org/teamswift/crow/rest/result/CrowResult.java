package org.teamswift.crow.rest.result;

import org.springframework.http.HttpStatus;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.InternalServerException;
import org.teamswift.crow.rest.result.impl.CrowErrorResult;
import org.teamswift.crow.rest.result.impl.CrowListResult;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.DozerUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class CrowResult {

    static private final CrowServiceProperties properties = CrowBeanUtils.getBean(CrowServiceProperties.class);

    static public CrowErrorResult ofError(BusinessException e) {
        return new CrowErrorResult(e);
    }

    static public CrowErrorResult ofError(Exception e) {
        if(e instanceof BusinessException) {
            return new CrowErrorResult((BusinessException) e);
        }
        CrowErrorResult result = new CrowErrorResult();
        result.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setSuccess(false);
        result.setData(e.getLocalizedMessage());
        result.setResultCode(CrowResultCode.SYSTEM_INNER_ERROR);
        result.setTitle(
                CrowMessageUtil.get("crow.titles.errorOccurred")
        );

        return result;
    }

    static public <E> ICrowResult<E> ofSuccess(E data) {
        return ofSuccess(data, properties.getDefaultResultClass());
    }

    static public <E, R extends ICrowResult<E>> ICrowResult<E> ofSuccess(E data, Class<R> resultCls) {
        try {
            R result = resultCls.getDeclaredConstructor().newInstance();
            result.setData(data);
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.CustomResultClass, e.getLocalizedMessage())
            );
        }
    }

    static public <E, L extends Collection<E>> ICrowListResult<E> ofList(L list, int totalItems, int page, int pageSize) {
        return ofList(list, totalItems, page, pageSize, properties.getDefaultListResultClass());
    }

    static public <E, V extends ICrowIO> ICrowListResult<V> ofList(ICrowListResult<E> rawResult, Class<V> cls) {
        List<V> mappedList = DozerUtils.mapList(rawResult.getData(), cls);

        return new CrowListResult<>(
                mappedList,
                rawResult.getTotalItems(),
                rawResult.getPage(),
                rawResult.getPageSize());
    }

    /**
     * Short cuts for create an ICrowListResult via custom result-class
     * @param data The original list or entities
     * @param totalItems
     * @param page
     * @param pageSize
     * @param resultCls
     * @param <E>
     * @param <R>
     * @param <L>
     * @return ICrowListResult<E>
     */
    static public <E, R extends ICrowListResult<E>, L extends Collection<E>> ICrowListResult<E> ofList(
            L data, int totalItems, int page, int pageSize, Class<R> resultCls
    ) {
        try {
            return resultCls.getDeclaredConstructor(
                    Object.class, Integer.class, Integer.class, Integer.class
            ).newInstance(data, totalItems, page, pageSize);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.CustomResultClass, e.getLocalizedMessage())
            );
        }
    }

}
