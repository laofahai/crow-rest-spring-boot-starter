package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.utils.GenericUtils;

/**
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 * @param <V> The VO
 * @param <D> The DTO
 */
public interface CrowController<
        ID,
        T extends ICrowEntity<ID, V>,
        V extends ICrowIO,
        D extends ICrowIO
        > {

    ICrowDBService<ID, T> getCrowProvider();

    default Class<ID> getIdCls(){
        return (Class<ID>) GenericUtils.get(this.getClass(), 0);
    }

    default Class<T> getEntityCls() {
        return (Class<T>) GenericUtils.get(this.getClass(), 1);
    }

    default Class<V> getVoCls() {
        return (Class<V>) GenericUtils.get(this.getClass(), 2);
    }

    default Class<D> getDtoCls() {
        return (Class<D>) GenericUtils.get(this.getClass(), 3);
    }

}
