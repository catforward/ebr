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
package pers.ebr.server.facade;

import io.vertx.core.AsyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import pers.ebr.server.common.verticle.FacadeResponse;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.application.AppTopic.MSG_EXEC_STATISTICS;
import static pers.ebr.server.common.verticle.VerticleConst.FACADE_DATA;
import static pers.ebr.server.common.verticle.VerticleConst.FACADE_MSG;
import static pers.ebr.server.facade.FacadeTopic.API_EXEC_STATISTICS;
import static pers.ebr.server.facade.FacadeTopic.API_GET_SERVER_INFO;

/**
 * <p>App信息请求处理类</p>
 * <ul>
 *      <li>环境变量</li>
 *      <li>执行器统计</li>
 *  </ul>
 * @author l.gong
 */
public class ServerInfoCollectorVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ServerInfoCollectorVerticle.class);

    private final static String RESPONSE_RESULT_INFO_ENV = "env";
    private final static String RESPONSE_RESULT_INFO_CONFIG = "config";

    /**
     * Verticle初始化
     * @throws Exception 任意异常发生时
     */
    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(API_GET_SERVER_INFO, this::handleGetServerInfo);
        bus.consumer(API_EXEC_STATISTICS,  this::handleGetExecStatistics);
    }

    /**
     * Verticle结束
     * @throws Exception 任意异常发生时
     */
    @Override
    public void stop() throws Exception {
        super.stop();
    }

    /**
     * 获取环境统计
     * @param msg 请求体
     */
    private void handleGetServerInfo(Message<JsonObject> msg) {
        JsonObject resultBody = new JsonObject();

        // get all environment variables
        JsonObject envVars = new JsonObject();
        System.getenv().forEach(envVars::put);
        resultBody.put(RESPONSE_RESULT_INFO_ENV, envVars);

        // get server config
        JsonObject cfgVars = new JsonObject();
        config().getMap().forEach(cfgVars::put);
        resultBody.put(RESPONSE_RESULT_INFO_CONFIG, cfgVars);

        FacadeResponse response = FacadeResponse.ok(msg.body().getString(FACADE_MSG));
        response.setData(resultBody);

        msg.reply(response.toJsonObject());
    }

    /**
     * 获取执行器状态
     * @param msg 请求体
     */
    private void handleGetExecStatistics(Message<JsonObject> msg) {
        vertx.eventBus().request(MSG_EXEC_STATISTICS, new JsonObject(), (AsyncResult<Message<JsonObject>> res) -> {
            if (res.failed()) {
                FacadeResponse response = FacadeResponse.ng(msg.body().getString(FACADE_MSG));
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, res.cause());
                response.setData(errInfo);
                msg.reply(response.toJsonObject());
            } else {
                FacadeResponse response = FacadeResponse.ok(msg.body().getString(FACADE_MSG));
                response.setData(res.result().body());
                msg.reply(response.toJsonObject());
            }
        });
    }

}
