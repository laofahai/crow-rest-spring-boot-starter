package org.teamswift.crow.rest.exception;

import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.teamswift.crow.rest.result.CrowResult;
import org.teamswift.crow.rest.result.CrowResultCode;
import org.teamswift.crow.rest.result.impl.CrowErrorResult;
import org.teamswift.crow.rest.utils.CrowMessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
public class CrowExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ResponseEntity<CrowErrorResult> handleBusinessException(BusinessException exception) {
        return new ResponseEntity<>(CrowResult.ofError(exception), exception.getHttpStatus());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, 0);
        }

        CrowErrorResult result;

        if(ex instanceof MethodArgumentNotValidException) {
            result = new CrowErrorResult();
            result.setHttpStatusCode(status.value());
            List<ObjectError> allErrors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
            List<String> errorsMsg = new ArrayList<>();
            allErrors.forEach(e -> {
                if(Strings.isNullOrEmpty(e.getDefaultMessage())) {
                    return;
                }
                errorsMsg.add(
                        CrowMessageUtil.get(
                                "error." +
                                e.getDefaultMessage().replaceAll(" ", "-")
                                        .replaceAll("\\.", "-")
                                        .replaceAll("=", "-")
                                        .toLowerCase(Locale.ROOT)
                        )
                );
            });
            result.setData(String.join(", ", errorsMsg));
            result.setResultCode(CrowResultCode.PARAM_IS_INVALID);
        } else {
            result = CrowResult.ofError(ex);
        }

        return new ResponseEntity(result, headers, status);
    }

}
