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
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.common.pool.IPool;
import pers.ebr.server.common.pool.Pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MAX;
import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MIN;
import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.MSG_TASK_STATE_CHANGED;
import static pers.ebr.server.common.model.TaskState.*;

/**
 * The Runner Verticle
 *
 * @author l.gong
 */
public class TaskExecutor extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    /** 执行队列 */
    private ExecutorService executorPool;
    long timerId;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        Integer minNum = config.getInteger(KEY_EXECUTOR_MIN);
        Integer maxNum = config.getInteger(KEY_EXECUTOR_MAX);
        executorPool = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new RunnerThreadFactory("ebr-runner-"));

        timerId = vertx.setPeriodic(1000, this::handlePeriodic);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executorPool.shutdown();
        vertx.cancelTimer(timerId);
    }

    private void handlePeriodic(Long id) {
        //logger.debug("Timer 1 fired: {}", id);
        IPool pool = Pool.get();
        ITask task = null;
        while ((task = pool.pollRunnableTaskQueue()) != null) {
            launchExecutableTask(task);
        }
    }

    /**
     * <pre>
     * 将一个任务提交至worker线程池排队执行
     * </pre>
     *
     * @param task    任务
     * @return Future 执行结果
     */
    private CompletableFuture<TaskState> deployTaskAsync(Supplier<TaskState> task) {
        return CompletableFuture.supplyAsync(task, executorPool);
    }

    private void launchExecutableTask(ITask task) {
        logger.info("Perform Task[instanceId:{} id:{} command:{}]", task.getInstanceId(), task.getId(), task.getCmdLine());
        JsonObject beginNoticeParam = new JsonObject();
        beginNoticeParam.put(MSG_PARAM_TASK_INSTANCE_ID, task.getInstanceId());
        beginNoticeParam.put(MSG_PARAM_TASK_URL, task.getUrl());
        beginNoticeParam.put(MSG_PARAM_TASK_STATE, ACTIVE);
        vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, beginNoticeParam);

        CompletableFuture<TaskState> future = deployTaskAsync(() -> {
            try {
                Process process = Runtime.getRuntime().exec(task.getCmdLine());
                process.getOutputStream().close();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.debug(line);
                    }
                }
                int exitCode = process.waitFor();
                logger.debug("Task[id = {} exitCode = {}]", task.getId(), exitCode);
                return (exitCode == 0) ? COMPLETE : FAILED;
            } catch (IOException | InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return FAILED;
            }
        });

        future.whenComplete((retValue, exception) -> {
            JsonObject endNoticeParam = new JsonObject();
            endNoticeParam.put(MSG_PARAM_TASK_INSTANCE_ID, task.getInstanceId());
            endNoticeParam.put(MSG_PARAM_TASK_URL, task.getUrl());
            endNoticeParam.put(MSG_PARAM_TASK_STATE, retValue);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, endNoticeParam);
        });
    }

}
