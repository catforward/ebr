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
package pers.ebr.server.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.base.graph.DirectedGraph;
import pers.ebr.server.base.graph.GraphBuilder;
import pers.ebr.server.model.TaskFlow;
import pers.ebr.server.model.TaskImpl;

import java.util.Optional;

import static pers.ebr.server.constant.Global.*;
import static pers.ebr.server.constant.TaskType.GROUP;

/**
 * The TaskItemCreateService
 *
 * @author l.gong
 */
public class TaskItemCreateService {

    private final static Logger logger = LoggerFactory.getLogger(TaskItemCreateService.class);

    public Optional<TaskFlow> createTaskFlowStruct(JsonObject flowObj) {
        TaskFlow flow = new TaskFlow();
        for (String id : flowObj.getMap().keySet()) {
            JsonObject taskBody = flowObj.getJsonObject(id);
            // System.out.println(id + ":" + taskBody.toString());
            TaskImpl task = new TaskImpl(id);
            task.groupId(taskBody.getString(TASK_GROUP, id)); // 如果没有设定group，默认行为group=id
            task.desc(taskBody.getString(TASK_DESC));
            task.cmdLine(taskBody.getString(TASK_CMD_LINE));
            JsonArray dependsArray = taskBody.getJsonArray(TASK_DEPENDS_LIST, new JsonArray());
            dependsArray.stream().forEach(dependTaskId -> task.depends(dependTaskId.toString()));
            flow.addTask(task);
        }
        return Optional.of(flow);
    }

    public void buildTaskFlow(TaskFlow flow) {
        // 更新Flow信息
        updateTaskFlowInfo(flow);
        // 更新DAG信息
        updateTaskGraphInfo(flow);
    }

    private void updateTaskFlowInfo(TaskFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = flow.getTask(id).orElseThrow();
            // 更新Flow信息
            // 判断是否是flow元素
            if (item.isFlowItem()) {
                if (flow.flowItem().isPresent()) {
                    logger.error("only one flow can be define in a signal file. id:{}", item.id());
                    throw new RuntimeException(String.format("only one flow can be define in a signal file. id:[%s]", item.id()));
                }
                flow.flowItem(item);
                flow.addTaskGraph(item.id(), makeEmptyGraph());
            }
            // 更新依赖Task的类型
            item.depends().forEach(depId -> flow.getTask(depId).orElseThrow().type(GROUP));
        });
    }

    private void updateTaskGraphInfo(TaskFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = flow.getTask(id).orElseThrow();
            if (item.isFlowItem()) return;
            DirectedGraph<TaskImpl> groupGraph = flow.getMutableTaskGraph(item.groupId()).orElseGet(() -> {
                flow.addTaskGraph(item.groupId(), makeEmptyGraph());
                return flow.getMutableTaskGraph(item.groupId()).orElseThrow();
            });
            groupGraph.addVertex(item);
            item.depends().forEach(depTaskId -> {
                TaskImpl predecessor = flow.getTask(depTaskId).orElseThrow();
                groupGraph.putEdge(predecessor, item);
            });
        });
    }

    private DirectedGraph<TaskImpl> makeEmptyGraph() {
        return GraphBuilder.directed().setAllowsSelfLoops(false).build();
    }

}
