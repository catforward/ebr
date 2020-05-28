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
package pers.ebr.server.executor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.Task;
import pers.ebr.server.common.model.TaskFlow;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.common.pool.ITaskPool;
import pers.ebr.server.common.pool.TaskPool;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.MiscUtils.checkNotNull;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class DAGTaskScheduler extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(DAGTaskScheduler.class);

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(MSG_TASK_STATE_CHANGED, this::handleTaskStateChanged);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void handleTaskStateChanged(Message<JsonObject> msg) {
        String taskId = msg.body().getString(MSG_PARAM_TASK_ID, "");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "UNKNOWN"));
        if (!taskId.isEmpty() && newState != UNKNOWN) {
            updateTaskState(taskId, newState);
        } else {
            logger.warn("ignore this event ({}:{})", taskId, newState.name());
        }
    }

    private void updateTaskState(String taskId, TaskState newState) {
        ITaskPool pool = TaskPool.get();
        TaskFlow flow = pool.getFlowItemOf(taskId);
        Task target = pool.getTaskItem(taskId);
        checkNotNull(target);
        target.status(newState);
        Task parent = target.groupId().equals(target.id()) ? target : pool.getTaskItem(target.groupId());
        checkNotNull(parent);
        // notice the parent is dead
        if (FAILED == newState && !parent.id().equals(target.id())) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_ID, target.groupId());
            param.put(MSG_PARAM_TASK_STATE, FAILED);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
            return;
        }
        // otherwise check state of the group
        long cnt = parent.subs().stream().filter(t -> t.status() != COMPLETE).count();
        if (cnt == 0 && !parent.id().equals(target.id())) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_ID, target.groupId());
            param.put(MSG_PARAM_TASK_STATE, COMPLETE);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
        }
        // find the executable task for next step
        gatherExecutableTasks(flow, parent, target);
    }

    private void gatherExecutableTasks(TaskFlow flow, Task parent, Task target) {
        checkNotNull(flow);
        checkNotNull(parent);
        checkNotNull(target);
        ITaskPool pool = TaskPool.get();
        if (target.type() == GROUP && target.status() != COMPLETE) {
            target.status(ACTIVE);
            target.subs().forEach(subTask -> {
                if (subTask.deps().isEmpty() && subTask.status() == INACTIVE) {
                    subTask.status(ACTIVE);
                    pool.putTaskQueue(subTask);
                }
            });
            return;
        }

        for (var postTask : flow.getSuccessors(parent, target)) {
            if (postTask.type() == GROUP) {
                gatherExecutableTasks(flow, pool.getTaskItem(postTask.groupId()), postTask);
            } else {
                if (flow.getPredecessors(parent, postTask).stream().filter(t -> t.status() != COMPLETE).count() == 0) {
                    postTask.status(ACTIVE);
                    pool.putTaskQueue(postTask);
                }
            }
        }
    }

}
