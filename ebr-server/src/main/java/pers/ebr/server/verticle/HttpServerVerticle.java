/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.server.verticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FaviconHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

import static pers.ebr.server.constant.Global.REQUEST_PARAM_PATH;
import static pers.ebr.server.constant.Topic.*;
import static pers.ebr.server.base.Configs.KEY_HTTP_PORT;

/**
 * The HttpServerVerticle
 *
 * @author l.gong
 */
public class HttpServerVerticle extends AbstractVerticle {

    final static String WEB_ROOT = "static";
    final static String INDEX_HTML = "e-panel.html";
    final static String PROC_URL = "/proc";
    final static String FAVICON = "/favicon.ico";
    final static String REQ_ALL = "/*";

    private final static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
    private HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route(FAVICON).handler(FaviconHandler.create());
        StaticHandler staticHandler = StaticHandler.create().setWebRoot(WEB_ROOT).setIndexPage(INDEX_HTML)
                .setAlwaysAsyncFS(true).setFilesReadOnly(true).setCachingEnabled(true)
                .setDirectoryListing(false).setIncludeHidden(false).setEnableFSTuning(true);
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create());
        router.get(REQ_ALL).handler(staticHandler);
        router.post(PROC_URL).handler(new HttpProcHandler());

        server.requestHandler(router::handle).listen(config.getInteger(KEY_HTTP_PORT), res -> {
            if (res.succeeded()) {
                logger.info("HttpServer Start Success...");
            } else {
                logger.info("HttpServer Start Failure...");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}

class HttpProcHandler implements Handler<RoutingContext> {

    private final static Logger logger = LoggerFactory.getLogger(HttpProcHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject reqBody = routingContext.getBodyAsJson();
        logger.info("HTTP REQ PATH: {} BODY: {}", routingContext.normalisedPath(), reqBody);
        String address = reqBody.getString(REQUEST_PARAM_PATH, "");
        switch (address) {
            case REQ_INFO_GET_SERVER_INFO:
            case REQ_TASK_VALIDATE_TASK_FLOW:
            case REQ_TASK_SAVE_TASK_FLOW:
            case REQ_TASK_GET_ALL_TASK_FLOW: {
                handleReqRequest(address, reqBody, routingContext);
                break;
            }
            default: {
                logger.error("UNKNOWN REQUEST");
                // 403 - Forbidden
                routingContext.fail(403);
                break;
            }
        }
    }

    private void handleReqRequest(String address, JsonObject reqBody, RoutingContext routingContext) {
        routingContext.vertx().eventBus().request(address, reqBody, (AsyncResult<Message<JsonObject>> res) -> {
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