/*
  Copyright 2021 liang gong

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.ebr.schd;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.BaseSchdVerticle;
import pers.ebr.base.AppConfigs;
import pers.ebr.base.ServiceSymbols;
import pers.ebr.data.Task;
import pers.ebr.data.TaskRepo;
import pers.ebr.types.TaskStateEnum;
import pers.ebr.types.TaskTypeEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * <pre>Task's runner</pre>
 *
 * @author l.gong
 */
public class TaskExecVerticle extends BaseSchdVerticle {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecVerticle.class);
    private ExecutorService executorPool;
    private long timerId = 0L;
    private long checkInterval = 0L;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        Integer minNum = config.getInteger(AppConfigs.SERVICE_TASK_EXECUTOR_MINIMUM_SIZE, 2);
        Integer maxNum = config.getInteger(AppConfigs.SERVICE_TASK_EXECUTOR_MAXIMUM_SIZE, 4);
        executorPool = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new TaskRunnerThreadFactory("ebr-executor-"));
        checkInterval = config.getLong(AppConfigs.SERVICE_TASK_EXECUTOR_CHECK_INTERVAL_SECONDS, 1L) * 1000;
        timerId = vertx.setTimer(checkInterval, this::handlePeriodic);
        String deploymentId = deploymentID();
        logger.info("TaskExecVerticle started. [{}]", deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (!isNull(executorPool)) {
            executorPool.shutdown();
        }
        vertx.cancelTimer(timerId);
        String deploymentId = deploymentID();
        logger.info("TaskExecVerticle stopped. [{}]", deploymentId);
    }

    private void handlePeriodic(Long id) {
        try {
            Task task;
            while ((task = TaskRepo.pollRunnableTask()) != null) {
                if (TaskTypeEnum.FLOW == task.getType() || TaskTypeEnum.GROUP == task.getType()) {
                    notice(ServiceSymbols.MSG_STATE_TASK_RUNNING, task);
                    continue;
                }
                // task
                TaskStateEnum taskState = task.getState();
                if (TaskStateEnum.STANDBY == taskState) {
                    launchExecutableTask(task);
                } else if (TaskStateEnum.PAUSED == taskState) {
                    notice(ServiceSymbols.MSG_STATE_TASK_PAUSED, task);
                } else if (TaskStateEnum.SKIPPED == taskState) {
                    notice(ServiceSymbols.MSG_STATE_TASK_SKIPPED, task);
                } else if (TaskStateEnum.ABORTED == taskState) {
                    notice(ServiceSymbols.MSG_STATE_TASK_ABORTED, task);
                }
            }
        } finally {
            // for next time
            timerId = vertx.setTimer(checkInterval, this::handlePeriodic);
        }
    }

    private CompletableFuture<TaskStateEnum> deployTaskAsync(Supplier<TaskStateEnum> task) {
        return CompletableFuture.supplyAsync(task, executorPool);
    }

    private void launchExecutableTask(Task task) {
        logger.info("Launch Task[url:{} command:{}]", task.getUrl(), task.getScript());
        notice(ServiceSymbols.MSG_STATE_TASK_RUNNING, task);

        CompletableFuture<TaskStateEnum> future = deployTaskAsync(() -> {
            try {
                Process process = Runtime.getRuntime().exec(task.getScript());
                process.getOutputStream().close();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.debug(line);
                    }
                }
                int exitCode = process.waitFor();
                logger.debug("Task[url = {} exitCode = {}]", task.getUrl(), exitCode);
                return (exitCode == 0) ? TaskStateEnum.FINISHED : TaskStateEnum.ERROR;
            } catch (IOException | InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return TaskStateEnum.ERROR;
            }
        });

        future.whenComplete((retValue, exception) -> {
            if (TaskStateEnum.FINISHED == retValue) {
                notice(ServiceSymbols.MSG_STATE_TASK_COMPLETE, task);
            } else {
                notice(ServiceSymbols.MSG_STATE_TASK_FAILED, task);
            }
        });
    }

}
