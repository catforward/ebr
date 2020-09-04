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
package pers.ebr.server.application;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.common.repository.Repository;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.TaskState.UNKNOWN;
import static pers.ebr.server.application.AppTopic.MSG_TASK_STATE_CHANGED;

/**
 * The TaskStateSaverVerticle
 *
 * @author l.gong
 */
public class TaskStateLoggerVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskStateLoggerVerticle.class);

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
        String taskUrl = msg.body().getString(MSG_PARAM_TASK_PATH, "");
        String taskInstanceId = msg.body().getString(MSG_PARAM_INSTANCE_ID, "");
        TaskState newState = TaskState.valueOf(msg.body().getString(MSG_PARAM_TASK_STATE, "-1"));
        if (taskUrl.isBlank() || taskInstanceId.isBlank() || UNKNOWN == newState) {
            return;
        }
        try {
            Repository.getDb().saveTaskExecHist(taskInstanceId, taskUrl, newState);
        } catch (Exception ex) {
            logger.error("procedure [handleTaskStateChanged] error:", ex);
        }
    }

}
