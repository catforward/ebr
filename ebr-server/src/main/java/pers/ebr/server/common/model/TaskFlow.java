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

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static pers.ebr.server.common.model.Task.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * <pre>
 * The TaskFlow Class
 * </pre>
 *
 * @author l.gong
 */
public class TaskFlow {
    private final static Logger logger = LoggerFactory.getLogger(TaskFlow.class);
    private String flowId = null;
    private String instanceId = null;

    private final HashMap<String, TaskImpl> taskItems = new HashMap<>();
    private final HashMap<String, DirectedGraph<Task>> graphItems = new HashMap<>();

    public void addTask(TaskImpl task) {
        taskItems.put(task.id(), task);
    }

    public void addTaskGraph(String graphId, DirectedGraph<Task> graph) {
        graphItems.put(graphId, graph);
    }

    public void flowId(String newId) {
        flowId = newId;
    }

    public void instanceId(String newId) {
        instanceId = newId;
    }

    public Optional<TaskImpl> getTask(String id) {
        return Optional.ofNullable(taskItems.get(id));
    }

    public Optional<DirectedGraph<Task>> getMutableTaskGraph(String id) {
        return Optional.ofNullable(graphItems.get(id));
    }

    public Set<Task> getSuccessors(Task parent, Task target) {
        DirectedGraph<Task> subTasks = graphItems.get(parent.id());
        if (parent.type() == GROUP && subTasks != null) {
            return subTasks.successors(target);
        } else {
            throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.id(), parent.url()));
        }
    }

    public Set<Task> getPredecessors(Task parent, Task target) {
        DirectedGraph<Task> subTasks = graphItems.get(parent.id());
        if (parent.type() == GROUP && subTasks != null) {
            return subTasks.predecessors(target);
        } else {
            throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.id(), parent.url()));
        }
    }

    public Optional<String> flowId() {
        return Optional.ofNullable(flowId);
    }

    public Optional<String> instanceId() {
        return Optional.ofNullable(instanceId);
    }

    public Stream<String> taskIdStream() {
        return taskItems.keySet().stream();
    }

    public boolean isEmpty() {
        return taskItems.isEmpty();
    }

    public TaskState status() {
        if (!taskItems.containsKey(flowId)) {
            throw new RuntimeException(String.format("no task item exists (flowId: [%s])", flowId));
        }
        return taskItems.get(flowId).status();
    }

    public String toJsonString() {
        JsonObject flowObj = new JsonObject();
        taskItems.forEach((id, task) -> {
            JsonObject taskObj = new JsonObject();
            if (task.desc() != null && !task.desc().isBlank()) {
                taskObj.put(TASK_DESC, task.desc());
            }
            if (task.groupId() != null && !task.groupId().isBlank()) {
                taskObj.put(TASK_GROUP, task.groupId());
            }
            if (task.cmdLine() != null && !task.cmdLine().isBlank()) {
                taskObj.put(TASK_CMD_LINE, task.cmdLine());
            }
            if (!task.deps().isEmpty()) {
                JsonArray arr = new JsonArray();
                task.deps().forEach(arr::add);
                taskObj.put(TASK_DEPENDS_LIST, arr);
            }
            flowObj.put(id, taskObj);
        });
        return flowObj.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        graphItems.forEach((id, graph) -> {
            sb.append(String.format("\n%s : (\n%s\n)", id, graph.toString()));
        });
        return sb.toString();
    }
}
