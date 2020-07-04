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
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.DAGFlow;
import pers.ebr.server.common.model.ItemBuilder;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.common.pool.IPool;
import pers.ebr.server.common.pool.Pool;

import java.util.Optional;
import java.util.UUID;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.GROUP;
import static pers.ebr.server.common.model.TaskType.UNIT;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class DAGScheduler extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(DAGScheduler.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(MSG_RUN_FLOW, this::handleRunFlow);
        bus.consumer(MSG_TASK_STATE_CHANGED, this::handleTaskStateChanged);
        bus.consumer(MSG_FLOW_FINISHED, this::handleFlowFinished);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleRunFlow(Message<JsonObject> msg) {
        String flowId = msg.body().getString(MSG_PARAM_FLOW_ID, "");
        String flowDefine= msg.body().getString(MSG_PARAM_FLOW_DEF, "");
        DAGFlow flow = new ItemBuilder().buildDagTaskFlow(new JsonObject(flowDefine));
        if (flow.isEmpty()) {
            logger.error(String.format("incorrect define of [%s])", flowId));
            return;
        }

        IPool pool = Pool.get();
        Optional<DAGFlow> oldOne = Optional.ofNullable(pool.getFlowByUrl(flow.getRootTask().getUrl()));
        if (oldOne.isPresent() && TaskState.ACTIVE == oldOne.get().getStatus()) {
            logger.error(String.format("task flow is already running. (id: [%s])", flowId));
            return;
        }
        flow.setInstanceId(UUID.randomUUID().toString());
        pool.setFlow(flow);

        //updateTaskState(flow.getInstanceId(), flow.getRootTask(), ACTIVE);
        collectExecutableTasks(flow, flow.getRootTask(), flow.getRootTask());
    }

    private void handleTaskStateChanged(Message<JsonObject> msg) {
        String taskUrl = msg.body().getString(MSG_PARAM_TASK_URL, "UNKNOWN");
        String taskInstanceId = msg.body().getString(MSG_PARAM_TASK_INSTANCE_ID, "UNKNOWN");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "UNKNOWN"));

        DAGFlow flow = Optional.ofNullable(Pool.get().getFlowByInstanceId(taskInstanceId)).orElseThrow();
        ITask target = Optional.ofNullable(flow.getTaskByUrl(taskUrl)).orElseThrow();

        updateTaskState(taskInstanceId, target, newState);
        collectExecutableTasks(flow, target.getGroup(), target);
    }

    private void handleFlowFinished(Message<JsonObject> msg) {
        String taskInstanceId = msg.body().getString(MSG_PARAM_TASK_INSTANCE_ID, "");
        DAGFlow flow = Optional.ofNullable(Pool.get().removeFlowByInstanceId(taskInstanceId)).orElseThrow();
        flow.release();
    }

    private void updateTaskState(String instanceId, ITask target, TaskState newState) {
        target.setStatus(newState);

        if (UNKNOWN == newState) {
            logger.warn("unknown state ({}::{})", instanceId, target.getUrl());
            return;
        }

        if (isRootTask(target)) {
            if (COMPLETE == newState || FAILED == newState) {
                JsonObject param = new JsonObject();
                param.put(MSG_PARAM_TASK_INSTANCE_ID, instanceId);
                param.put(MSG_PARAM_TASK_URL, target.getUrl());
                vertx.eventBus().publish(MSG_FLOW_FINISHED, param);
            }
            return;
        } else if (FAILED == newState) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_INSTANCE_ID, instanceId);
            param.put(MSG_PARAM_TASK_URL, target.getGroup().getUrl());
            param.put(MSG_PARAM_TASK_STATE, FAILED);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
            return;
        }

        long cnt = target.getGroup().getSubTaskList().stream().filter(t -> COMPLETE != t.getStatus()).count();
        if (cnt == 0) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_INSTANCE_ID, instanceId);
            param.put(MSG_PARAM_TASK_URL, target.getGroup().getUrl());
            param.put(MSG_PARAM_TASK_STATE, COMPLETE);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
        }
    }

    private void collectExecutableTasks(DAGFlow flow, ITask group, ITask task) {
        if (FAILED == task.getStatus()) {
            return;
        }

        if (GROUP == task.getType() && INACTIVE == task.getStatus()) {
            for (ITask sub : task.getSubTaskList()) {
                if (GROUP == sub.getType()) {
                    collectExecutableTasks(flow, sub.getGroup(), sub);
                } else {
                    long unfinishedDependTaskCnt = sub.getDependTaskSet().stream()
                            .filter(t -> COMPLETE != t.getStatus()).count();
                    if (unfinishedDependTaskCnt == 0 && INACTIVE == sub.getStatus()) {
                        Pool.get().addRunnableTaskQueue(sub);
                    }
                }
            }
        } else if (UNIT == task.getType() && COMPLETE == task.getStatus()) {
            for (ITask postTask : flow.getSuccessors(group, task)) {
                if (GROUP == postTask.getType()) {
                    collectExecutableTasks(flow, postTask.getGroup(), postTask);
                } else {
                    if (flow.getPredecessors(group, postTask).stream()
                            .anyMatch(t -> INACTIVE == t.getStatus())) {
                        Pool.get().addRunnableTaskQueue(postTask);
                    }
                }
            }
        }
    }

    private boolean isRootTask(ITask task) {
        return task.equals(task.getGroup());
    }

}
