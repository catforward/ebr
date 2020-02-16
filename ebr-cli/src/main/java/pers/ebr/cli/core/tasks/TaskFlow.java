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
    private final List<Task> executableList = new ArrayList<>();

    TaskFlow() {}

    void addTask(Task task) {
        taskPool.put(task.id, task);
        if (task.parentId.isEmpty()) {
            root = task;
        }
        if (GROUP == task.type) {
            task.subTasks = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        }
    }

    Task getTask(String id) {
        return taskPool.get(id);
    }

    TaskFlow build() {
        for (var entry : taskPool.entrySet()) {
            Task task = entry.getValue();
            if (task.parentId.isEmpty()) {
                continue;
            }
            Task parent = taskPool.get(task.parentId);
            if(parent == null) {
                throw new EbrException(String.format("unknown task structure [id: %s, url: %s]", task.id, task.url));
            }
            parent.addSubTask(task);
            for (String dependId : task.depends) {
                Task preTask = taskPool.get(dependId);
                if (preTask == null) {
                    throw new EbrException(String.format("unknown dependency [id: %s, url: %s]", task.id, task.url));
                }
                if (!preTask.parentId.equalsIgnoreCase(task.parentId)) {
                    throw new EbrException(String.format("the parent of prerequires must be the same one [id: %s, url: %s]",
                            task.id, task.url));
                }
                parent.addSequence(preTask, task);
            }
        }
        return this;
    }

    void gatherExecutableTasks(Task parent, Task target) {
        checkNotNull(parent);
        checkNotNull(target);

        if (target.type == GROUP && target.state != COMPLETE) {
            target.updateState(ACTIVE);
            target.getSubTasks().forEach(subTask -> {
                if (subTask.depends.isEmpty() && subTask.state == INACTIVE) {
                    subTask.updateState(ACTIVE);
                    executableList.add(subTask);
                }
            });
            return;
        }

        for (var postTask : parent.getSuccessors(target)) {
            if (postTask.type == GROUP) {
                gatherExecutableTasks(taskPool.get(postTask.parentId), postTask);
            } else {
                if (parent.getPredecessors(postTask).stream().filter(t -> t.state != COMPLETE).count() == 0) {
                    postTask.updateState(ACTIVE);
                    executableList.add(postTask);
                }
            }
        }
    }

    public void updateTaskState(String taskId, TaskState newState) {
        Task target = taskPool.get(taskId);
        checkNotNull(target);
        target.updateState(newState);
        Task parent = target.parentId.isEmpty() ? target : taskPool.get(target.parentId);
        checkNotNull(parent);
        // notice the parent is dead
        if (FAILED == newState) {
            HashMap<String, Object> param = new HashMap<>();
            param.put(TOPIC_DATA_TASK_ID, target.parentId);
            param.put(TOPIC_DATA_TASK_NEW_STATE, FAILED);
            messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
            return;
        }
        // otherwise check state of the group
        long cnt = parent.getSubTasks().stream().filter(t -> t.state != COMPLETE).count();
        if (cnt == 0 && !target.parentId.isEmpty() && messageBus != null) {
            HashMap<String, Object> param = new HashMap<>();
            param.put(TOPIC_DATA_TASK_ID, target.parentId);
            param.put(TOPIC_DATA_TASK_NEW_STATE, COMPLETE);
            messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
        }
        // find the executable task for next step
        executableList.clear();
        gatherExecutableTasks(parent, target);
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
