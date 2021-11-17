/*
  Copyright 2021 liang gong

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.ebr.base;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.types.ResultEnum;

import java.util.Map;

import static java.util.Objects.isNull;
import static pers.ebr.base.AppSymbols.BLANK_STR;
import static pers.ebr.base.AppSymbols.EMPTY_JSON_OBJ;

/**
 * <pre>
 *     Http's request handler
 *     Post Body:
 *     {
 *         "api": "xxx",
 *         "param": {
 *             "xxx":"xxx",
 *             ...
 *         }
 *     }
 * </pre>
 *
 * @author l.gong
 */
public class BaseHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private final Map<String, String> apiServiceMap;

    public BaseHandler(Map<String, String> apiServiceMap) {
        this.apiServiceMap = apiServiceMap;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            HttpServerRequest request = routingContext.request();
            JsonObject inData = new JsonObject()
                    .put(AppSymbols.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT))
                    .put(AppSymbols.BODY, routingContext.getBody().length() == 0 ? EMPTY_JSON_OBJ
                            : routingContext.getBody().toJsonObject());
            logger.trace("request info: {}", inData);

            doPrepare(inData)
            .compose(v -> doHandle(routingContext, inData))
            .onSuccess(ar -> {
                logger.trace("response info: {}", ar);
                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "application/json");
                response.end(ar.toString());
            }).onFailure(ex -> {
                logger.debug("service failed...", ex);
                if (ex instanceof AppException se) {
                    String respStr = new ServiceResultMsg(se.getReason()).toJsonObject().toString();
                    logger.trace("response info: {}", respStr);
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json");
                    response.end(respStr);
                } else {
                    routingContext.fail(Integer.parseInt(ResultEnum.ERR_400.getCode()));
                }
            });

        } catch (Exception ex) {
            logger.error("Exception: ", ex);
            routingContext.fail(Integer.parseInt(ResultEnum.ERR_500.getCode()));
        }
    }

    private Future<Void> doPrepare(JsonObject inData) {
        return Future.future(promise -> {
            String api = getHttpRequestBody(inData).getString(AppSymbols.API, BLANK_STR);
            if (isNull(apiServiceMap.get(api))) {
                promise.fail(new AppException(ResultEnum.ERR_404));
            } else {
                promise.complete();
            }
        });
    }

    private Future<JsonObject> doHandle(RoutingContext routingContext, JsonObject inData) {
        return Future.future(promise -> {
            String serviceName = apiServiceMap.get(getHttpRequestBody(inData).getString(AppSymbols.API));
            if (isNull(serviceName) || serviceName.isBlank()) {
                promise.fail(new AppException(ResultEnum.ERR_500));
                return;
            }
            EventBus bus = routingContext.vertx().eventBus();
            bus.request(serviceName, getRequestParams(inData), (AsyncResult<Message<JsonObject>> res) -> {
                if (res.failed()) {
                    logger.error("calling service failed... ", res.cause());
                    promise.fail(res.cause());
                } else {
                    promise.complete(res.result().body());
                }
            });
        });
    }

    private JsonObject getHttpRequestBody(JsonObject inData) {
        return inData.getJsonObject(AppSymbols.BODY, EMPTY_JSON_OBJ);
    }

    private JsonObject getRequestParams(JsonObject inData) {
        JsonObject body = getHttpRequestBody(inData);
        return body.getJsonObject(AppSymbols.PARAM, EMPTY_JSON_OBJ);
    }

}
