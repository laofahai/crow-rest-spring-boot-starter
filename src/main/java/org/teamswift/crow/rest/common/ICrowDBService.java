package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.ICrowListResult;

import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 */
public interface ICrowDBService<
        ID extends Serializable,
        D extends ICrowIO,
        T extends ICrowEntity<ID, ?>
        > {

    ICrowListResult<T> findAll(RequestBodyResolved body);

    ICrowListResult<T> findAllByIdsIn(Collection<ID> idList);

    ICrowListResult<T> findAllByIdsIn(Collection<ID> idList, boolean onlyTrash);

    Optional<T> findOneById(ID id);

    T store(T entity);

    T update(ID id, D entity);

    T softDelete(T entity);

    int softDeleteBatch(Collection<T> entities);

    T restore(T entity);

    int restoreBatch(Collection<ID> entities);

    int destroy(T entity);

    int destroyBatch(Collection<ID> idList);

    default Object setUpdateFieldValue(String fieldName, D dto, T exists) {
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(fieldName, dto.getClass());
            Method readMethod = pd.getReadMethod();
            Object value = readMethod.invoke(dto);

            PropertyDescriptor pdExists = new PropertyDescriptor(fieldName, exists.getClass());
            Method method = pdExists.getWriteMethod();
            method.invoke(exists, value);
            return value;
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }

    }

}
