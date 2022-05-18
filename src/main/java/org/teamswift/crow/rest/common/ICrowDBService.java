package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.ICrowListResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 */
public interface ICrowDBService<ID, T extends ICrowEntity<ID, ?>> {

    ICrowListResult<T> findAll(RequestBodyResolved body);

    ICrowListResult<T> findAllByIdsIn(Collection<ID> idList);

    Optional<T> findOneById(ID id);

    Optional<T> findOneBy(String path, Object value);

    T store(T entity);

    T update(ID id, T entity);

    T softDelete(T entity);

    int softDeleteBatch(Collection<T> entities);

    T restore(T entity);

    int restoreBatch(Collection<ID> entities);

    void destroy(T entity);

    void destroyBatch(Collection<T> entities);

}
