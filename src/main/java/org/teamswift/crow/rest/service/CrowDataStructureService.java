package org.teamswift.crow.rest.service;

import com.google.common.base.Strings;
import jdk.jfr.Label;
import lombok.Getter;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Service;
import org.teamswift.crow.rest.annotation.CrowEntity;
import org.teamswift.crow.rest.annotation.ForeignDisplayField;
import org.teamswift.crow.rest.annotation.ForeignPreLoad;
import org.teamswift.crow.rest.annotation.SystemGeneratedValue;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.handler.dataStructure.DataType;
import org.teamswift.crow.rest.handler.dataStructure.EntityMeta;
import org.teamswift.crow.rest.handler.dataStructure.FieldStructure;
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

    private final Map<Class<? extends ICrowEntity<?, ?>>, Class<? extends ICrowIO>> entityVoMap = new HashMap<>();

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
                continue;
            }

            Class<? extends ICrowEntity<?, ?>> entityCls = (Class<? extends ICrowEntity<?, ?>>) entityClsRaw;

            // api path
            String apiPath = getApiPath(entityCls);

            // entity name and label
            String entityName = entityCls.getSimpleName();
            String entityLabel = entityName;
            if(entityCls.isAnnotationPresent(Label.class)) {
                entityLabel = entityCls.getAnnotation(Label.class).value();
            }

            // vo class
            Class<? extends ICrowIO> voCls = (Class<? extends ICrowIO>) GenericUtils.get(entityCls, 1);
            entityVoMap.put(entityCls, voCls);

            Map<String, FieldStructure> fieldStructureMap = new HashMap<>();

            List<Field> fields = Scaffolds.getDeclareFieldsAll(entityCls);

            for(Field field: fields) {

                field.setAccessible(true);

                PropertyDescriptor pd;
                try {
                    pd = new PropertyDescriptor(field.getName(), entityCls);
                } catch (IntrospectionException e) {
                    continue;
                }
                // Field Type
                Class<?> type = pd.getPropertyType();

                FieldStructure fs = new FieldStructure();
                fs.setKey(field.getName());

                Column column = field.getAnnotation(Column.class);
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                Label label = field.getAnnotation(Label.class);
                Transient t = field.getAnnotation(Transient.class);

                // generated by system, can't modified from front-end
                if(field.isAnnotationPresent(SystemGeneratedValue.class)) {
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
                } else {
                    fs.setLabel(field.getName());
                }

                // type
                // Enum
                if(type.isEnum()) {
                    fs.setType(DataType.ENUM);
                    fs.setChoices(EnumUtils.enumToListMap(type));
                } else if(t == null && ICrowEntity.class.isAssignableFrom(type)) {
                    // foreign
                    if(type.isAnnotationPresent(ForeignDisplayField.class)) {
                        fs.setForeignDisplayField(type.getAnnotation(ForeignDisplayField.class).value());
                    } else {
                        fs.setForeignDisplayField("name");
                    }
                    String foreignApiPath = getApiPath(type);
                    fs.setForeignApi(foreignApiPath);
                    fs.setType(DataType.FOREIGN);
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
                            fs.setType(DataType.valueOf(
                                    simpleName
                            ));
                            break;
                    }
                }

                field.setAccessible(false);

                fieldStructureMap.put(field.getName(), fs);
            }

            EntityMeta entityMeta = new EntityMeta();
            entityMeta.setApiPath(apiPath);
            entityMeta.setFieldsMap(fieldStructureMap);
            entityMeta.setLabel(entityLabel);
            entityMeta.setName(entityName);
            this.entitiesDataStructureMap.put(apiPath, entityMeta);
        }
    }

    public String getApiPath(Class<?> cls) {
        String apiPath;
        CrowEntity crowEntityAnnotation = cls.getAnnotation(CrowEntity.class);
        if(crowEntityAnnotation != null && !Strings.isNullOrEmpty(crowEntityAnnotation.apiPath())) {
            apiPath = crowEntityAnnotation.apiPath();
        } else {
            String[] raw = cls.getName().split("\\.");
            apiPath = String.join(".", Arrays.copyOfRange(raw, raw.length - 2, raw.length));
        }
        return apiPath;
    }

}
