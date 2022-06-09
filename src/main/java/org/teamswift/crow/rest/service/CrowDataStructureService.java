package org.teamswift.crow.rest.service;

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
import org.teamswift.crow.rest.handler.dataStructure.DataType;
import org.teamswift.crow.rest.handler.dataStructure.EntityMeta;
import org.teamswift.crow.rest.handler.dataStructure.FieldStructure;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.EnumUtils;
import org.teamswift.crow.rest.utils.GenericUtils;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

@Service
@Getter
public class CrowDataStructureService {

    @PersistenceContext private final EntityManager entityManager;

    private final Map<String, EntityMeta> entitiesDataStructureMap = new HashMap<>();

    private final Map<String, EntityMeta> voDataStructureMap = new HashMap<>();

    private final Map<Class<? extends ICrowEntity<?, ?>>, Class<? extends ICrowIO>> entityVoMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(CrowDataStructureService.class);

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
            if(!ICrowEntity.class.isAssignableFrom(entityClsRaw)) {
                logger.warn(
                        "The entity {} is not managed by crow", entityClsRaw.getName()
                );
                continue;
            }

            Class<? extends ICrowEntity<?, ?>> entityCls = (Class<? extends ICrowEntity<?, ?>>) entityClsRaw;

            Class<? extends ICrowVo> voCls = (Class<? extends ICrowVo>) GenericUtils.get(entityCls, 1);

            if(voCls == null) {
                continue;
            }

            // api path
            String apiPath = getApiPath(entityCls);

            // entity name and label
            String entityName = entityCls.getSimpleName();
            String entityLabel;
            if(entityCls.isAnnotationPresent(Label.class)) {
                entityLabel = entityCls.getAnnotation(Label.class).value();
            } else if(entityCls.isAnnotationPresent(I18N.class)) {
                entityLabel = CrowMessageUtil.get(
                        entityCls.getAnnotation(I18N.class).value()
                );
            } else {
                entityLabel = CrowMessageUtil.entityLabel(apiPath);
            }

            // vo class
            entityVoMap.put(entityCls, voCls);

            Map<String, FieldStructure> fieldStructureMap = new HashMap<>();
            Map<String, FieldStructure> voStructureMap = new HashMap<>();

            List<Field> fields = Scaffolds.getDeclareFieldsAll(entityCls);

            for(Field field: fields) {
                FieldStructure fs = parseFieldStructure(field, entityCls, apiPath);
                FieldStructure voFs = parseFieldStructure(field, voCls, apiPath);

                String voLabel = String.format("%s.%s", apiPath, field.getName());
                voLabel = CrowMessageUtil.entityLabel(voLabel);
                if(Strings.isNullOrEmpty(voFs.getLabel()) || voFs.getLabel().equals(voLabel)) {
                    voFs.setLabel(fs.getLabel());
                }

                fieldStructureMap.put(field.getName(), fs);
                voStructureMap.put(field.getName(), voFs);
            }

            EntityMeta entityMeta = new EntityMeta();
            entityMeta.setApiPath(apiPath);
            entityMeta.setFieldsMap(fieldStructureMap);
            entityMeta.setLabel(entityLabel);
            entityMeta.setName(entityName);
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
            fs.setLabel(CrowMessageUtil.get(i18N.value()));
        } else {
            String labelDisplay = String.format("%s.%s", apiPath, field.getName());
            labelDisplay = CrowMessageUtil.entityLabel(labelDisplay);
            fs.setLabel(labelDisplay);
        }

        // type
        // Enum
        if(type.isEnum()) {
            fs.setType(DataType.ENUM);
            fs.setChoices(EnumUtils.enumToListMap(type));
        } else if(ICrowEntity.class.isAssignableFrom(type)) {
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
            apiPath = String.format("%s.%s", raw[0], raw[2]);
        }
        return apiPath;
    }

}
