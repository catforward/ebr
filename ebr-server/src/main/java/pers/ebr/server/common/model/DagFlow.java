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

import static pers.ebr.server.common.model.ITask.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * <pre>
 * The TaskFlow Class
 * </pre>
 *
 * @author l.gong
 */
public final class DagFlow {
    private final static Logger logger = LoggerFactory.getLogger(DagFlow.class);
    private String flowId = null;
    /**
     * key : url
     * value : task instance
     */
    private final HashMap<String, TaskImpl> taskItems = new HashMap<>();
    /**
     * key : url
     * value : dag graph
     */
    private final HashMap<String, DirectedGraph<ITask>> graphItems = new HashMap<>();

    public DagFlow() {
    }

    public void flowId(String newId) {
        flowId = newId;
    }

    public void addTask(TaskImpl task) {
        taskItems.put(task.id(), task);
    }

    public void addTaskGraph(String graphId, DirectedGraph<ITask> graph) {
        graphItems.put(graphId, graph);
    }


    public TaskImpl getTask(String id) {
        return taskItems.get(id);
    }

    public DirectedGraph<ITask> getMutableTaskGraph(String id) {
        return graphItems.get(id);
    }

    public Set<ITask> getSuccessors(ITask parent, ITask target) {
        DirectedGraph<ITask> subTasks = graphItems.get(parent.id());
        if (parent.type() == GROUP && subTasks != null) {
            return subTasks.successors(target);
        } else {
            throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.id(), parent.url()));
        }
    }

    public Set<ITask> getPredecessors(ITask parent, ITask target) {
        DirectedGraph<ITask> subTasks = graphItems.get(parent.id());
        if (parent.type() == GROUP && subTasks != null) {
            return subTasks.predecessors(target);
        } else {
            throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.id(), parent.url()));
        }
    }

    public String flowId() {
        return flowId;
    }

    public String url() {
        if (flowId == null || flowId.isBlank()) {
            throw new RuntimeException("Flow ID is empty...");
        }
        return String.format("/%s", flowId);
    }

    public Stream<String> taskIdStream() {
        return taskItems.keySet().stream();
    }

    public boolean isEmpty() {
        return taskItems.isEmpty();
    }

    public TaskState status() {
        return Optional.ofNullable(taskItems.get(flowId)).orElseThrow().status();
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
