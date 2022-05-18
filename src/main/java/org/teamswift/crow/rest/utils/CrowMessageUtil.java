package org.teamswift.crow.rest.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.result.CrowResultCode;

@Component
public class CrowMessageUtil {

    private static MessageSource messageSource;

    public CrowMessageUtil(MessageSource messageSource) {
        CrowMessageUtil.messageSource = messageSource;
    }

    public static String get(String key, Object ...args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    public static String entityLabel(String apiPathDotField, Object ...args) {
        try {
            return messageSource.getMessage(apiPathDotField, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return apiPathDotField;
        }
    }

    public static String error(CrowErrorMessage crowErrorMessage, Object ...args) {
        String key = String.format("crow.error.%s", crowErrorMessage.name());
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    public static String resultCode(CrowResultCode crowResultCode, Object ...args) {
        String key = String.format("crow.result.%s", crowResultCode.name());
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

}
