package org.teamswift.crow.rest.provider.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.teamswift.crow.rest.common.CrowController;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.handler.RequestBodyResolveHandler;
import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.DozerUtils;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * The basic Controller based on JPA provider. It's defined to abstract so you must create
 * your own controller based on this class. And it's highly recommend to create a new base-controller.
 * @param <ID> The primary key type of Entity
 * @param <T> The Entity
 * @param <V> The VO
 * @param <D> The DTO
 */
@RestController
abstract public class CrowControllerJpa<
        ID,
        T extends ICrowEntity<ID, V>,
        V extends ICrowIO,
        D extends ICrowIO>
        implements CrowController<ID, T, V, D> {

    @PersistenceContext private EntityManager entityManager;

    @Autowired private CrowServiceProperties properties;

    public ICrowDBService<ID, T> getCrowProvider() {
        return new CrowDBServiceJpa<>(getEntityCls(), entityManager);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ICrowListResult<V> findAll(HttpServletRequest request) {
        RequestBodyResolved body = RequestBodyResolveHandler.handle(request, getEntityCls());
        ICrowListResult<T> raw = getCrowProvider().findAll(body);

        return CrowResult.ofList(
                raw, getVoCls()
        );
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ICrowResult<V> findOneById(@PathVariable ID id) {
        Optional<T> raw = getCrowProvider().findOneById(id);

        if(raw.isPresent()) {
            V mapped = DozerUtils.map(raw.get(), getVoCls());
            return CrowResult.ofSuccess(mapped);
        }

        throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
    }

    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    public ICrowResult<V> store(@RequestBody D dto) {
        T entity = DozerUtils.map(dto, getEntityCls());
        entity = getCrowProvider().store(entity);

        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    @Transactional
    public ICrowResult<V> update(@PathVariable ID id, @RequestBody D dto) {
        T toUpdate = DozerUtils.map(dto, getEntityCls());
        T entity = getCrowProvider().update(id, toUpdate);
        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @Transactional
    public ICrowResult<?> delete(@PathVariable ID id) {
        ICrowDBService<ID, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });
        T deleted = provider.softDelete(entity);

        return CrowResult.ofSuccess(deleted);
    }

    /**
     * @param ids
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/batch/{ids}")
    @Transactional
    public ICrowResult<?> deleteBatch(@PathVariable String ids) {
        List<ID> idList = Scaffolds.parseIdStringToGeneric(ids, getIdCls());

        ICrowDBService<ID, T> provider = getCrowProvider();
        ICrowListResult<T> list = provider.findAllByIdsIn(idList);
        int num = provider.softDeleteBatch(list.getData());

        return CrowResult.ofSuccess(num);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/destroy/{id}")
    @Transactional
    public ICrowResult<?> destroy(@PathVariable ID id) {
        ICrowDBService<ID, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });
        provider.destroy(entity);

        return CrowResult.ofSuccess("");
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/restore/{id}")
    @Transactional
    public ICrowResult<V> restore(@PathVariable ID id) {
        ICrowDBService<ID, T> provider = getCrowProvider();
        T entity = provider.findOneById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });
        entity = provider.restore(entity);

        return CrowResult.ofSuccess(DozerUtils.map(entity, getVoCls()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/restoreBatch/{ids}")
    @Transactional
    public ICrowResult<?> restoreBatch(@PathVariable String ids) {
        List<ID> idList = Scaffolds.parseIdStringToGeneric(ids, getIdCls());

        ICrowDBService<ID, T> provider = getCrowProvider();
        int num = provider.restoreBatch(idList);

        return CrowResult.ofSuccess(num);
    }

}