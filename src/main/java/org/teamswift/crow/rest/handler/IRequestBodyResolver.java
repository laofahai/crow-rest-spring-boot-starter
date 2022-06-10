package org.teamswift.crow.rest.handler;

import org.teamswift.crow.rest.handler.requestParams.RequestBodyResolved;

public interface IRequestBodyResolver {

    void handle(RequestBodyResolved body);

}
