package org.teamswift.crow.rest.provider.mybatisPlus;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.configure.CrowServiceProperties;
import org.teamswift.crow.rest.enums.PresetTableFields;
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The JPA provider for database service, implemented most of the common CRUD operation via JPA.
 * @param <ID>
 * @param <T>
 */
public class CrowDBServiceMybatisPlus<
            ID extends Serializable,
            V extends ICrowIO,
            D extends ICrowIO,
            T extends ICrowEntity<ID, V>,
            M extends BaseMapper<T>
        >
        implements ICrowDBService<ID, D, T> {

    private final CrowServiceProperties properties = CrowBeanUtils.getBean(CrowServiceProperties.class);

    private final ObjectMapper objectMapper = CrowBeanUtils.getBean(ObjectMapper.class);

    private final CrowDataStructureService dataStructureService = CrowBeanUtils.getBean(CrowDataStructureService.class);

    private final Class<T> entityCls;

    private final Logger logger = LoggerFactory.getLogger(CrowDBServiceMybatisPlus.class);

    private final M mapper;

    public CrowDBServiceMybatisPlus(Class<T> domainClass, M mapper) {
        this.entityCls = domainClass;
        this.mapper = mapper;
    }

    public CrowServiceProperties getProperties() {
        return properties;
    }

    @Override
    public ICrowListResult<T> findAll(RequestBodyResolved body) {

        EntityMeta entityMeta = dataStructureService.getEntitiesDataStructureMap().get(
                dataStructureService.getApiPath(entityCls)
        );

        CrowQueryBuilderMybatisPlus<ID, V, T> queryWrapper = new CrowQueryBuilderMybatisPlus<>();

        if(entityMeta == null) {
            return null;
        }

        if(entityMeta.isSoftDelete()) {
            if(body.isOnlyDeleted()) {
                queryWrapper.isNotNull(PresetTableFields.DeletedTime.getName());
            } else {
                queryWrapper.isNull(PresetTableFields.DeletedTime.getName());
            }
        }

        for(FilterItem filterItem: body.getFilterItems()) {
            try {
                // `name|symbol = "foo"` style, means `name = foo or symbol = foo`
                if(filterItem.getField().contains("|")) {
                    List<FieldStructure> fieldStructures = new ArrayList<>();
                    for(String orField: filterItem.getField().split("\\|")) {
                        FieldStructure fieldConfig = entityMeta.getFieldsMap().get(orField);
                        if(fieldConfig == null) {
                            continue;
                        }
                        fieldStructures.add(fieldConfig);
                    }
                    Method method = queryWrapper.getClass().getDeclaredMethod(
                            filterItem.getOperator().getOperatorName(),
                            List.class, Object.class, Object.class
                    );
                    queryWrapper = (CrowQueryBuilderMybatisPlus<ID, V, T>) method.invoke(
                            queryWrapper,
                            fieldStructures,
                            filterItem.getValue(),
                            filterItem.getValue()
                    );
                    // normal style
                } else {
                    Method method = queryWrapper.getClass().getDeclaredMethod(
                            filterItem.getOperator().getOperatorName(),
                            FieldStructure.class, Object.class, Object.class
                    );

                    FieldStructure fieldConfig = entityMeta.getFieldsMap().get(filterItem.getField());
                    queryWrapper = (CrowQueryBuilderMybatisPlus<ID, V, T>) method.invoke(
                            queryWrapper,
                            fieldConfig,
                            filterItem.getValue(),
                            filterItem.getValue()
                    );
                }

            } catch (Exception e) {
                logger.error("Error while using custom-result-class: {}", e.getLocalizedMessage());
                throw new InternalServerException(
                        CrowMessageUtil.error(CrowErrorMessage.CustomResultClass, e.getLocalizedMessage())
                );
            }
        }

        Sort sortOrder = body.getSortOrders();
        Map<String, FieldStructure> fieldsMap = entityMeta.getFieldsMap();
        for(Sort.Order sort: sortOrder) {
            if(!fieldsMap.containsKey(sort.getProperty())) {
                continue;
            }
            if(sort.isDescending()) {
                queryWrapper.orderByDesc(
                        fieldsMap.get(sort.getProperty()).getPhysicalFieldName()
                );
            } else {
                queryWrapper.orderByAsc(
                        fieldsMap.get(sort.getProperty()).getPhysicalFieldName()
                );
            }
        }

        assert properties != null;
        ICrowListResult<T> result = properties.getListResultInstance();

        int pageNumber = body.getPage();
        int pageSize = body.getPageSize();

        Page<T> page = new Page<>(
                pageNumber,
                pageSize
        );

        if(pageSize > 0 && pageNumber > 0) {
            Page<T> pageResult = mapper.selectPage(page, queryWrapper);
            result.setPage(pageNumber);
            result.setPageSize(pageSize);
            result.setTotalPages(Math.toIntExact(pageResult.getPages()));
            result.setTotalItems(pageResult.getTotal());
            result.setData(pageResult.getRecords());
        } else {
            List<T> list = mapper.selectList(queryWrapper);
            result.setData(list);
        }

        return result;
    }

    @Override
    public ICrowListResult<T> findAllByIdsIn(Collection<ID> idList) {
        return findAllByIdsIn(idList, false);
    }

    @Override
    public ICrowListResult<T> findAllByIdsIn(Collection<ID> idList, boolean onlyTrash) {
        RequestBodyResolved bodyResolved = new RequestBodyResolved();
        List<FilterItem> filterItems = new ArrayList<>(){{
            add(new FilterItem(
                    "id", QueryOperator.IN, idList
            ));
        }};
        bodyResolved.setFilterItems(filterItems);
        bodyResolved.setPage(1);
        bodyResolved.setOnlyDeleted(onlyTrash);
        bodyResolved.setPageSize(idList.size());
        bodyResolved.setSortOrders(RequestBodyResolveHandler.handleSortItem("-id"));

        return findAll(bodyResolved);
    }

    @Override
    public Optional<T> findOneById(ID id) {
        T result = mapper.selectById(id);
        return Optional.ofNullable(result);
    }

    @Override
    public T store(T entity) {
        mapper.insert(entity);
        return entity;
    }

    @Override
    public T update(ID id, D dto) {

        T exists = mapper.selectById(id);
        if(exists == null) {
            throw new DataNotFoundException(CrowMessageUtil.error(CrowErrorMessage.NotFoundByID));
        }

        EntityMeta entityMeta = dataStructureService.getEntitiesDataStructureMap().get(
                dataStructureService.getApiPath(entityCls)
        );

        if(entityMeta == null) {
            logger.error("Entity not managed by crow: {}", entityCls.getSimpleName());
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.EntityMustManagedByCrow, entityCls.getSimpleName())
            );
        }

        Map<String, FieldStructure> fieldsMap = entityMeta.getFieldsMap();
        for(String fieldName: fieldsMap.keySet()) {
            FieldStructure fs = fieldsMap.get(fieldName);
            if(fs.isVirtual() || fs.isSystemGenerated() || !fs.isUpdatable()) {
                continue;
            }

            Object value = setUpdateFieldValue(fieldName, dto, exists);
            if(value == null) {
                logger.warn("Error while set entity property {} in {}, value: {}", fieldName, exists.getClass(), value);
            }
        }

        mapper.updateById(exists);
        return exists;
    }

    @Override
    public T softDelete(T entity) {
        entity.setDeletedDate(new Date());
        mapper.updateById(entity);
        return entity;
    }

    @Override
    public int softDeleteBatch(Collection<T> entities) {
        UpdateWrapper<T> queryWrapper = new UpdateWrapper<>();
        queryWrapper.in("id", entities.stream().map(ICrowEntity::getId).collect(Collectors.toList()));
        queryWrapper.set(PresetTableFields.DeletedTime.getName(), new Date());
        mapper.update(null, queryWrapper);
        return 0;
    }

    @Override
    public T restore(T entity) {
        entity.setDeletedDate(null);
        mapper.updateById(entity);
        return entity;
    }

    @Override
    public int restoreBatch(Collection<ID> entities) {
        UpdateWrapper<T> queryWrapper = new UpdateWrapper<>();
        queryWrapper.in("id", entities);
        queryWrapper.set(PresetTableFields.DeletedTime.getName(), null);
        mapper.update(null, queryWrapper);
        return 0;
    }

    @Override
    public int destroy(T entity) {
        return mapper.deleteById(entity);
    }

    @Override
    public int destroyBatch(Collection<ID> idList) {
        return mapper.deleteBatchIds(idList);
    }
}
