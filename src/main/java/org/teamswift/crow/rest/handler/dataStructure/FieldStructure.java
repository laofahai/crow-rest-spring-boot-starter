package org.teamswift.crow.rest.handler.dataStructure;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FieldStructure implements Serializable {

    private String label;

    private String key;

    private DataType type = DataType.TEXT;

    private String foreignDisplayField;

    private String foreignApi;

    private List<?> choices;

    private int maxLength;

    private boolean required;

    private boolean updatable = true;

    private Object value;

    private boolean virtual;

    private boolean systemGenerated;

    private boolean preload;

}
