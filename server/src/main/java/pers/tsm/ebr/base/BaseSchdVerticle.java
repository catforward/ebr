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
package pers.tsm.ebr.base;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <pre>Base Functions</pre>
 *
 * @author l.gong
 */
public class BaseSchdVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BaseSchdVerticle.class);

    protected void notice(String msg, Task task) {
        String flowUrl = isNull(task.getRoot()) ? task.getUrl() : task.getRoot().getUrl();
        JsonObject param = new JsonObject();
        param.put(AppConsts.FLOW, flowUrl);
        param.put(AppConsts.TASK, task.getUrl());
        vertx.eventBus().publish(msg, param);
    }

    protected void notice(String msg, Flow flow) {
        JsonObject param = new JsonObject();
        param.put(AppConsts.FLOW, flow.getUrl());
        vertx.eventBus().publish(msg, param);
    }

    protected Task getSpecifiedTask(JsonObject target) {
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

    protected Flow getSpecifiedFlow(JsonObject target) {
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
