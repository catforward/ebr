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
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.DagFlow;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.common.pool.IPool;
import pers.ebr.server.common.pool.Pool;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Utils.checkNotNull;
import static pers.ebr.server.common.Topic.*;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class DAGScheduler extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(DAGScheduler.class);

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
        String taskUrl = msg.body().getString(MSG_PARAM_TASK_URL, "");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "UNKNOWN"));
        switch (newState) {
            case ACTIVE:
            case COMPLETE:
            case FAILED: {
                updateTaskState(taskUrl, newState);
                break;
            }
            default: {
                logger.warn("ignore this event ({}:{})", taskUrl, newState.name());
            }
        }
    }

    private void updateTaskState(String taskUrl, TaskState newState) {
        IPool pool = Pool.get();
        ITask target = pool.getTaskItem(taskUrl);
        checkNotNull(target);
        ITask parent = target.groupId().equals(target.id()) ? target : pool.getTaskItem(target.groupId());
        checkNotNull(parent);
        DagFlow flow = pool.getFlowItemOf(taskUrl);
        checkNotNull(flow);

        target.status(newState);

        // notice the parent is dead
        if (FAILED == target.status() && !parent.id().equals(target.id())) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_URL, target.url());
            param.put(MSG_PARAM_TASK_STATE, FAILED);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
            return;
        }

        // otherwise check state of the group
        long cnt = parent.subs().stream().filter(t -> t.status() != COMPLETE).count();
        if (cnt == 0 && !parent.id().equals(target.id())) {
            JsonObject param = new JsonObject();
            param.put(MSG_PARAM_TASK_URL, target.url());
            param.put(MSG_PARAM_TASK_STATE, COMPLETE);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, param);
        }

        // find the executable task for next step
        gatherExecutableTasks(flow, parent, target);
    }

    private void gatherExecutableTasks(DagFlow flow, ITask parent, ITask task) {
        IPool pool = Pool.get();
        if (GROUP == task.type() && COMPLETE != task.status()) {
            task.subs().forEach(subTask -> {
                if (subTask.deps().isEmpty() && INACTIVE == subTask.status()) {
                    pool.addTaskQueue(subTask);
                }
            });
            return;
        }

        for (var postTask : flow.getSuccessors(parent, task)) {
            if (GROUP == postTask.type()) {
                gatherExecutableTasks(flow, pool.getTaskItem(postTask.groupId()), postTask);
            } else {
                if (flow.getPredecessors(parent, postTask).stream().noneMatch(t -> COMPLETE != t.status())) {
                    pool.addTaskQueue(postTask);
                }
            }
        }
    }

}
