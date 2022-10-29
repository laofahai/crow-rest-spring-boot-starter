package org.teamswift.crow.rest.provider.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.teamswift.crow.rest.common.CrowController;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
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
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
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
abstract public class CrowControllerJpa<
        ID extends Serializable,
        T extends ICrowEntity<ID, V>,
        V extends ICrowIO,
        D extends ICrowIO>
        implements CrowController<ID, T, V, D> {

    @PersistenceContext private EntityManager entityManager;

    public ICrowDBService<ID, D, T> getCrowProvider() {
        return new CrowDBServiceJpa<>(getEntityCls(), entityManager);
    }



}