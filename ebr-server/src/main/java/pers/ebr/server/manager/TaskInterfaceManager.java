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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.ItemBuilder;
import pers.ebr.server.common.model.TaskFlow;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.common.pool.ITaskPool;
import pers.ebr.server.common.pool.TaskPool;
import pers.ebr.server.common.repo.Repository;
import pers.ebr.server.common.repo.RepositoryException;

import java.util.List;
import java.util.Optional;

import static pers.ebr.server.common.model.Task.TASK_ID;
import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskState.ACTIVE;

/**
 * The ManageVerticle
 *
 * @author l.gong
 */
public class TaskInterfaceManager extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskInterfaceManager.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(REQ_TASK_VALIDATE_TASK_FLOW, this::handleValidateTaskFlow);
        bus.consumer(REQ_TASK_SAVE_TASK_FLOW, this::handleSaveTaskFlow);
        bus.consumer(REQ_TASK_GET_ALL_TASK_FLOW, this::handleGetAllTaskFlow);
        bus.consumer(REQ_TASK_GET_TASK_FLOW_STATUS,  this:: handleGetTaskFlowStatus);
        bus.consumer(REQ_START_TASK, this::handleStartTask);
        bus.consumer(REQ_SHOW_LOG, this::handleShowLogOf);
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
           TaskFlow flow = new ItemBuilder().buildTaskFlow(flowBody.orElseThrow());
            logger.info("create a task flow -> {}", flow.toString());
            ret = true;
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleSaveTaskFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        boolean ret = true;
        try {
            Optional<JsonObject> flowBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM));
            TaskFlow flow = new ItemBuilder().buildTaskFlow(flowBody.orElseThrow());
            Repository.get().setFlowItem(flow.flowId().orElseThrow(), flow.toJsonString());
        } catch (RepositoryException ex) {
            logger.error("procedure [saveTaskFlow] error...");
            ret = false;
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleGetAllTaskFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        try {
            List<String> flows = Repository.get().loadAllFlowId();
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

    private void handleGetTaskFlowStatus(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String flowId = reqBody.getString(TASK_ID, "");
            if (!flowId.isBlank()) {
                String flowDefine = Repository.get().getFlowItem(flowId);
                JsonObject retInfo = new JsonObject();
                retInfo.put(flowId, new JsonObject(flowDefine));
                result.put(RESPONSE_RESULT, retInfo);
            } else {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid task flow id: [%s]", flowId));
                result.put(RESPONSE_ERROR, errInfo);
            }
        } catch (Exception ex) {
            logger.error("error at [handleGetTaskFlowStatus]");
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, ex.getMessage());
            result.put(RESPONSE_ERROR, errInfo);
        } finally {
            msg.reply(result);
        }
    }

    private void handleStartTask(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_PATH, msg.body().getString(REQUEST_PARAM_PATH));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String taskId = reqBody.getString(TASK_ID, "");
            // check
            if (taskId.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid task flow id: [%s]", taskId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            // 暂时只有启动flow的http请求
            String flowDefine = Repository.get().getFlowItem(taskId);
            if (flowDefine == null || flowDefine.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("define is not exists (id: [%s])", taskId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            TaskFlow flow = new ItemBuilder().buildTaskFlow(new JsonObject(flowDefine));
            if (flow.isEmpty()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("incorrect define of [%s])", taskId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            ITaskPool pool = TaskPool.get();
            Optional<TaskFlow> oldOne = Optional.ofNullable(pool.getFlowItem(flow.flowId().orElseThrow()));
            if (oldOne.isPresent() && TaskState.ACTIVE == oldOne.get().status()) {
                JsonObject retInfo = new JsonObject();
                retInfo.put(RESPONSE_INFO, String.format("task flow is already running. (id: [%s])", taskId));
                result.put(RESPONSE_RESULT, retInfo);
                return;
            }

            // run it
            pool.setFlowItem(flow);
            EventBus bus = vertx.eventBus();
            JsonObject noticeParam = new JsonObject();
            noticeParam.put(MSG_PARAM_TASK_ID, taskId);
            noticeParam.put(MSG_PARAM_TASK_STATE, ACTIVE);
            bus.publish(MSG_TASK_STATE_CHANGED, noticeParam);

            // response
            JsonObject retInfo = new JsonObject();
            retInfo.put(RESPONSE_INFO, String.format("task flow start. (id: [%s])", taskId));
            result.put(RESPONSE_RESULT, retInfo);
        } catch (Exception ex) {
            logger.error("error at [handleStartTask]");
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, ex.getMessage());
            result.put(RESPONSE_ERROR, errInfo);
        } finally {
            msg.reply(result);
        }
    }

    private void handleShowLogOf(Message<JsonObject> msg) {
        // TODO
    }

}
