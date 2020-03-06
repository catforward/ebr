package pers.ebr.server.http;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class HttpProcHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("req path:"+ routingContext.normalisedPath());
        System.out.println("req data:"+ routingContext.getBodyAsJson());
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        JsonObject resData = new JsonObject();
        resData.put("aa", "aa-1").put("bb", "bb1");
        response.end(resData.encode());
    }
}
