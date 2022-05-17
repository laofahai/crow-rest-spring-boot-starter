package org.teamswift.crow.rest.configure;

import org.teamswift.crow.rest.result.ICrowListResult;
import org.teamswift.crow.rest.result.ICrowResult;
import org.teamswift.crow.rest.result.impl.CrowDefaultResult;
import org.teamswift.crow.rest.result.impl.CrowListResult;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("crow.starter")
@Data
public class CrowServiceProperties {

    private boolean enabled;

    private Class<? extends ICrowListResult> defaultListResultClass = CrowListResult.class;

    private Class<? extends ICrowResult> defaultResultClass = CrowDefaultResult.class;

    private boolean dataStructureController = false;

}
