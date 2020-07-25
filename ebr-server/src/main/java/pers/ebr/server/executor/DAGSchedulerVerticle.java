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
package pers.ebr.server.executor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.*;
import pers.ebr.server.pool.IPool;
import pers.ebr.server.pool.Pool;

import java.util.Optional;
import java.util.UUID;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class DAGSchedulerVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(DAGSchedulerVerticle.class);
    private final static String TYPE = "DAG";
    private final SchdSummary summary = ModelItemBuilder.buildSchdSummary(TYPE);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(MSG_RUN_FLOW, this::handleRunFlow);
        bus.consumer(MSG_TASK_STATE_CHANGED, this::handleTaskStateChanged);
        bus.consumer(MSG_WORKFLOW_FINISHED, this::handleWorkflowFinished);
        bus.consumer(MSG_SCHD_SUMMARY, this::handleGetSchdSummary);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        summary.reset();
    }

    private void handleRunFlow(Message<JsonObject> msg) {
        String flowId = msg.body().getString(MSG_PARAM_WORKFLOW_ID, "");
        String flowDefine= msg.body().getString(MSG_PARAM_WORKFLOW_DEF, "");
        DAGWorkflow flow = ModelItemBuilder.buildDagTaskFlow(new JsonObject(flowDefine));
        if (flow.isEmpty()) {
            logger.error(String.format("incorrect define of [%s])", flowId));
            return;
        }

        IPool pool = Pool.get();
        Optional<DAGWorkflow> oldOne = Optional.ofNullable(pool.getWorkflowByUrl(flow.getRootTask().getUrl()));
        if (oldOne.isPresent() && TaskState.ACTIVE == oldOne.get().getStatus()) {
            logger.error(String.format("task flow is already running. (id: [%s])", flowId));
            return;
        }
        flow.setInstanceId(UUID.randomUUID().toString());
        pool.setFlow(flow);
        pool.addRunnableTaskQueue(flow.getRootTask());
    }

    private void handleTaskStateChanged(Message<JsonObject> msg) {
        String taskUrl = msg.body().getString(MSG_PARAM_TASK_URL, "");
        String taskInstanceId = msg.body().getString(MSG_PARAM_INSTANCE_ID, "");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "-1"));

        DAGWorkflow flow = Optional.ofNullable(Pool.get().getWorkflowByInstanceId(taskInstanceId)).orElseThrow();
        ITask target = Optional.ofNullable(flow.getTaskByUrl(taskUrl)).orElseThrow();

        updateTaskState(taskInstanceId, target, newState);
        collectExecutableTasks(flow, target.getGroup(), target);

        updateSchdSummaryData(newState);
    }

    private void handleWorkflowFinished(Message<JsonObject> msg) {
        String taskInstanceId = msg.body().getString(MSG_PARAM_INSTANCE_ID, "");
        DAGWorkflow flow = Optional.ofNullable(Pool.get().removeWorkflowByInstanceId(taskInstanceId)).orElseThrow();
        flow.release();
    }

    private void handleGetSchdSummary(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(TYPE, summary.toJsonObject());
        msg.reply(result);
    }

    private void updateTaskState(String instanceId, ITask target, TaskState newState) {
        target.setStatus(newState);

        if (UNKNOWN == newState) {
            logger.warn("unknown state ({}::{})", instanceId, target.getUrl());
            return;
        }

        if (target.isRootTask()) {
            if (COMPLETE == newState || FAILED == newState) {
                JsonObject param = new JsonObject();
                param.put(MSG_PARAM_INSTANCE_ID, instanceId);
                param.put(MSG_PARAM_TASK_URL, target.getUrl());
                vertx.eventBus().publish(MSG_WORKFLOW_FINISHED, param);
            }
            return;
        } else if (FAILED == newState) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_INSTANCE_ID, instanceId);
            param.put(MSG_PARAM_TASK_URL, target.getGroup().getUrl());
            param.put(MSG_PARAM_TASK_STATE, FAILED);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
            return;
        }

        long cnt = target.getGroup().getSubTaskList().stream().filter(t -> COMPLETE != t.getStatus()).count();
        if (cnt == 0) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_INSTANCE_ID, instanceId);
            param.put(MSG_PARAM_TASK_URL, target.getGroup().getUrl());
            param.put(MSG_PARAM_TASK_STATE, COMPLETE);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
        }
    }

    private void collectExecutableTasks(DAGWorkflow flow, ITask group, ITask task) {
        if (FAILED == task.getStatus()) {
            return;
        }

        if (GROUP == task.getType() && ACTIVE == task.getStatus()) {
            task.getSubTaskList().forEach(sub -> {
                long unfinishedDependTaskCnt = sub.getDependTaskSet().stream()
                        .filter(t -> COMPLETE != t.getStatus()).count();
                if (unfinishedDependTaskCnt == 0 && INACTIVE == sub.getStatus()) {
                    Pool.get().addRunnableTaskQueue(sub);
                }
            });
        } else if (COMPLETE == task.getStatus()) {
            flow.getSuccessors(group, task).forEach(successor -> {
                long unfinishedDependTaskCnt = successor.getDependTaskSet().stream()
                        .filter(t -> COMPLETE != t.getStatus()).count();
                if (unfinishedDependTaskCnt == 0 && INACTIVE == successor.getStatus()) {
                    Pool.get().addRunnableTaskQueue(successor);
                }
            });
        }
    }

    private void updateSchdSummaryData(TaskState newState) {
        switch (newState) {
            case ACTIVE: {
                summary.setActiveCnt(Pool.get().getActiveTaskCount());
                break;
            }
            case COMPLETE: {
                summary.incCompleteSumCnt();
                break;
            }
            case FAILED: {
                summary.incFailedSumCnt();
                break;
            }
            default: { break; }
        }
    }
}
