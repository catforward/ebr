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
package pers.ebr.server.common.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * <p>
 *   基础Verticle
 * </p>
 *
 * @author l.gong
 */
public abstract class MulitFunctionalFacadeVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(MulitFunctionalFacadeVerticle.class);
    private final Map<String, IHandler<FacadeContext>> handlerMap = new HashMap<>();

    @Override
    public void start() throws Exception {
        super.start();
        onStart();
    }

    @Override
    public void stop() throws Exception {
        onStop();
        super.stop();
    }

    /**
     * Verticle初始化
     * @throws Exception 任意异常发生时
     */
    protected abstract void onStart() throws Exception;

    /**
     * Verticle结束
     * @throws Exception 任意异常发生时
     */
    protected abstract void onStop() throws Exception;

    /**
     * 绑定外部请求的处理函数
     * @param address 外部请求事件ID
     * @param handler 外部请求的处理函数
     */
    protected void subscribe(String address, IHandler<FacadeContext> handler) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(handler);
        handlerMap.put(address, handler);
        vertx.eventBus().consumer(address, this::onFacadeProc);
    }

    /**
     * 外部请求的处理函数
     * @param msg 外部请求数据
     */
    private void onFacadeProc(Message<JsonObject> msg) {
        IHandler<FacadeContext> handler = handlerMap.get(msg.address());
        if (handler == null) {
            String errMsg = String.format("no handler for facade msg: [%s]", msg.address());
            logger.warn(errMsg);
            msg.fail(500, errMsg);
            return;
        }
        FacadeContext context = new FacadeContext(msg);
        try {
            if(handler.handle(context)) {
                doFacadeSuccess(context);
            } else {
                doFacadeError(context);
            }
        } catch (Exception ex) {
            logger.error("", ex);
            doFacadeError(context);
        } finally {
            context.release();
        }
    }

    private void doFacadeError(FacadeContext ctx) {
        FacadeResponse response = FacadeResponse.ng(ctx.getRequest().getMsg());
        response.setData(ctx.getResponseData().getMap());
        ctx.getRawMessage().reply(response.toJsonObject());
    }

    private void doFacadeSuccess(FacadeContext ctx) {
        FacadeResponse response = FacadeResponse.ok(ctx.getRequest().getMsg());
        response.setData(ctx.getResponseData().getMap());
        ctx.getRawMessage().reply(response.toJsonObject());
    }

}
