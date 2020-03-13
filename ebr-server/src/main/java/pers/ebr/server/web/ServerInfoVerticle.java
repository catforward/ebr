package pers.ebr.server.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.com.GlobalConstants.*;

public class ServerInfoVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ServerInfoVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(TOPIC_GET_SERVER_INFO, this::handleGetServerInfo);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleGetServerInfo(Message<JsonObject> msg) {
        System.out.println("recv data->"+msg.body().toString());
        JsonObject result = new JsonObject();
        JsonObject resultBody = new JsonObject();
        result.put(HTTP_PARAM_PATH, msg.body().getString(HTTP_PARAM_PATH));
        result.put(HTTP_PARAM_RESULT, resultBody);

        // get all environment variables
        JsonObject envVars = new JsonObject();
        System.getenv().forEach(envVars::put);
        resultBody.put(HTTP_PARAM_ENV, envVars);

        msg.reply(result);
    }

}
