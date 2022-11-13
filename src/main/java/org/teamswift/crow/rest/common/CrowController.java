package org.teamswift.crow.rest.common;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.teamswift.crow.rest.annotation.SoftDelete;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.handler.RequestBodyResolveHandler;
import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.DozerUtils;
import org.teamswift.crow.rest.utils.GenericUtils;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 * @param <V> The VO
 * @param <D> The DTO
 */
public interface CrowController<
        ID extends Serializable,
        T extends ICrowEntity<ID, V>,
        V extends ICrowIO,
        D extends ICrowIO
        > {

    ICrowDBService<ID, D, T> getCrowProvider();

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

    @RequestMapping(method = RequestMethod.GET)
    default ICrowListResult<V> findAll(HttpServletRequest request) {
        RequestBodyResolved body = RequestBodyResolveHandler.handle(request, getEntityCls());
        ICrowListResult<T> raw = getCrowProvider().findAll(body);

        return CrowResult.ofList(
                raw, getVoCls()
        );
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    default ICrowResult<V> findOneById(@PathVariable ID id) {
        Optional<T> raw = getCrowProvider().findOneById(id);

        if(raw.isPresent()) {
            V mapped = DozerUtils.map(raw.get(), getVoCls());
            return CrowResult.ofSuccess(mapped);
        }

        throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
    }

    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    default ICrowResult<V> store(@RequestBody @Validated D dto) {
        T entity = DozerUtils.map(dto, getEntityCls());
        entity.setId(null);
        entity = getCrowProvider().store(entity);
        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    @Transactional
    default ICrowResult<V> update(@PathVariable ID id, @RequestBody @Validated D dto) {
        T entity = getCrowProvider().update(id, dto);
        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @Transactional
    default ICrowResult<?> delete(@PathVariable ID id) {
        ICrowDBService<ID, D, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });

        if(entity.getClass().isAnnotationPresent(SoftDelete.class)) {
            T deleted = provider.softDelete(entity);
            return CrowResult.ofSuccess(deleted);
        } else {
            return destroy(id);
        }
    }

    /**
     * @param ids
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/batch/{ids}")
    @Transactional
    default ICrowResult<?> deleteBatch(@PathVariable String ids) {
        List<ID> idList = Scaffolds.parseIdStringToGeneric(ids, getIdCls());

        ICrowDBService<ID, D, T> provider = getCrowProvider();
        ICrowListResult<T> list = provider.findAllByIdsIn(idList);

        if(getEntityCls().isAnnotationPresent(SoftDelete.class)) {
            int num = provider.softDeleteBatch(list.getData());
            return CrowResult.ofSuccess(num);
        } else {
            return destroyBatch(ids);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/destroy/{id}")
    @Transactional
    default ICrowResult<?> destroy(@PathVariable ID id) {
        ICrowDBService<ID, D, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });

        if(entity.getClass().isAnnotationPresent(SoftDelete.class) && !entity.isDeleted()) {
            throw new BusinessException(CrowMessageUtil.error(CrowErrorMessage.EntityNotBeenSoftDeleted));
        }

        provider.destroy(entity);

        return CrowResult.ofSuccess("");
    }

    /**
     * @param ids
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/destroyBatch/{ids}")
    @Transactional
    default ICrowResult<?> destroyBatch(@PathVariable String ids) {
        List<ID> idList = Scaffolds.parseIdStringToGeneric(ids, getIdCls());

        ICrowDBService<ID, D, T> provider = getCrowProvider();
        int num = provider.destroyBatch(idList);

        return CrowResult.ofSuccess(num);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/restore/{id}")
    @Transactional
    default ICrowResult<V> restore(@PathVariable ID id) {
        ICrowDBService<ID, D, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });
        entity = provider.restore(entity);

        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/restoreBatch/{ids}")
    @Transactional
    default ICrowResult<?> restoreBatch(@PathVariable String ids) {
        List<ID> idList = Scaffolds.parseIdStringToGeneric(ids, getIdCls());

        ICrowDBService<ID, D, T> provider = getCrowProvider();
        int num = provider.restoreBatch(idList);

        return CrowResult.ofSuccess(num);
    }

}
