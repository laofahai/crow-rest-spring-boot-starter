package org.teamswift.crow.rest.utils;

import com.google.common.base.Strings;
import org.springframework.util.Assert;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.exception.BusinessException;
import jdk.jfr.Label;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.InternalServerException;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Scaffolds {

    private static final String[] trueValue = new String[]{
            "是", "Yes", "YES", "yes", "1", "true"
    };

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static BigDecimal inputValueToDecimal(Object inputValue) {
        String v = String.valueOf(inputValue);
        if(v.isEmpty() || v.equals("null")) {
            v = "0";
        }
        return BigDecimal.valueOf(
                Double.parseDouble(v)
        );
    }

    public static boolean inputValueToBoolean(Object inputValue) {
        String raw = String.valueOf(inputValue);

        return Arrays.asList(trueValue).contains(raw);
    }

    public static String inputValueToString(Object inputValue) {
        if(inputValue == null) {
            return "";
        }
        if(inputValue instanceof ICrowEntity) {
            return inputValue.toString();
        }
        String raw = String.valueOf(inputValue);

        return raw.equals("null") ? "" : raw;
    }

    public static Integer inputValueToInteger(Object inputValue) {
        if(inputValue == null || inputValue == "null" || inputValue == "") {
            return null;
        }
        return Integer.valueOf(String.valueOf(inputValue));
    }

    static public Date tryTransDateString(Object value) {
        return tryTransDateString(value, null);
    }

    static public Date tryTransDateString(Object value, String[] formats) {

        if(value instanceof Date) {
            return (Date) value;
        }

        String[] defaultFormats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        };

        List<String> formatsList = new ArrayList<>(Arrays.asList(defaultFormats));
        if(formats != null) {
            formatsList.addAll(Arrays.asList(formats));
        }

        SimpleDateFormat sdf;

        for(String format: formatsList) {
            sdf = new SimpleDateFormat(format);

            Date result;
            try {
                result = sdf.parse(value.toString());
                return result;
            } catch (ParseException ignore) {
            }
        }

        return null;
    }

    public static String dateFormat(Date date) {
        return sdf.format(date);
    }

    static public long compareBetweenDays(Date from, Date to) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

        if(from == null) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        long ssd = calendar.getTimeInMillis();
        calendar.setTime(to == null ? new Date() : to);
        long now = calendar.getTimeInMillis();

        return (now - ssd) / (1000L*3600L*24L);
    }

    static public String dateFormat(Date date, String format) {
        format = format == null ? "yyyy-MM-dd" : format;

        if(date == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 获取所有已声明的属性，包括其父类
     * @param cls
     * @return
     */
    static public Map<String, Field> getAllDeclareFieldsMap(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        while (cls!=null){
            fields.addAll(new ArrayList<>(Arrays.asList(cls.getDeclaredFields())));
            cls = cls.getSuperclass();
        }

        Map<String, Field> result = new HashMap<>();
        for(Field field: fields) {
            if(!result.containsKey(field.getName())) {
                result.put(field.getName(), field);
            }
        }

        return result;
    }

    /**
     * 获取已声明的属性，包括父类继承
     * @param cls
     * @param field
     * @return
     * @throws NoSuchFieldException
     */
    static public Field getDeclareFieldAll(Class<?> cls, String field) {
        Map<String, Field> fields = getAllDeclareFieldsMap(cls);

        if(fields.containsKey(field)) {
            return fields.get(field);
        }
        try {
            throw new NoSuchFieldException(cls.getName() + "." +field);
        } catch (NoSuchFieldException ignored) {}

        return null;
    }

    public static List<Field> getDeclareFieldsAll(Class<?> cls) {
        Class<?> currentCls = cls;
        List<Field> fieldList = new ArrayList<>();
        while (currentCls != null) {
            fieldList.addAll(0, Arrays.asList(currentCls.getDeclaredFields()));
            currentCls = currentCls.getSuperclass();
        }

        return fieldList;
    }


    /**
     * handle the fieldName style like foo.bar.hello
     * @param fieldName
     * @param root
     * @return
     */
    public static Path<?> getExpressionPath(String fieldName, Root<?> root) {
        Path<?> path = root;
        for(String f: fieldName.split("\\.")) {
            path = path.get(f);
        }

        return path;
    }

    public static <ID> List<ID> parseIdStringToGeneric(String ids, Class<ID> genericType) {
        return Arrays.stream(ids.split(",")).map(id -> {
            try {
                Assert.notNull(genericType, "ID generic must provided.");
                Method method = genericType.getMethod("valueOf", String.class);
                return (ID) method.invoke(null, id);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new InternalServerException(
                        CrowMessageUtil.error(CrowErrorMessage.ConvertIDStringToGeneric, e.getMessage())
                );
            }
        }).collect(Collectors.toList());
    }
}
