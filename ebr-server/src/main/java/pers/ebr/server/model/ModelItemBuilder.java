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
package pers.ebr.server.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static pers.ebr.server.model.IExternalCommandTask.*;

/**
 * <p>
 *  model包下对象创建器
 * </p>
 *
 * @author l.gong
 */
public final class ModelItemBuilder {

    private ModelItemBuilder() {}

    /**
     * 创建一个任务视图数据
     * @return TaskView
     */
    public static ExternalCommandTaskView createTaskView() {
        return new ExternalCommandTaskView();
    }

    /**
     * 创建一个任务流视图数据
     * @param root [in] 根任务对象
     * @return WorkflowView
     */
    public static ExternalCommandWorkflowView createWorkflowView(ExternalCommandTaskView root) {
        return new ExternalCommandWorkflowView(root);
    }

    /**
     * 创建一个任务执行器的统计视图数据
     * @param type [in] 任务执行器类型
     * @return ExecutorStatisticsView
     */
    public static ExecutorStatisticsView createExecutorStatistics(String type) {
        return new ExecutorStatisticsView(type);
    }

    /**
     * 创建一个任务流对象
     * @param define [in] 任务流的JSON定义
     * @return IWorkflow
     */
    public static IWorkflow createExternalTaskWorkflow(JsonObject define) {
        ExternalCommandWorkflow workflow = new ExternalCommandWorkflow();
        for (String taskId : define.getMap().keySet()) {
            JsonObject taskBody = define.getJsonObject(taskId);
            ExternalCommandTask task = new ExternalCommandTask(taskId);
            // 如果没有设定group，默认行为group=id
            task.meta.setGroup(taskBody.getString(TASK_GROUP, taskId));
            task.meta.setDesc(taskBody.getString(TASK_DESC));
            task.meta.setCmd(taskBody.getString(TASK_CMD_LINE));
            JsonArray dependsArray = taskBody.getJsonArray(TASK_DEPENDS_LIST, new JsonArray());
            dependsArray.stream().forEach(dependTaskId -> task.meta.addDepends(dependTaskId.toString()));
            workflow.addTask(task);
        }
        return workflow.build();
    }

}
