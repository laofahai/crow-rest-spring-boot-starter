package org.teamswift.crow.rest.configure;

import org.teamswift.crow.rest.exception.CrowErrorMessage;
import org.teamswift.crow.rest.exception.impl.InternalServerException;
import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.result.impl.CrowDefaultResult;
import org.teamswift.crow.rest.result.impl.CrowListResult;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.teamswift.crow.rest.utils.CrowMessageUtil;

import java.lang.reflect.InvocationTargetException;

@ConfigurationProperties("crow.starter")
@Data
public class CrowServiceProperties {

    private boolean enabled;

    private Class<? extends ICrowListResult> defaultListResultClass = CrowListResult.class;

    private Class<? extends ICrowResult> defaultResultClass = CrowDefaultResult.class;

    private boolean dataStructureController = false;

    public <T> ICrowListResult<T> getListResultInstance() {
        try {
            return defaultListResultClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.EntityMustManagedByCrow)
            );
        }
    }

    public <T> ICrowResult<T> getResultInstance() {
        try {
            return defaultResultClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalServerException(
                    CrowMessageUtil.error(CrowErrorMessage.ErrorWhenInstance)
            );
        }
    }

}
