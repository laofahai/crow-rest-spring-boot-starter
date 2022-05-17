package org.teamswift.crow.rest.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericUtils {

    static public Class<?> get(Class<?> cls, int index) {
        Type genType = cls.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return null;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (!(params[index] instanceof Class)) {
            return null;
        }
        return (Class<?>) params[index];
    }

}
