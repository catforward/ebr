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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.graph.DirectedGraph;
import pers.ebr.server.common.graph.GraphBuilder;

import java.util.Optional;

import static pers.ebr.server.common.model.ITask.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The Builder Of Task Item
 *
 * @author l.gong
 */
public final class ItemBuilder {
    private final static Logger logger = LoggerFactory.getLogger(ItemBuilder.class);

    public ItemBuilder() {}

    public DAGFlow buildDagTaskFlow(JsonObject define) {
        DAGFlow flow = createDAGFlow(define);
        updateFlowInfo(flow);
        updateDAGInfo(flow);
        return flow.build();
    }

    private DAGFlow createDAGFlow(JsonObject define) {
        DAGFlow flow = new DAGFlow();
        for (String taskId : define.getMap().keySet()) {
            JsonObject taskBody = define.getJsonObject(taskId);
            TaskImpl task = new TaskImpl(taskId);
            // 如果没有设定group，默认行为group=id
            task.setGroupId(taskBody.getString(TASK_GROUP, taskId));
            task.setDesc(taskBody.getString(TASK_DESC));
            task.setCmdLine(taskBody.getString(TASK_CMD_LINE));
            JsonArray dependsArray = taskBody.getJsonArray(TASK_DEPENDS_LIST, new JsonArray());
            dependsArray.stream().forEach(dependTaskId -> task.addDependId(dependTaskId.toString()));
            flow.addTask(task);
        }
        return flow;
    }

    private void updateFlowInfo(DAGFlow flow) {
        flow.getTaskIdStream().forEach(id -> {
            TaskImpl task = Optional.ofNullable(flow.getTaskById(id)).orElseThrow();
            // 更新Flow信息
            // 判断是否是flow元素
            if (isRootTask(task)) {
                if (Optional.ofNullable(flow.getRootTask()).isPresent()) {
                    logger.error("only one flow can be define in a signal file. id:{}", task.getId());
                    throw new RuntimeException(String.format("only one flow can be define in a signal file. id:[%s]", task.getId()));
                }
                task.setGroup(task);
                task.setUrl(String.format("/%s", task.getId()));
                flow.setRootTask(task);
                flow.addTaskGraph(task.getId(), makeEmptyGraph());
            } else {
                TaskImpl groupTask = Optional.ofNullable(flow.getTaskById(task.getGroupId())).orElseThrow();
                if (GROUP != groupTask.getType()) {
                    groupTask.setType(GROUP);
                }
                groupTask.addSubTask(task);
                task.getDependIdList().forEach(depId -> {
                    TaskImpl depTask = Optional.ofNullable(flow.getTaskById(depId)).orElseThrow();
                    task.addDependTask(depTask);
                });
                task.setGroup(groupTask);
                task.setUrl(String.format("%s/%s", groupTask.getUrl(), task.getId()));
            }
        });
    }

    private void updateDAGInfo(DAGFlow flow) {
        flow.getTaskIdStream().forEach(id -> {
            TaskImpl task = Optional.ofNullable(flow.getTaskById(id)).orElseThrow();
            if (isRootTask(task)) {
                // do nothing
                return;
            }

            DirectedGraph<ITask> groupGraph = Optional.ofNullable(flow.getMutableTaskGraph(task.getGroupId())).orElseGet(() -> {
                flow.addTaskGraph(task.getGroupId(), makeEmptyGraph());
                return Optional.ofNullable(flow.getMutableTaskGraph(task.getGroupId())).orElseThrow();
            });
            groupGraph.addVertex(task);

            task.getDependIdList().forEach(depTaskId -> {
                TaskImpl predecessor = Optional.ofNullable(flow.getTaskById(depTaskId)).orElseThrow();
                groupGraph.putEdge(predecessor, task);
            });
        });
    }

    private DirectedGraph<ITask> makeEmptyGraph() {
        return GraphBuilder.directed().setAllowsSelfLoops(false).build();
    }

    private boolean isRootTask(TaskImpl item) {
        return item.getId() == null || item.getId().strip().isEmpty() || item.getId().equalsIgnoreCase(item.getGroupId());
    }
}
