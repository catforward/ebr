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
import pers.ebr.server.model.ExternalCommandWorkflowView;
import pers.ebr.server.repository.Repository;

import java.util.Collection;
import java.util.Optional;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;

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
        bus.consumer(REQ_RUN_WORKFLOW, this::handleRunWorkflow);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleGetAllWorkflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            Collection<ExternalCommandWorkflowView> flows = Repository.get().getAllWorkflowDetail();
            if (!flows.isEmpty()) {
                //updateWorkflowStatus(flows);
                JsonArray array = new JsonArray();
                flows.forEach(workflowView -> {array.add(workflowView.toJsonObject());});
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

    private void handleRunWorkflow(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(REQUEST_PARAM_REQ, msg.body().getString(REQUEST_PARAM_REQ));
        try {
            JsonObject reqBody = Optional.ofNullable(msg.body().getJsonObject(REQUEST_PARAM_PARAM)).orElse(new JsonObject());
            String flowId = reqBody.getString(MSG_PARAM_WORKFLOW_ID, "");
            // check
            if (flowId.isBlank()) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("invalid workflow id: [%s]", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            String flowPath = String.format("/%s", flowId);
            if (Repository.getPool().getRunningWorkflowByPath(flowPath) != null) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("workflow id: [%s] is already running", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }
            if (!Repository.get().isWorkflowExists(flowId)) {
                JsonObject errInfo = new JsonObject();
                errInfo.put(RESPONSE_INFO, String.format("define is not exists (id: [%s])", flowId));
                result.put(RESPONSE_ERROR, errInfo);
                return;
            }

            EventBus bus = vertx.eventBus();
            JsonObject noticeParam = new JsonObject();
            noticeParam.put(MSG_PARAM_WORKFLOW_ID, flowId);
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

//    private void updateWorkflowStatus(Collection<WorkflowView> flows) {
//        for (var detail : flows) {
//            IWorkflow runningFlow = Pool.get().getWorkflowByPath(detail.getRootView().getPath());
//            if (runningFlow == null) {
//                continue;
//            }
//            detail.getTasks().forEach(taskDetail -> {
//                IExternalTask task = runningFlow.getTaskById(taskDetail.getId());
//                if (task != null) {
//                    taskDetail.setState(task.getState());
//                }
//            });
//        }
//    }

}
