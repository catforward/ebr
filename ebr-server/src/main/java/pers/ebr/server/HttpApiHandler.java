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

import static pers.ebr.server.common.verticle.VerticleConst.FACADE_MSG;
import static pers.ebr.server.facade.FacadeTopic.*;

/**
 * The HttpProcHandler
 *
 * @author l.gong
 */
public class HttpApiHandler implements Handler<RoutingContext> {
    private final static Logger logger = LoggerFactory.getLogger(HttpApiHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject reqBody = routingContext.getBodyAsJson();
        logger.debug("HTTP REQ PATH: {} BODY: {}", routingContext.normalisedPath(), reqBody);
        String address = reqBody.getString(FACADE_MSG, "");
        switch (address) {
            case API_GET_SERVER_INFO:
            case API_EXEC_STATISTICS:
            case API_VALIDATE_FLOW:
            case API_SAVE_FLOW:
            case API_DELETE_FLOW:
            case API_QUERY_ALL_FLOW:
            case API_LAUNCH_FLOW:
            case API_DUMP_FLOW_DEF: {
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
