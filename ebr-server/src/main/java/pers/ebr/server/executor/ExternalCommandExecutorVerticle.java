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
import pers.ebr.server.model.IExternalCommandTask;
import pers.ebr.server.model.ModelItemBuilder;
import pers.ebr.server.model.ExecutorStatisticsView;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.repository.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MAX;
import static pers.ebr.server.common.Configs.KEY_EXECUTOR_MIN;
import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.common.TaskState.*;
import static pers.ebr.server.common.TaskType.GROUP;
import static pers.ebr.server.common.Topic.*;

/**
 * The Runner Verticle
 *
 * @author l.gong
 */
public class ExternalCommandExecutorVerticle extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ExternalCommandExecutorVerticle.class);

    public final static String TYPE = "EC";
    private final ExecutorStatisticsView statistics = ModelItemBuilder.createExecutorStatistics(TYPE);
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
                new LinkedBlockingQueue<>(), new RunnerThreadFactory("ebr-executor-"));

        timerId = vertx.setPeriodic(1000, this::handlePeriodic);
        vertx.eventBus().consumer(MSG_EXEC_STATISTICS, this::handleGetExecStatistics);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executorPool.shutdown();
        vertx.cancelTimer(timerId);
        statistics.reset();
    }

    private void handlePeriodic(Long id) {
        IExternalCommandTask task;
        while ((task = Repository.getPool().pollRunnableTaskQueue()) != null) {
            if (GROUP != task.getType()) {
                launchExecutableTask(task);
            }
        }
    }

    private void handleGetExecStatistics(Message<JsonObject> msg) {
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
     * @return CompletableFuture
     */
    private CompletableFuture<TaskState> deployTaskAsync(Supplier<TaskState> task) {
        return CompletableFuture.supplyAsync(task, executorPool);
    }

    private void launchExecutableTask(IExternalCommandTask task) {
        logger.info("Launch Task[instanceId:{} id:{} command:{}]", task.getInstanceId(), task.getId(), task.getCmdLine());
        JsonObject beginNoticeParam = new JsonObject();
        beginNoticeParam.put(MSG_PARAM_INSTANCE_ID, task.getInstanceId());
        beginNoticeParam.put(MSG_PARAM_TASK_PATH, task.getPath());
        beginNoticeParam.put(MSG_PARAM_TASK_STATE, ACTIVE.toString());
        vertx.eventBus().publish(MSG_EXEC_RESULT, beginNoticeParam);

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
            endNoticeParam.put(MSG_PARAM_TASK_PATH, task.getPath());
            endNoticeParam.put(MSG_PARAM_TASK_STATE, retValue.toString());
            vertx.eventBus().publish(MSG_EXEC_RESULT, endNoticeParam);

            updateExecStatisticsData(retValue);
        });
    }

}
