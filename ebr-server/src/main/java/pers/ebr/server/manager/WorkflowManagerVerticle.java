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
import pers.ebr.server.common.model.IDetail;
import pers.ebr.server.common.model.WorkflowDetail;
import pers.ebr.server.repository.Repository;

import java.util.Collection;
import java.util.Optional;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.Topic.REQ_SHOW_FLOW_LOG;
import static pers.ebr.server.common.model.ITask.TASK_ID;

/**
 * The ManageVerticle
 *
 * @author l.gong
 */
public class WorkflowManagerVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowManagerVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(REQ_ALL_WORKFLOW, this::handleGetAllWorkflow);
        bus.consumer(REQ_GET_FLOW_STATUS,  this::handleGetFlowStatus);
        bus.consumer(REQ_RUN_FLOW, this::handleRunFlow);
        bus.consumer(REQ_SHOW_FLOW_LOG, this::handleShowFlowLog);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleGetAllWorkflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            Collection<IDetail> flows = Repository.get().getAllWorkflowDetail();
            if (!flows.isEmpty()) {
                // TODO
                JsonArray array = new JsonArray();
                flows.forEach(workflowDetail -> {array.add(workflowDetail.toJsonObject());});
                result.put(RESPONSE_RESULT, array);
            } else {
                result.put(RESPONSE_RESULT, new JsonObject());
            }
        } catch (Exception ex) {
            logger.error("procedure [handleGetAllTaskFlow] error:", ex);
            result.put(RESPONSE_ERROR, new JsonObject());
        } finally {
            msg.reply(result);
        }
    }

    private void handleGetFlowStatus(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String flowId = reqBody.getString(TASK_ID, "");
            if (!flowId.isBlank()) {
                String flowDefine = Repository.get().getWorkflow(flowId);
                JsonObject retInfo = new JsonObject();
                retInfo.put(flowId, new JsonObject(flowDefine));
                result.put(RESPONSE_RESULT, retInfo);
            } else {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid task flow id: [%s]", flowId));
                result.put(RESPONSE_ERROR, errInfo);
            }
        } catch (Exception ex) {
            logger.error("procedure [handleGetTaskFlowStatus] error:", ex);
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, ex.getMessage());
            result.put(RESPONSE_ERROR, errInfo);
        } finally {
            msg.reply(result);
        }
    }

    private void handleRunFlow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String flowId = reqBody.getString(TASK_ID, "");
            // check
            if (flowId.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid task flow id: [%s]", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            // 暂时只有启动flow的http请求
            String flowDefine = Repository.get().getWorkflow(flowId);
            if (flowDefine == null || flowDefine.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("define is not exists (id: [%s])", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }

            EventBus bus = vertx.eventBus();
            JsonObject noticeParam = new JsonObject();
            noticeParam.put(MSG_PARAM_WORKFLOW_ID, flowId);
            noticeParam.put(MSG_PARAM_WORKFLOW_DEF, flowDefine);
            bus.publish(MSG_RUN_FLOW, noticeParam);

            // response
            JsonObject retInfo = new JsonObject();
            retInfo.put(RESPONSE_INFO, String.format("task flow start. (id: [%s])", flowId));
            result.put(RESPONSE_RESULT, retInfo);
        } catch (Exception ex) {
            logger.error("procedure [handleStartTask] error:", ex);
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, ex.getMessage());
            result.put(RESPONSE_ERROR, errInfo);
        } finally {
            msg.reply(result);
        }
    }

    private void handleShowFlowLog(Message<JsonObject> msg) {
        // TODO
    }

}
