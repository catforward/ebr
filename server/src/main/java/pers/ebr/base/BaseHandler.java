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
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * <pre>Http's request handler</pre>
 *
 * @author l.gong
 */
public class BaseHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private final Map<String, String> apiServiceMap;
    protected final JsonObject emptyBody = new JsonObject();

    public BaseHandler(Map<String, String> apiServiceMap) {
        this.apiServiceMap = apiServiceMap;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            HttpServerRequest request = routingContext.request();
            JsonObject inData = new JsonObject()
                    .put(AppConsts.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT))
                    .put(AppConsts.METHOD, request.method().toString())
                    .put(AppConsts.PATH, request.path())
                    .put(AppConsts.BODY, Optional.ofNullable(routingContext.getBodyAsJson()).orElse(emptyBody));
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
                if (ex instanceof AppException) {
                    AppException se = (AppException) ex;
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
            String path = inData.getString(AppConsts.PATH);
            if (isNull(apiServiceMap.get(path))) {
                promise.fail(new AppException(ResultEnum.ERR_404));
            } else {
                promise.complete();
            }
        });
    }

    private Future<JsonObject> doHandle(RoutingContext routingContext, JsonObject inData) {
        return Future.future(promise -> {
            String serviceName = apiServiceMap.get(inData.getString(AppConsts.PATH));
            if (isNull(serviceName) || serviceName.isBlank()) {
                promise.fail(new AppException(ResultEnum.ERR_500));
                return;
            }
            EventBus bus = routingContext.vertx().eventBus();
            bus.request(serviceName, inData, (AsyncResult<Message<JsonObject>> res) -> {
                if (res.failed()) {
                    logger.error("calling service failed... ", res.cause());
                    promise.fail(res.cause());
                } else {
                    promise.complete(res.result().body());
                }
            });
        });
    }

}
