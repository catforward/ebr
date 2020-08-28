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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.domain.ITaskflow;
import pers.ebr.server.domain.ModelItemMaker;
import pers.ebr.server.common.repository.Repository;

import java.util.Optional;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;

/**
 * The EditorVerticle
 *
 * @author l.gong
 */
public class TaskflowEditorVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskflowEditorVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(REQ_VALIDATE_FLOW, this::handleValidateTaskflow);
        bus.consumer(REQ_SAVE_FLOW, this::handleSaveTaskflow);
        bus.consumer(REQ_DEL_FLOW, this::handleDeleteTaskflow);
        bus.consumer(REQ_DUMP_FLOW_DEF, this::handleDumpTaskflowDefine);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleValidateTaskflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        boolean ret = false;
        try {
            Optional<JsonObject> flowBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM));
            ITaskflow workflow = ModelItemMaker.makeExternalTaskflow(flowBody.orElseThrow());
            logger.info("create a task flow -> {}", workflow.toString());
            ret = true;
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleSaveTaskflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        boolean ret = true;
        try {
            Optional<JsonObject> flowBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM));
            ITaskflow workflow = ModelItemMaker.makeExternalTaskflow(flowBody.orElseThrow());
            Repository.getDb().saveTaskflow(workflow);
        } catch (Exception ex) {
            logger.error("procedure [saveTaskFlow] error:", ex);
            ret = false;
        } finally {
            result.put(ret ? RESPONSE_RESULT : RESPONSE_ERROR, new JsonObject());
            msg.reply(result);
        }

    }

    private void handleDeleteTaskflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String flowId = reqBody.getString(MSG_PARAM_TASKFLOW_ID, "");
            // check
            if (flowId.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid taskflow id: [%s]", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            String flowUrl = String.format("/%s", flowId);
            if (Repository.getPool().getRunningTaskflowByPath(flowUrl) != null) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("taskflow id: [%s] is already running", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            // delete it
            int cnt = Repository.getDb().removeTaskflow(flowId);
            if (cnt == 0) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("taskflow id: [%s] is already running", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            } else {
                JsonObject retInfo = new JsonObject();
                retInfo.put(RESPONSE_INFO, String.format("delete records of taskflow : [%s]", cnt));
                result.put(RESPONSE_RESULT, retInfo);
            }
        } catch (Exception ex) {
            logger.error("procedure [handleStartTask] error:", ex);
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, ex.getMessage());
            result.put(RESPONSE_ERROR, errInfo);
        } finally {
            msg.reply(result);
        }
    }

    private void handleDumpTaskflowDefine(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        // TODO
    }

}
