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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.constant.Topic.REQ_TASK_SAVE_TASK_FLOW;
import static pers.ebr.server.constant.Topic.REQ_TASK_VALIDATE_TASK_FLOW;

/**
 * The ManageVerticle
 *
 * @author l.gong
 */
public class TaskManageVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskManageVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(REQ_TASK_VALIDATE_TASK_FLOW, this::handleValidateTaskFlow);
        bus.consumer(REQ_TASK_SAVE_TASK_FLOW, this::handleTrySaveTaskFlow);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleValidateTaskFlow(Message<JsonObject> msg) {
        logger.info("recv data->{}", msg.body().toString());

    }

    private void handleTrySaveTaskFlow(Message<JsonObject> msg) {
        logger.info("recv data->{}", msg.body().toString());

    }
}
