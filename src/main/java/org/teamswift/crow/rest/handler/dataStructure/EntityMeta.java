package org.teamswift.crow.rest.handler.dataStructure;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.Map;

@Data
public class EntityMeta {

    private String apiPath;

    private String name;

    private String label;

    private Map<String, FieldStructure> fieldsMap;

    private Map<String, FieldStructure> voFieldsMap;

}
