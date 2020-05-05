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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.base.graph.DirectedGraph;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

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
    private HashMap<String, TaskImpl> taskMap = new HashMap<>();
    private HashMap<String, DirectedGraph<TaskImpl>> graphMap = new HashMap<>();

    public void addTask(TaskImpl task) {
        taskMap.put(task.id(), task);
    }

    public void addTaskGraph(String graphId, DirectedGraph<TaskImpl> graph) {
        graphMap.put(graphId, graph);
    }

    public void flowId(String id) {
        flowId = id;
    }

    public Optional<TaskImpl> getTask(String id) {
        return Optional.ofNullable(taskMap.get(id));
    }

    public Optional<DirectedGraph<TaskImpl>> getMutableTaskGraph(String id) {
        return Optional.ofNullable(graphMap.get(id));
    }

    public Optional<String> flowId() {
        return Optional.ofNullable(flowId);
    }

    public Stream<String> taskIdStream() {
        return taskMap.keySet().stream();
    }

    public boolean isEmpty() {
        return taskMap.isEmpty();
    }

    public boolean contains(String id) {
        return taskMap.containsKey(id);
    }

    public String toJsonString() {
        JsonObject flowObj = new JsonObject();
        // TODO
        taskMap.forEach((id, task) -> {
            JsonObject taskObj = new JsonObject();
            if (task.desc() != null && !task.desc().isBlank()) {
                taskObj.put("desc", task.desc());
            }
            if (task.groupId() != null && !task.groupId().isBlank()) {
                taskObj.put("group", task.groupId());
            }
            if (task.cmdLine() != null && !task.cmdLine().isBlank()) {
                taskObj.put("cmd", task.cmdLine());
            }
            if (!task.depends().isEmpty()) {
                JsonArray arr = new JsonArray();
                task.depends().forEach(arr::add);
                taskObj.put("depends", arr);
            }
            flowObj.put(id, taskObj);
        });
        return flowObj.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        graphMap.forEach((id, graph) -> {
            sb.append(String.format("\n%s : (\n%s\n)", id, graph.toString()));
        });
        return sb.toString();
    }
}
