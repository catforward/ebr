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
package pers.tsm.ebr.data;

import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppConsts;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskTypeEnum;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>flow's maker</pre>
 *
 * @author l.gong
 */
class FlowMaker {
    private final List<IValidator> taskValidators;
    private final List<IValidator> flowValidators;
    private final Map<String, Task> idTaskMapping;
    private final String flowUrl;
    private final JsonObject content;
    private Flow flow;

    FlowMaker(String flowUrl, JsonObject content) {
        requireNonNull(flowUrl);
        requireNonNull(content);
        this.flowUrl = flowUrl;
        this.content = content;
        this.idTaskMapping = new HashMap<>();
        this.taskValidators = new ArrayList<>();
        this.flowValidators = new ArrayList<>();
        this.taskValidators.add(new TaskMetaValidator());
        this.taskValidators.add(new ExternalScriptAttrValidator());
        this.flowValidators.add(new DAGFlowValidator());
    }

    Flow makeAndValidate() {
        makeBasicFlowInfo();
        updateTaskPropInfo();
        updateTaskUrlInfo(flow.root);
        validateAllTask();
        return flow;
    }

    private void makeBasicFlowInfo() {
        for (String taskId : content.getMap().keySet()) {
            JsonObject taskBody = content.getJsonObject(taskId);
            Task task = new Task(Task.Meta.buildFrom(taskId, taskBody));
            if (AppConsts.FLOW.equalsIgnoreCase(taskId)) {
                if (!isNull(flow)) {
                    throw new AppException(ResultEnum.ERR_10102);
                }
                task.url = this.flowUrl;
                task.type = TaskTypeEnum.FLOW;
                flow = new Flow(task);
            }
            idTaskMapping.put(taskId, task);
        }
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_10101);
        }
    }

    private void updateTaskPropInfo() {
        idTaskMapping.forEach((id, task) -> {
            if (TaskTypeEnum.FLOW != task.type) {
                Task groupTask = Optional.ofNullable(idTaskMapping.get(task.meta.group)).orElseThrow();
                if (TaskTypeEnum.TASK == groupTask.type) {
                    groupTask.type = TaskTypeEnum.GROUP;
                }
                groupTask.children.add(task);
                task.meta.depends.forEach(depId -> {
                    Task depTask = Optional.ofNullable(idTaskMapping.get(depId)).orElseThrow();
                    task.predecessor.add(depTask);
                    depTask.successor.add(task);
                });
                task.root = flow.root;
                if (!isNull(task.parent)) {
                    throw new AppException(ResultEnum.ERR_10107);
                }
                task.parent = groupTask;
            }
        });
    }

    private void updateTaskUrlInfo(Task task) {
        if (TaskTypeEnum.FLOW != task.type) {
            task.url = String.format("%s/%s", task.parent.url, task.meta.id);
            flow.urlTaskMapping.put(task.url, task);
        }
        for (var subTask : task.children) {
            if (TaskTypeEnum.GROUP == subTask.type) {
                updateTaskUrlInfo(subTask);
            } else {
                subTask.url = String.format("%s/%s", task.url, subTask.meta.id);
                flow.urlTaskMapping.put(subTask.url, subTask);
            }
        }
    }

    private void validateAllTask() {
        idTaskMapping.forEach((id, task) -> {
            taskValidators.forEach(validator -> validator.validate(task));
            if (TaskTypeEnum.FLOW == task.type) {
                flowValidators.forEach(validator -> validator.validate(task));
            }
        });
    }

}
