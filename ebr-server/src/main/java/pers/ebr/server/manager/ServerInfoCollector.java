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
package pers.ebr.server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.REQ_GET_SERVER_INFO;

/**
 * The ServerInfoVerticle
 *
 * @author l.gong
 */
public class ServerInfoCollector extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ServerInfoCollector.class);

    private final static String RESPONSE_RESULT_INFO_ENV = "env";
    private final static String RESPONSE_RESULT_INFO_CONFIG = "config";

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(REQ_GET_SERVER_INFO, this::handleGetServerInfo);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleGetServerInfo(Message<JsonObject> msg) {
        logger.info("recv data->{}", msg.body().toString());
        JsonObject result = new JsonObject();
        JsonObject resultBody = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        result.put(RESPONSE_RESULT, resultBody);

        // get all environment variables
        JsonObject envVars = new JsonObject();
        System.getenv().forEach(envVars::put);
        resultBody.put(RESPONSE_RESULT_INFO_ENV, envVars);

        // get server config
        JsonObject cfgVars = new JsonObject();
        config().getMap().forEach(cfgVars::put);
        resultBody.put(RESPONSE_RESULT_INFO_CONFIG, cfgVars);

        msg.reply(result);
    }

}
