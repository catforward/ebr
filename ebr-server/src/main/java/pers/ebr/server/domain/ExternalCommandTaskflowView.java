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
package pers.ebr.server.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.ebr.server.common.IObjectConverter;

import java.util.ArrayList;
import java.util.List;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.domain.IExternalCommandTask.*;

/**
 * <p>
 * 工作流(Workflow)的视图数据，与客户端交互时使用
 * </p>
 *
 * @author l.gong
 */
public final class ExternalCommandTaskflowView implements IObjectConverter {
    private String instanceId;
    private final ExternalCommandTaskView rootDetail;
    private final ArrayList<ExternalCommandTaskView> views = new ArrayList<>();

    ExternalCommandTaskflowView(ExternalCommandTaskView root) {
        rootDetail = root;
    }

    /**
     * 获取根任务视图
     * @return ExternalCommandTaskView
     */
    public ExternalCommandTaskView getRootView() {
        return rootDetail;
    }

    /**
     * 获取任务的实例ID
     * @return String
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 获取所有任务视图数据
     * @return List
     */
    public List<ExternalCommandTaskView> getTaskViews() {
        return views;
    }

    /**
     * 设置任务的实例ID
     * @param instanceId [in] 待设置任务的实例ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * 添加任务视图数据
     * @param taskView [in] 待添加的任务
     */
    public void addTaskView(ExternalCommandTaskView taskView) {
        if (!rootDetail.getId().equals(taskView.getId())) {
            views.add(taskView);
        }
    }

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(MSG_PARAM_TASKFLOW_ID, rootDetail.getId());
        jsonObject.put(MSG_PARAM_INSTANCE_ID, instanceId);
        jsonObject.put(TASK_DESC, rootDetail.getDesc());
        JsonArray taskArr = new JsonArray();
        views.forEach(taskView -> {taskArr.add(taskView.toJsonObject());});
        jsonObject.put(MSG_PARAM_TASKS, taskArr);
        return jsonObject;
    }
}
