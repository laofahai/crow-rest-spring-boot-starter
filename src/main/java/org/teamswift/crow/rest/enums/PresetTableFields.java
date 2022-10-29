package org.teamswift.crow.rest.enums;

import lombok.Getter;

@Getter
public enum PresetTableFields {

    DeletedTime("deleted_date"),
    Deleted("deleted"),
    ID("id"),
    ;

    private final String name;

    PresetTableFields(String name) {
        this.name = name;
    }
}
