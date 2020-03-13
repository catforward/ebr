package pers.ebr.server.web;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.com.GlobalConstants.*;

public class HttpProcHandler implements Handler<RoutingContext> {

    private final static Logger logger = LoggerFactory.getLogger(HttpProcHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject param = routingContext.getBodyAsJson();
        logger.info("HTTP REQ PATH: {} BODY: {}", routingContext.normalisedPath(), param);

        switch (param.getString(HTTP_PARAM_PATH, "")) {
            case TOPIC_GET_SERVER_INFO: {
                handleGetServerInfo(TOPIC_GET_SERVER_INFO, param, routingContext);
                break;
            }
            default: {
                logger.error("UNKNOWN REQUEST");
                break;
            }
        }
    }

    private void handleGetServerInfo(String address, JsonObject param, RoutingContext routingContext) {
        routingContext.vertx().eventBus().request(address, param, (AsyncResult<Message<JsonObject>> res) -> {
            if (res.failed()) {
                routingContext.fail(res.cause());
            } else {
                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "application/json");
                response.end(res.result().body().encode());
            }
        });
    }
}
