package org.teamswift.crow.rest.utils;

import com.google.common.base.CaseFormat;

public class NamingUtils {

    public static String camelToLine(String raw) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, raw);
    }

    public static String underlineToCamel(String raw) {
        if(!raw.contains("_")) {
            return raw;
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, raw);
    }

}
