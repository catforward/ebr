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
package pers.ebr.server.web;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.com.Constants.*;
import static pers.ebr.server.com.Topic.INFO_GET_SERVER_INFO;

/**
 * The HttpServerVerticle
 *
 * @author l.gong
 */
public class HttpProcHandler implements Handler<RoutingContext> {

    private final static Logger logger = LoggerFactory.getLogger(HttpProcHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject param = routingContext.getBodyAsJson();
        logger.info("HTTP REQ PATH: {} BODY: {}", routingContext.normalisedPath(), param);

        switch (param.getString(REQUEST_PARAM_PATH, "")) {
            case INFO_GET_SERVER_INFO: {
                handleGetServerInfo(INFO_GET_SERVER_INFO, param, routingContext);
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
