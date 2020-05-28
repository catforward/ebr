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

import static pers.ebr.server.common.model.Task.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The Builder Of Task Item
 *
 * @author l.gong
 */
public class ItemBuilder {
    private final static Logger logger = LoggerFactory.getLogger(ItemBuilder.class);

    public ItemBuilder() {}

    public TaskFlow buildTaskFlow(JsonObject define) {
        // 创建一个空的flow结构
        TaskFlow flow = createTaskFlowStruct(define);
        // 更新Flow信息
        updateTaskFlowInfo(flow);
        // 更新DAG信息
        updateTaskGraphInfo(flow);

        return flow;
    }

    private TaskFlow createTaskFlowStruct(JsonObject define) {
        TaskFlow flow = new TaskFlow();
        for (String id : define.getMap().keySet()) {
            JsonObject taskBody = define.getJsonObject(id);
            // System.out.println(id + ":" + taskBody.toString());
            TaskImpl task = new TaskImpl(id);
            task.groupId(taskBody.getString(TASK_GROUP, id)); // 如果没有设定group，默认行为group=id
            task.desc(taskBody.getString(TASK_DESC));
            task.cmdLine(taskBody.getString(TASK_CMD_LINE));
            JsonArray dependsArray = taskBody.getJsonArray(TASK_DEPENDS_LIST, new JsonArray());
            dependsArray.stream().forEach(dependTaskId -> task.deps(dependTaskId.toString()));
            flow.addTask(task);
        }
        return flow;
    }

    private void updateTaskFlowInfo(TaskFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = flow.getTask(id).orElseThrow();
            // 更新Flow信息
            // 判断是否是flow元素
            if (item.isRootTask()) {
                if (flow.flowId().isPresent()) {
                    logger.error("only one flow can be define in a signal file. id:{}", item.id());
                    throw new RuntimeException(String.format("only one flow can be define in a signal file. id:[%s]", item.id()));
                }
                flow.flowId(item.id());
                flow.addTaskGraph(item.id(), makeEmptyGraph());
            }
            // 更新依赖Task的类型,及依赖Task的子Task集合
            item.deps().forEach(depId -> {
                TaskImpl depTask = flow.getTask(depId).orElseThrow();
                depTask.type(GROUP);
                depTask.subs(item);
            });
        });
    }

    private void updateTaskGraphInfo(TaskFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = flow.getTask(id).orElseThrow();
            if (item.isRootTask()) return;
            DirectedGraph<Task> groupGraph = flow.getMutableTaskGraph(item.groupId()).orElseGet(() -> {
                flow.addTaskGraph(item.groupId(), makeEmptyGraph());
                return flow.getMutableTaskGraph(item.groupId()).orElseThrow();
            });
            groupGraph.addVertex(item);
            item.deps().forEach(depTaskId -> {
                TaskImpl predecessor = flow.getTask(depTaskId).orElseThrow();
                groupGraph.putEdge(predecessor, item);
            });
        });
    }

    private DirectedGraph<Task> makeEmptyGraph() {
        return GraphBuilder.directed().setAllowsSelfLoops(false).build();
    }
}
