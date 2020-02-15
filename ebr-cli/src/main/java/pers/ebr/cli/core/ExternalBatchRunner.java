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
package pers.ebr.cli.core;

import pers.ebr.cli.core.bus.MessageSubscriber;
import pers.ebr.cli.core.tasks.Task;
import pers.ebr.cli.core.tasks.TaskItemBuilder;
import pers.ebr.cli.core.tasks.TaskFlow;
import pers.ebr.cli.core.tasks.TaskState;
import pers.ebr.cli.util.AppLogger;
import pers.ebr.cli.core.bus.AsyncMessageBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pers.ebr.cli.core.Topic.*;

/**
 * <pre>
 * The Task Runner
 * </pre>
 *
 * @author l.gong
 */
public class ExternalBatchRunner implements MessageSubscriber {

    private static class RunnerHolder {
        static final ExternalBatchRunner RUNNER = new ExternalBatchRunner();
    }

    /** 主线程循环停止标志 */
    private boolean terminated;
    /** 事件执行线程池 （本应用同一时刻只有1个线程来执行具体处理，不考虑耗时操作的情况，因为没有打算写耗时处理） */
    private ExecutorService singleEventDispatcher;
    /** 执行队列 */
    private ExecutorService workerExecutorPool;
    /** 消息总线 */
    private AsyncMessageBus messageBus;
    /** 任务流 */
    private TaskFlow flow;

    private ExternalBatchRunner() {
        terminated = false;
        try {
            singleEventDispatcher = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024), new EbrThreadFactory("ebr-message-"), new ThreadPoolExecutor.AbortPolicy());
            messageBus = new AsyncMessageBus(singleEventDispatcher);
            // workerThreadPool
            int maxNum = Runtime.getRuntime().availableProcessors();
            int minNum = Math.min(2, maxNum);
            workerExecutorPool = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), new EbrThreadFactory("ebr-worker-"));
        } catch (Exception ex) {
            AppLogger.dumpError(ex);
            throw new EbrException(ex);
        }
    }

    /**
     * get the instance of ExternalBatchRunner
     * @return A instance of ExternalBatchRunner
     */
    static ExternalBatchRunner getInstance() {
        return RunnerHolder.RUNNER;
    }

    private void finish() {
        messageBus.unsubscribe(this);
        singleEventDispatcher.shutdown();
        workerExecutorPool.shutdown();
    }

    public static void launch() {
        getInstance().run(new TaskItemBuilder().load());
    }

    public void run(TaskFlow newFlow) {
        flow = newFlow;
        flow.setMessageBus(messageBus);
        messageBus.subscribe(TOPIC_TASK_STATE_CHANGED, this);
        messageBus.subscribe(TOPIC_TASK_LAUNCH, this);
        mainLoop();
        finish();
    }

    /**
     * <pre>
     * 将一个任务提交至worker线程池排队执行
     * </pre>
     *
     * @param task    任务
     * @return Future 执行结果
     */
    CompletableFuture<TaskState> deployTaskAsync(Supplier<TaskState> task) {
        return CompletableFuture.supplyAsync(task, workerExecutorPool);
    }

    private void mainLoop() {
        AppLogger.info("EBR MAIN LOOP START!");
        while (!terminated) {
            boolean shouldStop = false;
            try {
                switch (flow.getState()) {
                    case INACTIVE: {
                        HashMap<String, Object> param = new HashMap<>();
                        param.put(TOPIC_DATA_TASK_ID, flow.getId());
                        param.put(TOPIC_DATA_TASK_NEW_STATE, TaskState.ACTIVE);
                        messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
                        break;
                    }
                    case COMPLETE: {
                        System.err.println("All Task Done ...");
                        shouldStop = true;
                        break;
                    }
                    case FAILED: {
                        System.err.println("Task Failed ...");
                        shouldStop = true;
                        break;
                    }
                    default: break;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                AppLogger.dumpError(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                terminated = shouldStop;
            }
        }
        AppLogger.info("EBR MAIN LOOP FINISHED!");
    }

    private void launchExecutableTask(Task task) {
        AppLogger.debug(String.format("Perform Task[id:%s command:%s]", task.getId(), task.getCommand()));
        CompletableFuture<TaskState> future = deployTaskAsync(() -> {
            try {
                Process process = Runtime.getRuntime().exec(task.getCommand());
                process.getOutputStream().close();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        AppLogger.debug(line);
                    }
                }
                int exitCode = process.waitFor();
                AppLogger.debug(String.format("Task[id = %s exitCode = %s]", task.getId(), exitCode));
                return (exitCode == 0) ? TaskState.COMPLETE : TaskState.FAILED;
            } catch (IOException | InterruptedException e) {
                AppLogger.dumpError(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return TaskState.FAILED;
            }
        });
        future.whenComplete((retValue, exception) -> {
            HashMap<String, Object> param = new HashMap<>();
            param.put(TOPIC_DATA_TASK_ID, task.getId());
            param.put(TOPIC_DATA_TASK_NEW_STATE, retValue);
            messageBus.publish(TOPIC_TASK_STATE_CHANGED, param);
        });
    }

    /**
     * <pre>
     * 接受消息
     * </pre>
     *
     * @param topic   主题
     * @param message 消息体
     */
    @Override
    public void onMessage(String topic, Map<String, Object> message) {
        switch (topic) {
            case TOPIC_TASK_STATE_CHANGED : {
                String tid = (String) message.getOrDefault(TOPIC_DATA_TASK_ID, "");
                TaskState newState = (TaskState) message.get(TOPIC_DATA_TASK_NEW_STATE);
                if (!tid.isEmpty() && newState != null) {
                    flow.updateTaskState(tid, newState);
                }
                break;
            }
            case TOPIC_TASK_LAUNCH : {
                Task task = (Task) message.get(TOPIC_DATA_TASK_OBJ);
                if (task != null) {
                    launchExecutableTask(task);
                }
                @SuppressWarnings("unchecked")
                List<Task> tasks = (List<Task>) message.get(TOPIC_DATA_TASK_OBJ_LIST);
                if (tasks != null && !tasks.isEmpty()) {
                    tasks.forEach(this::launchExecutableTask);
                }
                break;
            }
            default: {
                AppLogger.debug(String.format("Unknown Topic: [%s]", topic));
                break;
            }
        }
    }

}

class EbrThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    EbrThreadFactory(final String prefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
