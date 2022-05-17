package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.result.ICrowListResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 */
public interface ICrowDBService<ID, T extends ICrowEntity<ID, ?>> {

    ICrowListResult<T> findAll(HttpServletRequest request);

    Optional<T> findOneById(ID id);

    Optional<T> findOneBy(String path, Object value);

    T store(T entity);



}
