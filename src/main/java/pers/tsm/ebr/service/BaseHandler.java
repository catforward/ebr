/**
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
package pers.tsm.ebr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import pers.tsm.ebr.types.ServiceResultEnum;

import static java.util.Objects.isNull;
import static pers.tsm.ebr.common.Symbols.*;

import java.util.Map;
import java.util.Optional;

/**
 *
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

    /**
     * <p>处理REST请求</p>
     *
     * @param routingContext 路由上下文
     */
    @Override
    public void handle(RoutingContext routingContext) {
        try {
            HttpServerRequest request = routingContext.request();
            JsonObject inData = new JsonObject()
                    .put(USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT))
                    .put(METHOD, request.method().toString())
                    .put(PATH, request.path())
                    .put(BODY, Optional.ofNullable(routingContext.getBodyAsJson()).orElse(emptyBody));
            logger.trace("request info: {}", inData);

            // 准备工作（验证，获取数据等）-> 服务处理 的处理顺序
            doPrepare(inData)
            .compose(v -> doHandle(routingContext, inData))
            .onSuccess(ar -> {
                logger.trace("response info: {}", ar);
                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "application/json");
                response.end(ar.toString());
            }).onFailure(ex -> {
                logger.debug("处理结果：失败...", ex);
                if (ex instanceof ServiceException) {
                    ServiceException se = (ServiceException) ex;
                    String respStr = new ServiceResultMsg(se.getReason()).toJsonObject().toString();
                    logger.trace("response info: {}", respStr);
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json");
                    response.end(respStr);
                } else {
                    // 其他未知异常
                    routingContext.fail(Integer.parseInt(ServiceResultEnum.HTTP_400.getCode()));
                }
            });

        } catch (Exception ex) {
            // 出现异常时，vertx会向客户端返回HTTP 500错误，但是服务器端没有任何日志
            logger.error("Exception: ", ex);
            routingContext.fail(Integer.parseInt(ServiceResultEnum.HTTP_500.getCode()));
        }
    }

    /**
     * <p>前置准备处理</p>
     * <p>使用场景：特殊的参数检查，或特别的参数取得</p>
     * @param inData 请求数据
     * @return 处理结果
     */
    private Future<Void> doPrepare(JsonObject inData) {
        return Future.future(promise -> {
        	String path = inData.getString(PATH);
        	if (isNull(apiServiceMap.get(path))) {
        		promise.fail(new ServiceException(ServiceResultEnum.HTTP_404));
        	} else {
        		promise.complete();
        	}
        });
    }

    /**
     * <p>服务处理</p>
     * @param routingContext 路由上下文
     * @param inData 请求数据
     * @return 处理结果
     */
    private Future<JsonObject> doHandle(RoutingContext routingContext, JsonObject inData) {
        return Future.future(promise -> {
        	String serviceName = apiServiceMap.get(inData.getString(PATH));
            if (isNull(serviceName) || serviceName.isBlank()) {
                promise.fail(new ServiceException(ServiceResultEnum.HTTP_500));
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
