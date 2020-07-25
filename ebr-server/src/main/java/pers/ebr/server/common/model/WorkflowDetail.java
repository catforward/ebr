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
package pers.ebr.server.common.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.model.ITask.*;

/**
 * <pre>
 * WorkflowDetail Object
 * </pre>
 *
 * @author l.gong
 */
public final class WorkflowDetail implements IDetail {
    private String instanceId;
    private final TaskDetail rootDetail;
    private final ArrayList<TaskDetail> tasks = new ArrayList<>();

    WorkflowDetail(TaskDetail root) {
        rootDetail = root;
    }

    @Override
    public int type() {
        return IDetail.WORKFLOW;
    }

    public TaskDetail getRootDetail() {
        return rootDetail;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void addTaskDetail(TaskDetail task) {
        if (!rootDetail.getId().equals(task.getId())) {
            tasks.add(task);
        }
    }

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(MSG_PARAM_WORKFLOW_ID, rootDetail.getId());
        jsonObject.put(MSG_PARAM_INSTANCE_ID, instanceId);
        jsonObject.put(TASK_DESC, rootDetail.getDesc());
        JsonArray taskArr = new JsonArray();
        tasks.forEach(taskDetail -> {taskArr.add(taskDetail.toJsonObject());});
        jsonObject.put(MSG_PARAM_TASKS, taskArr);
        return jsonObject;
    }
}
