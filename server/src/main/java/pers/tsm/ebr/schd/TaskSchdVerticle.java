/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.tsm.ebr.schd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.tsm.ebr.base.ServiceResultMsg;
import pers.tsm.ebr.common.AppConsts;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.data.Flow;
import pers.tsm.ebr.data.Task;
import pers.tsm.ebr.data.TaskRepo;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskStateEnum;
import pers.tsm.ebr.types.TaskTypeEnum;

import java.util.List;

import static java.util.Objects.isNull;

/**
 *<pre>
 * task start request:
 * {
 *    "flow": string,
 *    "task"(optional): string
 * }
 * 
 * task running/complete msg:
 * {
 *    "flow": string,
 *    "task": string
 * }
 * 
 * task failed msg:
 * {
 *    "flow": string,
 *    "task": string,
 *    "cause": string
 * }
 * 
 * </pre>
 *
 * @author l.gong
 */
public class TaskSchdVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(TaskSchdVerticle.class);
    
    @Override
    public void start() throws Exception {
        super.start();
        // request
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_TASK_START, this::handleTaskStartAction);
        // message
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_RUNNING, this::handleTaskRunningMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_COMPLETE, this::handleTaskCompleteMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_FAILED, this::handleTaskFailedMsg);
        String deploymentId = deploymentID();
        logger.info("TaskSchdVerticle started. [{}]", deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        String deploymentId = deploymentID();
        logger.info("TaskSchdVerticle stopped. [{}]", deploymentId);
    }

    private void notice(String msg, Task task) {
        String flowUrl = isNull(task.getRoot()) ? task.getUrl() : task.getRoot().getUrl();
        JsonObject param = new JsonObject();
        param.put(AppConsts.FLOW, flowUrl);
        param.put(AppConsts.TASK, task.getUrl());
        vertx.eventBus().publish(msg, param);
    }

    private void handleTaskStartAction(Message<JsonObject> msg) {
        JsonObject target = msg.body();
        String flowUrl = target.getString(AppConsts.FLOW);
        String taskUrl = target.getString(AppConsts.TASK);
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }
        if (isNull(taskUrl) || taskUrl.isBlank()) {
            TaskStateEnum state = flow.getState();
            if (TaskStateEnum.RUNNING == state || TaskStateEnum.SKIPPED == state) {
                throw new AppException(ResultEnum.ERR_11005);
            }
            // finished, failed, paused, unknown, standby
            flow.standby();
            TaskRepo.pushRunnableFlow(flow);
            TaskRepo.pushRunnableTask(flow.getRootTask());
        } else {
            Task task = flow.getTask(taskUrl);
            if (isNull(task)) {
                throw new AppException(ResultEnum.ERR_11004);
            }
            TaskStateEnum state = task.getState();
            if (TaskStateEnum.RUNNING == state || TaskStateEnum.SKIPPED == state) {
                throw new AppException(ResultEnum.ERR_11006);
            }
            TaskRepo.pushRunnableTask(task);
        }
        msg.reply(new ServiceResultMsg(ResultEnum.SUCCESS).rawData());
    }

    private void handleTaskRunningMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.RUNNING);
        if (TaskTypeEnum.TASK != task.getType()) {
            findRunnableTask(task);
        }
    }

    private void handleTaskCompleteMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.FINISHED);
        checkParentState(task);
        if (TaskTypeEnum.FLOW != task.getType()) {
            findRunnableTask(task);
        } else {
            Flow flow = TaskRepo.getFlow(task.getUrl());
            flow.reset();
            TaskRepo.removeRunnableFlow(flow);
        }
    }

    private void handleTaskFailedMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.ERROR);
        checkParentState(task);
    }

    private Task getSpecifiedTask(JsonObject target) {
        Flow flow = getSpecifiedFlow(target);
        String taskUrl = target.getString(AppConsts.TASK);
        if (isNull(taskUrl) || taskUrl.isBlank()) {
            logger.debug("specified task: {}", taskUrl);
            throw new AppException(ResultEnum.ERR_11004);
        }
        Task task = flow.getTask(taskUrl);
        if (isNull(task)) {
            logger.debug("specified task: {}", taskUrl);
            throw new AppException(ResultEnum.ERR_11004);
        }
        return task;
    }

    private Flow getSpecifiedFlow(JsonObject target) {
        String flowUrl = target.getString(AppConsts.FLOW);
        if (isNull(flowUrl) || flowUrl.isBlank()) {
            logger.debug("specified flow: {}", flowUrl);
            throw new AppException(ResultEnum.ERR_11003);
        }
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            logger.debug("specified flow: {}", flowUrl);
            throw new AppException(ResultEnum.ERR_11003);
        }
        return flow;
    }

    private void findRunnableTask(Task src) {
        List<Task> targets = getRunnableCheckTargets(src);
        for (Task target : targets) {
            if (TaskTypeEnum.FLOW != target.getType()
                    && TaskStateEnum.RUNNING != target.getParent().getState()) {
                continue;
            }
            List<Task> predecessors = target.getPredecessor();
            boolean allDone = true;
            for (Task predecessor : predecessors) {
                if (TaskStateEnum.FINISHED != predecessor.getState()) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                TaskRepo.pushRunnableTask(target);
            }
        }
    }

    private List<Task> getRunnableCheckTargets(Task src) {
        if (TaskTypeEnum.FLOW == src.getType()) {
            return src.getChildren();
        } else if (TaskTypeEnum.GROUP == src.getType() && TaskStateEnum.RUNNING == src.getState()) {
            return src.getChildren();
        } else if (TaskTypeEnum.GROUP == src.getType() && TaskStateEnum.FINISHED == src.getState()) {
            return src.getSuccessor();
        } else if (TaskTypeEnum.TASK == src.getType() && TaskStateEnum.FINISHED == src.getState()) {
            return src.getSuccessor();
        } else {
            return List.of();
        }
    }

    private void checkParentState(Task src) {
        if (isNull(src) || TaskTypeEnum.FLOW == src.getType()) {
            return;
        }
        Task parent = src.getParent();
        int doneCount = 0;
        for (Task child : parent.getChildren()) {
            if (TaskStateEnum.ERROR == child.getState()) {
                notice(ServiceSymbols.MSG_STATE_TASK_FAILED, parent);
                break;
            }
            if (TaskStateEnum.FINISHED == child.getState()) {
                doneCount++;
            }
        }
        if (doneCount == parent.getChildren().size()) {
            notice(ServiceSymbols.MSG_STATE_TASK_COMPLETE, parent);
        }
    }

}
