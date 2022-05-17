package org.teamswift.crow.rest.provider.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.exception.impl.InternalServerException;
import org.teamswift.crow.rest.handler.RequestBodyResolveHandler;
import org.teamswift.crow.rest.handler.requestParams.FilterItem;
import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.impl.CrowListResult;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.Scaffolds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The JPA provider for database service, implemented most of the common CRUD operation via JPA.
 * @param <ID>
 * @param <T>
 */
public class CrowDBServiceJpa<
            ID, T extends ICrowEntity<ID>
        >
        extends SimpleJpaRepository<T, ID>
        implements ICrowDBService<ID, T> {

    private final EntityManager entityManager;

    private final CrowServiceProperties properties = CrowBeanUtils.getBean(CrowServiceProperties.class);

    private final ObjectMapper objectMapper = CrowBeanUtils.getBean(ObjectMapper.class);

    public CrowDBServiceJpa(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        entityManager = em;
    }

    public CrowServiceProperties getProperties() {
        return properties;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ICrowListResult<T> findAll(HttpServletRequest request) {
        RequestBodyResolved body = RequestBodyResolveHandler.handle(request, getDomainClass());

        Specification<T> spec = ((root, query, criteriaBuilder) -> {
            Predicate condition;
            if(body.isOnlyDeleted()) {
                condition = criteriaBuilder.isNotNull(root.get("deletedDate"));
            } else {
                condition = criteriaBuilder.isNull(root.get("deletedDate"));
            }

            for(FilterItem filterItem: body.getFilterItems()) {
                try {
                    // `name|symbol = "foo"` style, means `name = foo or symbol = foo`
                    if(filterItem.getField().contains("|")) {
                        List<Path<?>> paths = new ArrayList<>();
                        for(String orField: filterItem.getField().split("\\|")) {
                            paths.add(Scaffolds.getExpressionPath(orField, root));
                        }

                        Method method = QueryOperator.class.getDeclaredMethod(
                                filterItem.getOperator().getOperatorName(),
                                List.class, Object.class, Object.class, Predicate.class, CriteriaBuilder.class
                        );

                        condition = (Predicate) method.invoke(
                                null,
                                paths,
                                filterItem.getValue(),
                                filterItem.getValue(),
                                condition, criteriaBuilder);
                    // normal style
                    } else {
                        Method method = QueryOperator.class.getDeclaredMethod(
                                filterItem.getOperator().getOperatorName(),
                                Expression.class, Object.class, Object.class, Predicate.class, CriteriaBuilder.class
                        );

                        condition = (Predicate) method.invoke(
                                null,
                                Scaffolds.getExpressionPath(filterItem.getField(), root),
                                filterItem.getValue(),
                                filterItem.getValue(),
                                condition, criteriaBuilder);
                    }

                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new InternalServerException("An error occurred when building the query:" + e.getMessage());
                }
            }

            return condition;
        });

        // start about query total count
        CrowListResult<T> result = new CrowListResult<>();
        result.setTotalItems(count(spec));
        // only return count
        if(body.isOnlyCount()) {
            return null;
        }
        // end of count

        PageRequest page = PageRequest.of(body.getPage() - 1, body.getPage(), body.getSortOrders());

        Page<T> rawPage = findAll(spec, page);

        result.setPage(rawPage.getPageable().getPageNumber());
        result.setPageSize(rawPage.getPageable().getPageSize());
        result.setTotalPages(rawPage.getTotalPages());
        result.setData(rawPage.getContent());
        return result;
    }

    @Override
    public Optional<T> findOneById(ID id) {
        return findOneBy("id", id);
    }

    @Override
    public Optional<T> findOneBy(String path, Object value) {
        Specification<T> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                root.get(path), value
        );
        return findOne(spec);
    }

    @Override
    public T store(T entity) {
        return super.save(entity);
    }

}
