package org.teamswift.crow.rest.provider.jpa;

import org.teamswift.crow.rest.common.CrowController;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.utils.DozerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
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
        T extends ICrowEntity<ID>,
        V extends ICrowIO,
        D extends ICrowIO>
        implements CrowController<ID, T, V, D> {

    @PersistenceContext private EntityManager entityManager;

    @Autowired private CrowServiceProperties properties;

    public ICrowDBService<ID, T> getDBService() {
        return new CrowDBServiceJpa<>(getEntityCls(), entityManager);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ICrowListResult<V> findAll(HttpServletRequest request) {
        ICrowListResult<T> raw = getDBService().findAll(request);

        return CrowResult.ofList(
                raw, getVoCls()
        );
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ICrowResult<V> findOneById(@PathVariable ID id) {
        Optional<T> raw = getDBService().findOneById(id);

        if(raw.isPresent()) {
            V mapped = DozerUtils.map(raw.get(), getVoCls());
            return CrowResult.ofSuccess(mapped);
        }

        throw new DataNotFoundException("Data can't be founded according to the provided ID");
    }

    public T store(D dto) {
        T result = getDBService().store(null);

        return null;
    }
}