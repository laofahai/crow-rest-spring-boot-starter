package org.teamswift.crow.rest.provider.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.exception.impl.InternalServerException;
import org.teamswift.crow.rest.handler.RequestBodyResolveHandler;
import org.teamswift.crow.rest.handler.dataStructure.EntityMeta;
import org.teamswift.crow.rest.handler.dataStructure.FieldStructure;
import org.teamswift.crow.rest.handler.requestParams.FilterItem;
import org.teamswift.crow.rest.handler.requestParams.QueryOperator;
import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.service.CrowDataStructureService;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.Scaffolds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The JPA provider for database service, implemented most of the common CRUD operation via JPA.
 * @param <ID>
 * @param <T>
 */
public class CrowDBServiceJpa<
            ID, T extends ICrowEntity<ID, ?>
        >
        extends SimpleJpaRepository<T, ID>
        implements ICrowDBService<ID, T> {

    private final EntityManager entityManager;

    private final CrowServiceProperties properties = CrowBeanUtils.getBean(CrowServiceProperties.class);

    private final ObjectMapper objectMapper = CrowBeanUtils.getBean(ObjectMapper.class);

    private final CrowDataStructureService dataStructureService = CrowBeanUtils.getBean(CrowDataStructureService.class);

    private final Class<T> entityCls;

    private final Logger logger = LoggerFactory.getLogger(CrowDBServiceJpa.class);

    public CrowDBServiceJpa(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        entityCls = domainClass;
        entityManager = em;
    }

    public CrowServiceProperties getProperties() {
        return properties;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public ICrowListResult<T> findAllByIdsIn(Collection<ID> idList, boolean onlyDeleted) {
        RequestBodyResolved bodyResolved = new RequestBodyResolved();
        List<FilterItem> filterItems = new ArrayList<>(){{
            add(new FilterItem(
                    "id", QueryOperator.IN, idList
            ));
        }};
        bodyResolved.setOnlyDeleted(onlyDeleted);
        bodyResolved.setFilterItems(filterItems);
        bodyResolved.setPage(1);
        bodyResolved.setPageSize(idList.size());
        bodyResolved.setSortOrders(RequestBodyResolveHandler.handleSortItem("-id"));

        return findAll(bodyResolved);
    }


    @Override
    public ICrowListResult<T> findAllByIdsIn(Collection<ID> idList) {
        return findAllByIdsIn(idList, false);
    }

    @Override
    public ICrowListResult<T> findAll(RequestBodyResolved body) {

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

                        Method method = CrowQueryBuilder.class.getDeclaredMethod(
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
                        Method method = CrowQueryBuilder.class.getDeclaredMethod(
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
                    logger.error("Error while using custom-result-class: {}", e.getMessage());
                    throw new InternalServerException(
                            CrowMessageUtil.error(CrowErrorMessage.CustomResultClass, e.getMessage())
                    );
                }
            }

            return condition;
        });

        ICrowListResult<T> result = properties.getListResultInstance();

        if(body.isOnlyCount()) {
            result.setTotalItems(count(spec));
            return result;
        }

        PageRequest page = PageRequest.of(body.getPage() - 1, body.getPageSize(), body.getSortOrders());

        Page<T> rawPage = findAll(spec, page);

        result.setPage(rawPage.getPageable().getPageNumber() + 1);
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
    public Optional<T> findOneBy(String field, Object value) {
        Specification<T> spec = (root, query, criteriaBuilder) -> {
            Path<Object> path = null;
            if(field.contains(".")) {
                String[] pathArray = field.split("\\.");
                for(String p: pathArray) {
                    path = Objects.requireNonNullElse(path, root).get(p);
                }
            } else {
                path = root.get(field);
            }

            return criteriaBuilder.equal(
                    path, value
            );
        };
        return findOne(spec);
    }

    @Override
    public T store(T entity) {
        return super.save(entity);
    }

    @Override
    public T update(ID id, T dto) {

        Class<T> domainCls = getEntityCls();

        T exists = findById(id).orElseThrow(() -> {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        });

        EntityMeta entityConfiguration = dataStructureService.getEntitiesDataStructureMap().get(
                dataStructureService.getApiPath(getEntityCls())
        );

        if(entityConfiguration == null) {
            logger.error("Entity not managed by crow: {}", domainCls.getSimpleName());
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.EntityMustManagedByCrow, domainCls.getSimpleName())
            );
        }

        Map<String, FieldStructure> fieldsMap = entityConfiguration.getFieldsMap();
        for(String fieldName: fieldsMap.keySet()) {
            FieldStructure fs = fieldsMap.get(fieldName);
            if(fs.isVirtual() || fs.isSystemGenerated() || !fs.isUpdatable()) {
                continue;
            }

            Object value = null;
            try {
                // get new value
                PropertyDescriptor pd = new PropertyDescriptor(fieldName, dto.getClass());
                Method readMethod = pd.getReadMethod();
                value = readMethod.invoke(dto);

                PropertyDescriptor pdExists = new PropertyDescriptor(fieldName, domainCls);
                Method method = pdExists.getWriteMethod();
                method.invoke(exists, value);
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
                logger.warn("Error while set entity property {} in {}, value: {}", fieldName, dto.getClass(), value);
                continue;
            }
        }

        return super.save(exists);
    }

    @Override
    public T softDelete(T entity) {
        entity.setDeleted(true);
        return save(entity);
    }

    @Override
    public int softDeleteBatch(Collection<T> entities) {
        String hql = String.format("update %s as e set e.deletedDate = :date where e in :entities", getEntityCls().getSimpleName());
        Query query = getEntityManager().createQuery(hql);
        query.setParameter("date", new Date());
        query.setParameter("entities", entities);
        return query.executeUpdate();
    }

    @Override
    public T restore(T entity) {
        entity.setDeleted(false);
        return save(entity);
    }

    @Override
    public int restoreBatch(Collection<ID> idList) {
        ICrowListResult<T> result = findAllByIdsIn(idList, true);

        String hql = String.format("update %s e set e.deletedDate = null where e in :entities", getEntityCls().getSimpleName());
        Query query = getEntityManager().createQuery(hql);
        query.setParameter("entities", result.getData());
        return query.executeUpdate();
    }

    @Override
    public void destroy(T entity) {
        delete(entity);
    }

    @Override
    public int destroyBatch(Collection<ID> idList) {
        ICrowListResult<T> result = findAllByIdsIn(idList, true);

        String hql = String.format("delete from %s e where e in :entities", getEntityCls().getSimpleName());
        Query query = getEntityManager().createQuery(hql);
        query.setParameter("entities", result.getData());
        return query.executeUpdate();
    }

    public Class<T> getEntityCls() {
        return entityCls;
    }


}
