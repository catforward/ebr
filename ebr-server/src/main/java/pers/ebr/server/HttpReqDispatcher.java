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
package pers.ebr.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.common.Const.REQUEST_PARAM_REQ;
import static pers.ebr.server.common.Topic.*;

/**
 * The HttpProcHandler
 *
 * @author l.gong
 */
public class HttpReqDispatcher implements Handler<RoutingContext> {
    private final static Logger logger = LoggerFactory.getLogger(HttpReqDispatcher.class);

    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject reqBody = routingContext.getBodyAsJson();
        logger.info("HTTP REQ PATH: {} BODY: {}", routingContext.normalisedPath(), reqBody);
        String address = reqBody.getString(REQUEST_PARAM_REQ, "");
        switch (address) {
            case REQ_GET_SERVER_INFO:
            case REQ_VALIDATE_FLOW:
            case REQ_SAVE_FLOW:
            case REQ_DEL_FLOW:
            case REQ_ALL_FLOW:
            case REQ_EXEC_STATISTICS:
            case REQ_LAUNCH_FLOW:
            case REQ_DUMP_FLOW_DEF: {
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
