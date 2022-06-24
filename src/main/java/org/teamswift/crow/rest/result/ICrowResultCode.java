package org.teamswift.crow.rest.result;

import org.teamswift.crow.rest.utils.CrowMessageUtil;

public interface ICrowResultCode {

    String name();

    int getCode();

    default String getMessage() {
        return CrowMessageUtil.resultCode(this);
    };

}
