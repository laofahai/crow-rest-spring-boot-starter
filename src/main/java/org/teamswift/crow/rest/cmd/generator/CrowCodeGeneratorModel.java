package org.teamswift.crow.rest.cmd.generator;

import lombok.Data;

@Data
public class CrowCodeGeneratorModel {

    private String app;

    private String appLC;

    private String module;

    private String moduleLC;

    private String packageName;

    private String superEntity;

    private String superEntityClass;

    private String primaryKeyType;

}
