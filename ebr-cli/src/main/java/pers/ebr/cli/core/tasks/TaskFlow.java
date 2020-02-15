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
package pers.ebr.cli.core.tasks;

import pers.ebr.cli.core.EbrException;
import pers.ebr.cli.core.bus.AsyncMessageBus;
import pers.ebr.cli.core.graph.DirectedGraph;
import pers.ebr.cli.core.graph.GraphBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pers.ebr.cli.core.Topic.*;
import static pers.ebr.cli.core.tasks.TaskState.*;
import static pers.ebr.cli.core.tasks.TaskType.GROUP;
import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 * <pre>
 * The TaskFlow Class
 * </pre>
 *
 * @author l.gong
 */
public class TaskFlow {
    private AsyncMessageBus messageBus = null;
    private Task root = null;
    private final Map<String, Task> taskPool = new HashMap<>();
    private final Map<String, DirectedGraph<Task>> taskGroups = new HashMap<>();
    private final List<Task> executableList = new ArrayList<>();

    TaskFlow() {}

    void addTask(Task task) {
        taskPool.put(task.id, task);
        if ("/".equals(task.location)) {
            root = task;
        }
        if (GROUP == task.type) {
            DirectedGraph<Task> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
            taskGroups.put(task.url, graph);
        }
    }

    Task getTask(String id) {
        return taskPool.get(id);
    }

    TaskFlow build() {
        for (var entry : taskPool.entrySet()) {
            Task task = entry.getValue();
            if ("/".equals(task.location)) {
                continue;
            }
            DirectedGraph<Task> graph = taskGroups.get(task.location);
            if(graph == null) {
                throw new EbrException(String.format("unknown task structure [id: %s, location: %s, url: %s]",
                        task.id, task.location, task.url));
            }
            graph.addVertex(task);
            for (String dependId : task.depends) {
                Task preTask = taskPool.get(dependId);
                if (preTask == null) {
                    throw new EbrException(String.format("unknown dependency [id: %s, location: %s, url: %s]",
                            task.id, task.location, task.url));
                }
                if (!preTask.location.equalsIgnoreCase(task.location)) {
                    throw new EbrException(String.format("the group of prerequires must be the same one with [id: %s, location: %s, url: %s]",
                            task.id, task.location, task.url));
                }
                graph.putEdge(preTask, task);
            }
        }
        return this;
    }

    void gatherExecutableTasks(DirectedGraph<Task> group, Task target) {
        if (group == null || target == null) {
            throw new IllegalArgumentException();
        }
        if (target.type == GROUP && target.state != COMPLETE) {
            target.updateState(ACTIVE);
            group.vertexes().forEach(subTask -> {
                if (subTask.depends.isEmpty() && subTask.state == INACTIVE) {
                    subTask.updateState(ACTIVE);
                    executableList.add(subTask);
                }
            });
        } else {
            for (var postTask : group.successors(target)) {
                if (postTask.type == GROUP) {
                    DirectedGraph<Task> postGroup = taskGroups.get(postTask.url);
                    gatherExecutableTasks(postGroup, postTask);
                } else {
                    long cnt = group.predecessors(postTask).stream().filter(t -> t.state != COMPLETE).count();
                    if (cnt == 0) {
                        postTask.updateState(ACTIVE);
                        executableList.add(postTask);
                    }
                }
            }
        }
    }

    public void updateTaskState(String taskId, TaskState newState) {
        System.err.println(String.format("state changed->task:[%s] new state:[%s]", taskId, newState));
        Task target = taskPool.get(taskId);
        checkNotNull(target);
        target.updateState(newState);
        // check task group
        String groupId = "/".equals(target.location) ? target.url : target.location;
        DirectedGraph<Task> group = taskGroups.get(groupId);
        if (group == null) {
            throw new EbrException(String.format("no task group named:[%s]", groupId));
        }
        // notice the group is dead
        if (FAILED == newState) {
            HashMap<String, Object> param = new HashMap<>();
            param.put(TOPIC_DATA_TASK_ID, target.parentId);
            param.put(TOPIC_DATA_TASK_NEW_STATE, FAILED);
            messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
            return;
        }
        // otherwise check state of the group
        long cnt = group.vertexes().stream().filter(t -> t.state != COMPLETE).count();
        if (cnt == 0 && !target.parentId.isEmpty() && messageBus != null) {
            HashMap<String, Object> param = new HashMap<>();
            param.put(TOPIC_DATA_TASK_ID, target.parentId);
            param.put(TOPIC_DATA_TASK_NEW_STATE, COMPLETE);
            messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
        }
        // find the executable task for next step
        executableList.clear();
        gatherExecutableTasks(group, target);
        if (!executableList.isEmpty()) {
            HashMap<String, Object> param = new HashMap<>();
            if (executableList.size() == 1) {
                param.put(TOPIC_DATA_TASK_OBJ, executableList.get(0));
            } else {
                param.put(TOPIC_DATA_TASK_OBJ_LIST, List.copyOf(executableList));
            }
            messageBus.publish(TOPIC_TASK_LAUNCH, param);
        }
    }

    public void setMessageBus(AsyncMessageBus bus) {
        messageBus = bus;
    }

    public String getId() {
        return root.id;
    }

    public TaskState getState() {
        return root.state;
    }

}
