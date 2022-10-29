package org.teamswift.crow.rest.handler.dataStructure;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FieldStructure implements Serializable {

    // 字段显示名称
    private String label;

    // 字段名称
    private String key;

    // 字段类型
    private DataType type = DataType.TEXT;

    // 外键显示字段
    private String foreignDisplayField;

    // 外键API
    private String foreignApi;

    // 外键可选值列表
    private List<?> choices;

    private int maxLength;

    private boolean required;

    private boolean updatable = true;

    private Object value;

    // 虚拟字段
    private boolean virtual;

    private boolean systemGenerated;

    private boolean preload;

    private boolean jsonIgnore = false;

    // 数据库中表字段名称
    private String physicalFieldName;

    private boolean allowFuzzySearch;

}
