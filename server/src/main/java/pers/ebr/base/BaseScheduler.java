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
package pers.ebr.base;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.data.Flow;
import pers.ebr.data.Task;
import pers.ebr.data.TaskRepo;
import pers.ebr.types.ResultEnum;
import pers.ebr.types.TaskStateEnum;
import pers.ebr.types.TaskTypeEnum;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>Base Functions</pre>
 *
 * @author l.gong
 */
public class BaseScheduler extends BaseVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BaseScheduler.class);

    protected void launchCronFlow(Flow flow) {
        flow.standby();
        TaskRepo.appendCronObject(flow);
    }

    protected void launchFlow(Flow flow) {
        TaskRepo.pushRunnableFlow(flow);
        TaskRepo.pushRunnableTask(flow.getRootTask());
        notice(ServiceSymbols.MSG_STATE_FLOW_LAUNCH, flow);
    }

    protected Task getSpecifiedTask(JsonObject target) {
        Flow flow = getSpecifiedFlow(target);
        String taskUrl = target.getString(AppSymbols.TASK);
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

    protected Flow getSpecifiedFlow(JsonObject target) {
        String flowUrl = target.getString(AppSymbols.FLOW);
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

    protected void findRunnableTask(Task src) {
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

    protected List<Task> getRunnableCheckTargets(Task src) {
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

    protected void checkParentState(Task src) {
        if (isNull(src) || TaskTypeEnum.FLOW == src.getType()) {
            return;
        }
        Task parent = src.getParent();
        int doneCount = 0;
        for (Task child : parent.getChildren()) {
            TaskStateEnum taskState = child.getState();
            if (TaskStateEnum.ERROR == taskState) {
                notice(ServiceSymbols.MSG_STATE_TASK_FAILED, parent);
                break;
            }
            if (TaskStateEnum.FINISHED == taskState
                || TaskStateEnum.SKIPPED == taskState
                || TaskStateEnum.ABORTED == taskState) {
                doneCount++;
            }
            // paused -> stay in running
        }
        if (doneCount == parent.getChildren().size()) {
            notice(ServiceSymbols.MSG_STATE_TASK_COMPLETE, parent);
        }
    }

}
