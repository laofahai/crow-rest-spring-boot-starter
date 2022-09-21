package org.teamswift.crow.rest.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import jdk.jfr.Label;
import lombok.Getter;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Service;
import org.teamswift.crow.rest.annotation.*;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.common.ICrowVo;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.handler.dataStructure.DataType;
import org.teamswift.crow.rest.handler.dataStructure.EntityMeta;
import org.teamswift.crow.rest.handler.dataStructure.FieldStructure;
import org.teamswift.crow.rest.handler.requestParams.QueryOperator;
import org.teamswift.crow.rest.utils.EnumUtils;
import org.teamswift.crow.rest.utils.GenericUtils;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@Getter
public class CrowDataStructureService {

    @PersistenceContext private final EntityManager entityManager;

    private final Map<String, EntityMeta> entitiesDataStructureMap = new HashMap<>();

    private final Map<String, EntityMeta> voDataStructureMap = new HashMap<>();

    private final Map<Field, FieldStructure> allFieldsMap = new HashMap<>();

    private final Map<Class<? extends ICrowIO>, Class<? extends ICrowIO>> entityVoMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(CrowDataStructureService.class);

    private final List<QueryOperator> needSplitFormatOperator = Arrays.asList(
            QueryOperator.IN, QueryOperator.NIN, QueryOperator.BTW
    );

    private final List<String> defaultFields = Arrays.asList(
            "id", "createdBy", "modifiedBy", "createdAt", "modifiedAt", "deleted", "deletedDate",
            "used", "organizationId"
    );

    public CrowDataStructureService(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.register();
    }

    private void register() {
        MetamodelImpl model = (MetamodelImpl) entityManager.getEntityManagerFactory().getMetamodel();
        Map<String, EntityPersister> entityPersisters = model.entityPersisters();

        for(Map.Entry<String, EntityPersister> entry: entityPersisters.entrySet()) {
            EntityPersister persist = entry.getValue();
            Class<?> entityClsRaw = persist.getMappedClass();

            // only handle the entities belongs to ICrowEntity
//            if(!ICrowEntity.class.isAssignableFrom(entityClsRaw)) {
//                logger.warn(
//                        "The entity {} is not managed by crow", entityClsRaw.getName()
//                );
//                continue;
//            }

            Class<? extends ICrowIO> entityCls = (Class<? extends ICrowIO>) entityClsRaw;

            Class<? extends ICrowVo> voCls = (Class<? extends ICrowVo>) GenericUtils.get(entityCls, 1);

//            if(voCls == null) {
//                continue;
//            }

            // api path
            String apiPath = getApiPath(entityCls);

            // entity name and label
            String entityName = entityCls.getSimpleName();
            String entityLabel;
            if(entityCls.isAnnotationPresent(Label.class)) {
                entityLabel = entityCls.getAnnotation(Label.class).value();
            } else if(entityCls.isAnnotationPresent(I18N.class)) {
                entityLabel = entityCls.getAnnotation(I18N.class).value();
            } else {
                entityLabel = apiPath;
            }

            // vo class
            entityVoMap.put(entityCls, voCls);

            Map<String, FieldStructure> fieldStructureMap = new HashMap<>();
            Map<String, FieldStructure> voStructureMap = new HashMap<>();

            List<Field> fields = Scaffolds.getDeclareFieldsAll(entityCls);

            for(Field field: fields) {

                if(defaultFields.contains(field.getName())) {
                    continue;
                }

                FieldStructure fs = parseFieldStructure(field, entityCls, apiPath);
                fieldStructureMap.put(field.getName(), fs);

                if(voCls != null) {
                    FieldStructure voFs = parseFieldStructure(field, voCls, apiPath);
                    String voLabel = String.format("%s.%s", apiPath, field.getName());
                    if(Strings.isNullOrEmpty(voFs.getLabel()) || voFs.getLabel().equals(voLabel)) {
                        voFs.setLabel(fs.getLabel());
                    }
                    voStructureMap.put(field.getName(), voFs);
                }

                allFieldsMap.put(field, fs);
            }

            EntityMeta entityMeta = new EntityMeta();
            entityMeta.setApiPath(apiPath);
            entityMeta.setFieldsMap(fieldStructureMap);
            entityMeta.setLabel(entityLabel);
            entityMeta.setName(entityName);
            entityMeta.setBelongsToCrow(entityCls.getName().startsWith("org.teamswift.crow"));
            entityMeta.setVoFieldsMap(voStructureMap);
            this.entitiesDataStructureMap.put(apiPath, entityMeta);
        }
    }

    public FieldStructure parseFieldStructure(Field field, Class<? extends ICrowIO> cls, String apiPath) {
        field.setAccessible(true);

        FieldStructure fs = new FieldStructure();

        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(field.getName(), cls);
        } catch (IntrospectionException e) {
            logger.warn(
                    "Error while instance a new PropertyDescriptor for the field {} in {} ", field.getName(), cls.getName()
            );
            return fs;
        }
        // Field Type
        Class<?> type = pd.getPropertyType();

        fs.setKey(field.getName());

        Column column = field.getAnnotation(Column.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        Label label = field.getAnnotation(Label.class);
        I18N i18N = field.getAnnotation(I18N.class);
        Transient t = field.getAnnotation(Transient.class);

        // generated by system, can't modified from front-end
        if(field.isAnnotationPresent(SystemGeneratedValue.class)
                || field.isAnnotationPresent(Id.class)
                || field.isAnnotationPresent(CreatedBy.class)
                || field.isAnnotationPresent(CreatedDate.class)
                || field.isAnnotationPresent(LastModifiedBy.class)
                || field.isAnnotationPresent(LastModifiedDate.class)
        ) {
            fs.setSystemGenerated(true);
        }

        // virtual field.
        if(t != null) {
            fs.setVirtual(true);
        }

        // if this entity can pre-load in front-end widgets like select, combo, etc.
        if(field.isAnnotationPresent(ForeignPreLoad.class) || field.getType().isAnnotationPresent(ForeignPreLoad.class)) {
            fs.setPreload(true);
        }

        if(field.isAnnotationPresent(JsonIgnore.class)) {
            fs.setJsonIgnore(true);
        }

        // column
        if(column != null) {
            if(column.length() > 0) {
                fs.setMaxLength(column.length());
            }
            if(!column.nullable()) {
                fs.setRequired(true);
            }
            if(!column.updatable()) {
                fs.setUpdatable(false);
            }
        } else if(joinColumn != null) {
            if(!joinColumn.nullable()) {
                fs.setRequired(true);
            }
            if(!joinColumn.updatable()) {
                fs.setUpdatable(false);
            }
        }

        if(fs.getMaxLength() <= 0) {
            fs.setMaxLength(255);
        }

        // label
        if(label != null && !Strings.isNullOrEmpty(label.value())) {
            fs.setLabel(label.value());
        } else if(i18N != null) {
            fs.setLabel(i18N.value());
        } else {
            String labelDisplay = String.format("%s.%s", apiPath, field.getName());
            fs.setLabel(labelDisplay);
        }

        // type
        // Enum
        if(type.isEnum()) {
            fs.setType(DataType.ENUM);
            fs.setChoices(EnumUtils.enumToListMap(type));
        } else if(ICrowIO.class.isAssignableFrom(type)) {
            if(t == null) {
                // foreign
                if(type.isAnnotationPresent(ForeignDisplayField.class)) {
                    fs.setForeignDisplayField(type.getAnnotation(ForeignDisplayField.class).value());
                } else {
                    fs.setForeignDisplayField("name");
                }
                String foreignApiPath = getApiPath(type);
                fs.setForeignApi(foreignApiPath);
                fs.setType(DataType.FOREIGN);
            }
        } else {
            String simpleName = type.getSimpleName().toUpperCase();
            switch (simpleName) {
                case "BIGDECIMAL":
                    fs.setType(DataType.BIG_DECIMAL);
                    break;
                case "INTEGER":
                case "INT":
                case "LONG":
                case "SHORT":
                    fs.setType(DataType.NUMBER);
                    break;
                case "CHAR":
                case "STRING":
                    fs.setType(DataType.TEXT);
                    break;
                default:
                    if(t == null) {
                        fs.setType(DataType.valueOf(
                                simpleName
                        ));
                    }
                    break;
            }
        }

        field.setAccessible(false);

        return fs;
    }

    public String getApiPath(Class<?> cls) {
        String apiPath;
        CrowEntity crowEntityAnnotation = cls.getAnnotation(CrowEntity.class);
        if(crowEntityAnnotation != null && !Strings.isNullOrEmpty(crowEntityAnnotation.apiPath())) {
            apiPath = crowEntityAnnotation.apiPath();
        } else {
            String[] raw = cls.getName().split("\\.");
            raw = Arrays.copyOfRange(raw, raw.length - 3, raw.length);
            apiPath = String.format("%s.%s", raw[0], Scaffolds.lcfirst(raw[2]));
        }
        return apiPath;
    }

    public Object tryTransValue(Field field, QueryOperator queryOperator, Object value) {

        if(value instanceof List) {
            List<Object> result = new ArrayList<>();
            for(Object valueItem: (List<?>) value) {
                result.add(tryTransValue(field, queryOperator, valueItem));
            }
            return result;
        }

        FieldStructure fieldConfig = allFieldsMap.get(field);
        if(fieldConfig == null) {
            return value;
        }

        // 需要变成 list
        DataType dataType = fieldConfig.getType();

        if(needSplitFormatOperator.contains(queryOperator)) {
            String[] tmp = String.valueOf(value).replaceAll(",", "\\|").split("\\|");
            List<Object> result = new ArrayList<>();
            for(String str: tmp) {
                result.add(getResultValue(dataType.name().toLowerCase(), field, value));
            }
            return result;
        } else {
            return getResultValue(dataType.name().toLowerCase(), field, value);
        }
    }

    private <T extends ICrowEntity<Integer, ?>> Object getResultValue(String dataType, Field field, Object value) {
        Object resultValue;
        switch(dataType) {
            case "date":
                resultValue = Scaffolds.tryTransDateString(value);
                break;
            case "number":
            case "integer":
                resultValue = Scaffolds.inputValueToInteger(value);
                break;
            case "bigdecimal":
                resultValue = Scaffolds.inputValueToDecimal(value);
                break;
            case "boolean":
                resultValue = Scaffolds.inputValueToBoolean(value);
                break;
            case "enum":
                String fullName = field.getType().getName();
                Method method;
                try {
                    Class<?> cls = Class.forName(fullName);
                    method = cls.getDeclaredMethod("valueOf", String.class);
                    if(value instanceof Collection) {
                        List<Object> result = new ArrayList<>();
                        for(Object vItem: (Collection<?>) value) {
                            result.add(method.invoke(null, vItem));
                        }
                        resultValue = result;
                    } else {
                        resultValue = method.invoke(null, value);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new BusinessException("枚举类型转换错误：" + value + "; " + e.getMessage());
                }
                break;
            case "foreign":
                T entity;
                try {
                    if(value instanceof Collection) {
                        List<Object> result = new ArrayList<>();
                        for(Object vItem: (Collection<?>) value) {
                            T ne = (T) field.getType().getDeclaredConstructor().newInstance();
                            ne.setId(Scaffolds.inputValueToInteger(vItem));
                            result.add(ne);
                        }
                        resultValue = result;
                    } else if(value instanceof ICrowEntity) {
                        resultValue = value;
                    } else {
                        entity = (T) field.getType().getDeclaredConstructor().newInstance();
                        entity.setId(Scaffolds.inputValueToInteger(value));
                        resultValue = entity;
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new BusinessException("外键数据转换实例化错误：" + field.getType().getName() + "#" + value);
                }
                break;
            default:
                resultValue = value;
        }

        return resultValue;
    }

}
