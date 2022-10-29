package org.teamswift.crow.rest.provider.mybatisPlus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.ParameterInvalidException;
import org.teamswift.crow.rest.handler.dataStructure.FieldStructure;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.criteria.Expression;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class CrowQueryBuilderMybatisPlus<ID extends Serializable, V extends ICrowIO, T extends ICrowEntity<ID, V>> extends QueryWrapper<T> {
    
    public QueryWrapper<T> equals(FieldStructure field, Object value, Object raw) {
        if(value == null || value == "" || value == "null") {
            return isNull(field.getPhysicalFieldName());
        }
        return eq(field.getPhysicalFieldName(), value);
    }

    public QueryWrapper<T> equals(List<FieldStructure> fields, Object value, Object raw) {
        return and(item -> {
            for(FieldStructure field: fields) {
                item.or(orItem -> {
                    orItem.eq(field.getPhysicalFieldName(), value);
                });
            }
        });
    }

    public QueryWrapper<T> notEquals(FieldStructure field, Object value, Object raw) {
        if(Strings.isNullOrEmpty(String.valueOf(value)) || "null".equals(String.valueOf(value))) {
            return isNotNull(field.getPhysicalFieldName());
        }
        return ne(field.getPhysicalFieldName(), value);
    }


    public QueryWrapper<T> like(FieldStructure field, Object value, Object raw) {
        String stringValue = String.valueOf(value);
        if(!stringValue.contains("%")) {
            stringValue = "%" + stringValue + "%";
        }

        return like(field.getPhysicalFieldName(), value);
    }

    public QueryWrapper<T> like(List<FieldStructure> fields, Object value, Object raw) {
        String stringValue = String.valueOf(value);
        if (!stringValue.contains("%")) {
            stringValue = "%" + stringValue + "%";
        }

        String finalStringValue = stringValue;
        return and(item -> {
            for (FieldStructure field : fields) {
                item.or(orItem -> {
                    orItem.like(field.getPhysicalFieldName(), finalStringValue);
                });
            }
        });
    }

    public QueryWrapper<T> in(FieldStructure field, Object value, Object raw) {

        List<?> valueList;

        if(!(value instanceof List)) {
            valueList = Arrays.asList(value.toString().replaceAll(",", "\\|").split("\\|"));
        } else {
            valueList = (List<?>) value;
        }

        return in(field.getPhysicalFieldName(), valueList);
    }

    public QueryWrapper<T> notIn(Expression<?> path, FieldStructure field, Object value, Object raw) {

        List<?> valueList;

        if(!(value instanceof List)) {
            valueList = Arrays.asList(value.toString().replaceAll(",", "\\|").split("\\|"));
        } else {
            valueList = (List<?>) value;
        }

        return notIn(field.getPhysicalFieldName(), valueList);
    }

    public QueryWrapper<T> between(FieldStructure field, Object value, Object raw) {

        if(!(value instanceof List)) {
            throw new ParameterInvalidException(
                    CrowMessageUtil.error(CrowErrorMessage.BetweenQueryNeedParamForArray)
            );
        }

        List<?> valueList = (List<?>) value;

        if(valueList.size() < 2) {
            throw new ParameterInvalidException(
                    CrowMessageUtil.error(CrowErrorMessage.BetweenQueryNeedTwoParams)
            );
        }
        Object value1 = valueList.get(0);
        if(value1 instanceof Date) {
            Date since;
            Date until;
            String value1Str = value1.toString();
            if(value1.toString().length() == 10 && value1.toString().contains("-")) {
                String sinceStr = valueList.get(0).toString() + " 00:00:00";
                String untilStr = valueList.get(1).toString() + " 23:59:59";
                since = Scaffolds.tryTransDateString(sinceStr);
                until = Scaffolds.tryTransDateString(untilStr);
            } else {

                since =  (Date) valueList.get(0);
                until =  (Date) valueList.get(1);
            }

            return between(field.getPhysicalFieldName(), since, until);
        } else {
            return between(
                    field.getPhysicalFieldName(),
                    Scaffolds.inputValueToDecimal(valueList.get(0)),
                    Scaffolds.inputValueToDecimal(valueList.get(1))
            );
        }
    }

    public QueryWrapper<T> lessThan(FieldStructure field, Object value, Object raw) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 23:59:59";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            return lt(field.getForeignDisplayField(), value);
        } else {
            return lt(field.getPhysicalFieldName(), Scaffolds.inputValueToDecimal(value));
        }
    }

    public QueryWrapper<T>greaterThan(FieldStructure field, Object value, Object raw) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 00:00:00";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            return gt(field.getForeignDisplayField(), value);
        } else {
            return gt(field.getPhysicalFieldName(), Scaffolds.inputValueToDecimal(value));
        }
    }

    public QueryWrapper<T>lessThanOrEqualTo(FieldStructure field, Object value, Object raw) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 23:59:59";
            }
            value = Scaffolds.tryTransDateString(rawValue);

            return le(field.getForeignDisplayField(), value);
        } else {
            return le(field.getPhysicalFieldName(), Scaffolds.inputValueToDecimal(value));
        }
    }

    public QueryWrapper<T>greaterThanOrEqualTo(FieldStructure field, Object value, Object raw) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 00:00:00";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            return ge(field.getForeignDisplayField(), value);
        } else {
            return ge(field.getPhysicalFieldName(), Scaffolds.inputValueToDecimal(value));
        }
    }

}
