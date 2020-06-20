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

    public DagFlow buildDagTaskFlow(JsonObject define) {
        // 创建一个空的flow结构
        DagFlow flow = createDagTaskFlowStruct(define);
        // 更新Flow信息
        updateTaskFlowInfo(flow);
        // 更新DAG信息
        updateTaskGraphInfo(flow);

        return flow;
    }

    private DagFlow createDagTaskFlowStruct(JsonObject define) {
        DagFlow flow = new DagFlow();
        for (String id : define.getMap().keySet()) {
            JsonObject taskBody = define.getJsonObject(id);
            TaskImpl task = new TaskImpl(id);
            // 如果没有设定group，默认行为group=id
            task.groupId(taskBody.getString(TASK_GROUP, id));
            task.desc(taskBody.getString(TASK_DESC));
            task.cmdLine(taskBody.getString(TASK_CMD_LINE));
            JsonArray dependsArray = taskBody.getJsonArray(TASK_DEPENDS_LIST, new JsonArray());
            dependsArray.stream().forEach(dependTaskId -> task.deps(dependTaskId.toString()));
            flow.addTask(task);
        }
        return flow;
    }

    private void updateTaskFlowInfo(DagFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = Optional.ofNullable(flow.getTask(id)).orElseThrow();
            // 更新Flow信息
            // 判断是否是flow元素
            if (isRootTask(item)) {
                if (Optional.ofNullable(flow.flowId()).isPresent()) {
                    logger.error("only one flow can be define in a signal file. id:{}", item.id());
                    throw new RuntimeException(String.format("only one flow can be define in a signal file. id:[%s]", item.id()));
                }
                flow.flowId(item.id());
                flow.addTaskGraph(item.id(), makeEmptyGraph());
            }

            TaskImpl groupItem = Optional.ofNullable(flow.getTask(item.groupId())).orElseThrow();
            if (GROUP != groupItem.type()) {
                groupItem.type(GROUP);
            }
            if (!groupItem.id().equals(item.id())) {
                groupItem.subs(item);
            }
        });
    }

    private void updateTaskGraphInfo(DagFlow flow) {
        flow.taskIdStream().forEach(id -> {
            TaskImpl item = Optional.ofNullable(flow.getTask(id)).orElseThrow();
            if (isRootTask(item)) {
                // do nothing
                return;
            }
            TaskImpl groupItem = Optional.ofNullable(flow.getTask(item.groupId())).orElseThrow();
            item.url(String.format("%s/%s", groupItem.url(), item.id()));

            DirectedGraph<ITask> groupGraph = Optional.ofNullable(flow.getMutableTaskGraph(item.groupId())).orElseGet(() -> {
                flow.addTaskGraph(item.groupId(), makeEmptyGraph());
                return Optional.ofNullable(flow.getMutableTaskGraph(item.groupId())).orElseThrow();
            });
            groupGraph.addVertex(item);

            item.deps().forEach(depTaskId -> {
                TaskImpl predecessor = Optional.ofNullable(flow.getTask(depTaskId)).orElseThrow();
                groupGraph.putEdge(predecessor, item);
            });
        });
    }

    private DirectedGraph<ITask> makeEmptyGraph() {
        return GraphBuilder.directed().setAllowsSelfLoops(false).build();
    }

    private boolean isRootTask(TaskImpl item) {
        return item.id() == null || item.id().strip().isEmpty() || item.id().equalsIgnoreCase(item.groupId());
    }
}
