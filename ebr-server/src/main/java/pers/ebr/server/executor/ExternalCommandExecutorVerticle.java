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
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.ModelItemBuilder;
import pers.ebr.server.common.model.ExecutorStatistics;
import pers.ebr.server.common.model.TaskState;
import pers.ebr.server.pool.IPool;
import pers.ebr.server.pool.Pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MAX;
import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MIN;
import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.Topic.MSG_EXEC_STATISTICS;
import static pers.ebr.server.common.Topic.MSG_TASK_STATE_CHANGED;
import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.GROUP;

/**
 * The Runner Verticle
 *
 * @author l.gong
 */
public class ExternalCommandExecutorVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ExternalCommandExecutorVerticle.class);

    public final static String TYPE = "EC";
    private final ExecutorStatistics statistics = ModelItemBuilder.buildExecutorStatistics(TYPE);
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
        vertx.eventBus().consumer(MSG_EXEC_STATISTICS, this::handleGetExecSummary);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executorPool.shutdown();
        vertx.cancelTimer(timerId);
        statistics.reset();
    }

    private void handlePeriodic(Long id) {
        IPool pool = Pool.get();
        ITask task;
        while ((task = pool.pollRunnableTaskQueue()) != null) {
            if (GROUP == task.getType()) {
                JsonObject noticeParam = new JsonObject();
                noticeParam.put(MSG_PARAM_INSTANCE_ID, task.getInstanceId());
                noticeParam.put(MSG_PARAM_TASK_URL, task.getUrl());
                noticeParam.put(MSG_PARAM_TASK_STATE, ACTIVE);
                vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, noticeParam);
            } else{
                launchExecutableTask(task);
            }
        }
    }

    private void handleGetExecSummary(Message<JsonObject> msg) {
        JsonObject result = new JsonObject();
        result.put(TYPE, statistics.toJsonObject());
        msg.reply(result);
    }

    private void updateExecStatisticsData(TaskState newState) {
        switch (newState) {
            case ACTIVE: {
                statistics.incActiveCnt();
                break;
            }
            case COMPLETE: {
                statistics.decActiveCnt();
                statistics.incCompleteSumCnt();
                break;
            }
            case FAILED: {
                statistics.decActiveCnt();
                statistics.incFailedSumCnt();
                break;
            }
            default: { break; }
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
        beginNoticeParam.put(MSG_PARAM_INSTANCE_ID, task.getInstanceId());
        beginNoticeParam.put(MSG_PARAM_TASK_URL, task.getUrl());
        beginNoticeParam.put(MSG_PARAM_TASK_STATE, ACTIVE);
        vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, beginNoticeParam);

        updateExecStatisticsData(ACTIVE);

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
            endNoticeParam.put(MSG_PARAM_INSTANCE_ID, task.getInstanceId());
            endNoticeParam.put(MSG_PARAM_TASK_URL, task.getUrl());
            endNoticeParam.put(MSG_PARAM_TASK_STATE, retValue);
            vertx.eventBus().publish(MSG_TASK_STATE_CHANGED, endNoticeParam);

            updateExecStatisticsData(retValue);
        });
    }

}
