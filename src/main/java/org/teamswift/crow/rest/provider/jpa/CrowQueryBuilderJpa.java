package org.teamswift.crow.rest.provider.jpa;

import com.google.common.base.Strings;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.ParameterInvalidException;
import org.teamswift.crow.rest.utils.CrowMessageUtil;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CrowQueryBuilderJpa {

    static public Predicate equals(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(value == null || value == "" || value == "null") {
            return cb.and(result, cb.isNull(path));
        }
        return cb.and(
                result,
                cb.equal(path, value)
        );
    }

    static public Predicate equals(List<Expression> paths, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        List<Predicate> or = new ArrayList<>();
        for(Expression<?> e: paths) {
            or.add(cb.equal(e, value));
        }

        Predicate[] p = new Predicate[or.size()];
        return cb.or(or.toArray(p));
    }

    static public Predicate notEquals(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(Strings.isNullOrEmpty(String.valueOf(value)) || "null".equals(String.valueOf(value))) {
            return cb.and(result, cb.isNotNull(path));
        }
        return cb.and(
                result,
                cb.notEqual(path, value)
        );
    }

    static public Predicate notEquals(List<Expression> paths, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        List<Predicate> or = new ArrayList<>();
        for(Expression<?> e: paths) {
            or.add(cb.notEqual(e, value));
        }

        Predicate[] p = new Predicate[or.size()];
        return cb.or(or.toArray(p));
    }

    static public Predicate like(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        String stringValue = String.valueOf(value);
        if(!stringValue.contains("%")) {
            stringValue = "%" + stringValue + "%";
        }

        return cb.and(
                result,
                cb.like(
                        path,
                        stringValue
                )
        );
    }

    static public Predicate like(List<Expression> paths, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        String stringValue = String.valueOf(value);
        if(!stringValue.contains("%")) {
            stringValue = "%" + stringValue + "%";
        }

        List<Predicate> or = new ArrayList<>();
        for(Expression<String> e: paths) {
            or.add(cb.like(e, stringValue));
        }

        Predicate[] p = new Predicate[or.size()];
        return cb.or(or.toArray(p));
    }

    static public Predicate in(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {

        List<?> valueList;

        if(!(value instanceof List)) {
            valueList = Arrays.asList(value.toString().replaceAll(",", "\\|").split("\\|"));
        } else {
            valueList = (List<?>) value;
        }

        CriteriaBuilder.In<Object> in = cb.in(path);
        for (Object v: valueList) {
            in.value(v);
        }
        return cb.and(result, in);
    }

    static public Predicate notIn(Expression<?> path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {

        List<?> valueList;

        if(!(value instanceof List)) {
            valueList = Arrays.asList(value.toString().replaceAll(",", "\\|").split("\\|"));
        } else {
            valueList = (List<?>) value;
        }

        CriteriaBuilder.In<Object> notIn = cb.in(path);
        for (Object v: valueList) {
            notIn.value(v);
        }
        return cb.and(result, cb.not(notIn));
    }

    static public Predicate between(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {

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
            return cb.and(
                    cb.between(path, since, until)
            );
        } else {
            return cb.and(
                    cb.between(path, Scaffolds.inputValueToDecimal(valueList.get(0)), Scaffolds.inputValueToDecimal(valueList.get(1)))
            );
        }
    }

    static public Predicate lessThan(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 23:59:59";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            result = cb.and(
                    result,
                    cb.lessThan(path, (Date) value)
            );
        } else {
            result = cb.and(
                    result,
                    cb.lessThan(path, Scaffolds.inputValueToDecimal(value))
            );
        }

        return result;
    }

    static public Predicate greaterThan(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 00:00:00";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            result = cb.and(
                    result,
                    cb.greaterThan(path, (Date) value)
            );
        } else {
            result = cb.and(
                    result,
                    cb.greaterThan(path, Scaffolds.inputValueToDecimal(value))
            );
        }

        return result;
    }

    static public Predicate lessThanOrEqualTo(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 23:59:59";
            }
            value = Scaffolds.tryTransDateString(rawValue);

            result = cb.and(
                    result,
                    cb.lessThanOrEqualTo(path, (Date) value)
            );
        } else {
            result = cb.and(
                    result,
                    cb.lessThanOrEqualTo(path, Scaffolds.inputValueToDecimal(value))
            );
        }

        return result;
    }

    static public Predicate greaterThanOrEqualTo(Expression path, Object value, Object raw, Predicate result, CriteriaBuilder cb) {
        if(value instanceof Date) {
            String rawValue = String.valueOf(raw);
            if(rawValue.length() == 10) {
                rawValue += " 00:00:00";
            }
            value = Scaffolds.tryTransDateString(rawValue);
            result = cb.and(
                    result,
                    cb.greaterThanOrEqualTo(path, (Date) value)
            );
        } else {
            result = cb.and(
                    result,
                    cb.greaterThanOrEqualTo(path, Scaffolds.inputValueToDecimal(value))
            );
        }

        return result;
    }

}
