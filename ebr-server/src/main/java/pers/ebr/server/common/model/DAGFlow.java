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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.graph.DirectedGraph;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * <pre>
 * The TaskFlow Class
 * </pre>
 *
 * @author l.gong
 */
public final class DAGFlow {
    private final static Logger logger = LoggerFactory.getLogger(DAGFlow.class);
    private ITask rootTask = null;
    private String instanceId = null;
    /**
     * key : task's url
     * value : task instance
     */
    private final HashMap<String, TaskImpl> urlTaskMap = new HashMap<>();
    /**
     * key : task's id
     * value : task instance
     */
    private final HashMap<String, TaskImpl> idTaskMap = new HashMap<>();
    /**
     * key : group task's id
     * value : dag graph
     */
    private final HashMap<String, DirectedGraph<ITask>> allGraphs = new HashMap<>();

    public DAGFlow() {
    }

    DAGFlow build() {
        idTaskMap.forEach((id, task) -> urlTaskMap.put(task.getUrl(), task));
        return this;
    }

    public void release() {
        allGraphs.clear();
        urlTaskMap.clear();
        idTaskMap.forEach((id, task) -> task.release());
        idTaskMap.clear();
    }


    void setRootTask(ITask task) {
        rootTask = task;
    }

    void addTask(TaskImpl task) {
        idTaskMap.put(task.getId(), task);
    }

    void addTaskGraph(String graphId, DirectedGraph<ITask> graph) {
        allGraphs.put(graphId, graph);
    }

    public void setInstanceId(String newId) {
        instanceId = newId;
        idTaskMap.forEach((id, task) -> {
            task.setInstanceId(instanceId);
        });
    }

    public TaskImpl getTaskByUrl(String taskUrl) {
        return urlTaskMap.get(taskUrl);
    }

    public TaskImpl getTaskById(String taskId) {
        return idTaskMap.get(taskId);
    }

    public DirectedGraph<ITask> getMutableTaskGraph(String taskId) {
        return allGraphs.get(taskId);
    }

    public Set<ITask> getSuccessors(ITask parent, ITask target) {
        DirectedGraph<ITask> subTasks = allGraphs.get(parent.getId());
        if (GROUP == parent.getType() && subTasks != null) {
            return subTasks.successors(target);
        }
        throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.getId(), parent.getUrl()));
    }

    public Set<ITask> getPredecessors(ITask parent, ITask target) {
        DirectedGraph<ITask> subTasks = allGraphs.get(parent.getId());
        if (GROUP == parent.getType() && subTasks != null) {
            return subTasks.predecessors(target);
        }
        throw new RuntimeException(String.format("there is no any sub task [id:%s, url:%s]", parent.getId(), parent.getUrl()));
    }

    public ITask getRootTask() {
        return rootTask;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Stream<String> getTaskIdStream() {
        return idTaskMap.keySet().stream();
    }

    public boolean isEmpty() {
        return urlTaskMap.isEmpty();
    }

    public TaskState getStatus() {
        if (Optional.ofNullable(rootTask).isEmpty()) {
            throw new RuntimeException("Root Task is empty...");
        }
        return rootTask.getStatus();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        allGraphs.forEach((id, graph) -> sb.append(String.format("\n%s : (\n%s\n)", id, graph.toString())));
        return sb.toString();
    }
}
