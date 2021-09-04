/*
  Copyright 2021 liang gong

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
package pers.ebr.schd;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.*;
import pers.ebr.data.CronFlowRepo;
import pers.ebr.data.Flow;
import pers.ebr.data.Task;
import pers.ebr.data.TaskRepo;
import pers.ebr.types.ResultEnum;
import pers.ebr.types.TaskStateEnum;
import pers.ebr.types.TaskTypeEnum;

import static java.util.Objects.isNull;
import static pers.ebr.base.StringUtils.isNullOrBlank;

/**
 *<pre>
 * flow start/abort request:
 * {
 *    "flow": string,
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
public class ActionSchdVerticle extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ActionSchdVerticle.class);
    
    @Override
    public void start() throws Exception {
        super.start();
        // client request
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_FLOW_START, this::onStartFlowAction);
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_FLOW_ABORTED, this::onAbortFlowAction);
        // state changed message
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_RUNNING, this::onRunningMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_COMPLETE, this::onCompleteMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_FAILED, this::onFailedMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_SKIPPED, this::onSkippedMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_TASK_ABORTED, this::onAbortedMsg);
        String deploymentId = deploymentID();
        logger.info("TaskSchdVerticle started. [{}]", deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        String deploymentId = deploymentID();
        logger.info("TaskSchdVerticle stopped. [{}]", deploymentId);
    }

    private void onStartFlowAction(Message<JsonObject> msg) {
        JsonObject target = msg.body();
        String flowUrl = target.getString(AppConsts.FLOW);
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }
        TaskStateEnum state = flow.getState();
        if (TaskStateEnum.RUNNING == state || TaskStateEnum.SKIPPED == state) {
            throw new AppException(ResultEnum.ERR_11005);
        }
        if (!isNullOrBlank(flow.getRootTask().getCronStr())) {
            CronFlowRepo.addFlow(flow);
        } else {
            launchFlow(flow);
        }
        msg.reply(new ServiceResultMsg(ResultEnum.SUCCESS).rawData());
    }

    private void onAbortFlowAction(Message<JsonObject> msg) {
        JsonObject target = msg.body();
        String flowUrl = target.getString(AppConsts.FLOW);
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }

        CronFlowRepo.removeFlow(flow);

        TaskStateEnum flowState = flow.getState();
        if (TaskStateEnum.STORED == flowState
                || TaskStateEnum.FINISHED == flowState
                || TaskStateEnum.ABORTED == flowState) {
            logger.info("can not abort flow[{}]. state:[{}]  --> abort...", flowUrl, flowState.getName());
        } else {
            flow.abort();
        }

        msg.reply(new ServiceResultMsg(ResultEnum.SUCCESS).rawData());
    }

    private void onRunningMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.RUNNING);
        if (TaskTypeEnum.TASK != task.getType()) {
            findRunnableTask(task);
        }
    }

    private void onCompleteMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.FINISHED);
        checkParentState(task);
        if (TaskTypeEnum.FLOW != task.getType()) {
            findRunnableTask(task);
        } else {
            Flow flow = TaskRepo.getFlow(task.getUrl());
            flow.reset();
            TaskRepo.removeRunnableFlow(flow);
            notice(ServiceSymbols.MSG_STATE_FLOW_FINISH, flow);
        }
    }

    private void onFailedMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        task.updateState(TaskStateEnum.ERROR);
        checkParentState(task);
    }

    private void onSkippedMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        checkParentState(task);
        if (TaskTypeEnum.FLOW != task.getType()) {
            findRunnableTask(task);
        }
    }

    private void onAbortedMsg(Message<JsonObject> msg) {
        Task task = getSpecifiedTask(msg.body());
        checkParentState(task);
        if (TaskTypeEnum.FLOW != task.getType()) {
            findRunnableTask(task);
        }
    }

}
