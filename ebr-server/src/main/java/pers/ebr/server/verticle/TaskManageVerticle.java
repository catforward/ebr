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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.model.TaskFlow;
import pers.ebr.server.service.TaskItemCreateService;
import pers.ebr.server.service.TaskItemPersistService;

import java.util.List;
import java.util.Optional;

import static pers.ebr.server.constant.Global.REQUEST_PARAM_PATH;
import static pers.ebr.server.constant.Global.REQUEST_PARAM_PARAM;
import static pers.ebr.server.constant.Global.RESPONSE_RESULT;
import static pers.ebr.server.constant.Global.RESPONSE_ERROR;
import static pers.ebr.server.constant.Topic.*;

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
        bus.consumer(REQ_TASK_SAVE_TASK_FLOW, this::handleSaveTaskFlow);
        bus.consumer(REQ_TASK_GET_ALL_TASK_FLOW, this::handleGetAllTaskFlow);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleValidateTaskFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        boolean ret = false;
        try {
            Optional<JsonObject> flowBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM));
            TaskItemCreateService creator = new TaskItemCreateService();
            Optional<TaskFlow> flow = creator.createTaskFlowStruct(flowBody.orElseThrow());
            creator.buildTaskFlow(flow.orElseThrow());
            logger.info("create a task flow -> {}", flow.orElseThrow().toString());
            ret = true;
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleSaveTaskFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        boolean ret = false;
        try {
            Optional<JsonObject> flowBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM));
            TaskItemCreateService creator = new TaskItemCreateService();
            Optional<TaskFlow> flow = creator.createTaskFlowStruct(flowBody.orElseThrow());
            creator.buildTaskFlow(flow.orElseThrow());
            TaskItemPersistService saver = new TaskItemPersistService();
            ret = saver.saveTaskFlow(flow.orElseThrow());
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleGetAllTaskFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        try {
            TaskItemPersistService persist = new TaskItemPersistService();
            List<String> flows = persist.getAllTaskFlow();
            if (!flows.isEmpty()) {
                JsonArray array = new JsonArray();
                flows.forEach(array::add);
                result.put(RESPONSE_RESULT, array);
            } else {
                result.put(RESPONSE_RESULT, new JsonObject());
            }
        } catch (Exception ex) {
            logger.error("error at [handleGetAllTaskFlow]");
            result.put(RESPONSE_ERROR, new JsonObject());
        } finally {
            msg.reply(result);
        }
    }

}
