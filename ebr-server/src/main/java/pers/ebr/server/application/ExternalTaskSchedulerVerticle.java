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
package pers.ebr.server.application;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.domain.*;
import pers.ebr.server.common.repository.Repository;
import pers.ebr.server.common.repository.RepositoryException;

import java.util.Optional;
import java.util.UUID;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.TaskState.*;
import static pers.ebr.server.common.TaskType.GROUP;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.Utils.checkNotNull;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class ExternalTaskSchedulerVerticle extends AbstractVerticle implements IRunnableTaskAppender, ITaskStateWatcher {
    private final static Logger logger = LoggerFactory.getLogger(ExternalTaskSchedulerVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(MSG_LAUNCH_FLOW, this::handleRunFlow);
        bus.consumer(MSG_EXEC_RESULT, this::handleExecuteResult);
        bus.consumer(MSG_FLOW_FINISHED, this::handleWorkflowFinished);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleRunFlow(Message<JsonObject> msg) {
        String flowId = msg.body().getString(MSG_PARAM_TASKFLOW_ID, "");
        ITaskflow workflow;
        try {
            workflow = Repository.getDb().loadTaskflow(flowId);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        if (workflow.isEmpty()) {
            logger.error(String.format("incorrect define of [%s])", flowId));
            return;
        }

        Optional<ITaskflow> oldOne = Optional.ofNullable(Repository.getPool().getRunningTaskflowByPath(workflow.getRootTask().getPath()));
        if (oldOne.isPresent() && TaskState.ACTIVE == oldOne.get().getState()) {
            logger.error(String.format("task flow is already running. (id: [%s])", flowId));
            return;
        }
        workflow.setInstanceId(UUID.randomUUID().toString());
        workflow.setRunnableTaskAppender(this);
        workflow.setTaskStateWatcher(this);
        Repository.getPool().addRunningTaskflow(workflow);
        workflow.standby();
    }

    private void handleExecuteResult(Message<JsonObject> msg) {
        String taskPath = msg.body().getString(MSG_PARAM_TASK_PATH, "");
        String taskInstanceId = msg.body().getString(MSG_PARAM_INSTANCE_ID, "");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "-1"));

        ITaskflow workflow = Optional.ofNullable(Repository.getPool().getRunningTaskflowByInstanceId(taskInstanceId)).orElseThrow();
        workflow.setTaskState(taskPath, newState);
    }

    private void handleWorkflowFinished(Message<JsonObject> msg) {
        String taskInstanceId = msg.body().getString(MSG_PARAM_INSTANCE_ID, "");
        Repository.getPool().removeRunningTaskflowByInstanceId(taskInstanceId);
    }

    /**
     * 追加一个新的任务对象引用
     *
     * @param taskflow 待追加的任务所在任务流
     * @param task 待追加的任务
     */
    @Override
    public void append(ITaskflow taskflow, IExternalCommandTask task) {
        checkNotNull(task);
        if (GROUP == task.getType()) {
            taskflow.setTaskState(task.getPath(), ACTIVE);
        } else {
            Repository.getPool().addRunnableTaskQueue(task);
        }
    }

    /**
     * 推送任务新状态信息
     *
     * @param instanceId 任务所在工作流实例ID
     * @param path       任务逻辑路径
     * @param isRootTask 是否是根任务
     * @param src        旧状态
     * @param dst        新状态
     */
    @Override
    public void onStateChanged(String instanceId, String path, boolean isRootTask, TaskState src, TaskState dst) {
        checkNotNull(instanceId);
        checkNotNull(path);
        checkNotNull(src);
        checkNotNull(dst);

        logger.info("state changed->task:[{}::{}] state:[{} -> {}]", instanceId, path, src, dst);
        if (UNKNOWN == dst) {
            logger.warn("unknown state ({}::{})", instanceId, path);
            return;
        }

        if (isRootTask) {
            if (COMPLETE == dst || FAILED == dst) {
                JsonObject param = new JsonObject();
                param.put(MSG_PARAM_INSTANCE_ID, instanceId);
                param.put(MSG_PARAM_TASK_PATH, path);
                param.put(MSG_PARAM_TASK_STATE, dst);
                vertx.eventBus().publish(MSG_FLOW_FINISHED, param);
            }
        }
        JsonObject param = new JsonObject();
        param.put(MSG_PARAM_INSTANCE_ID, instanceId);
        param.put(MSG_PARAM_TASK_PATH, path);
        param.put(MSG_PARAM_TASK_STATE, FAILED);
        vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);

    }

}
