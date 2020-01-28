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

import pers.ebr.cli.core.Broker.BaseBroker;
import pers.ebr.cli.core.Broker.Id;
import pers.ebr.cli.core.types.JobState;
import pers.ebr.cli.core.jobs.JobExecuteBroker;
import pers.ebr.cli.core.jobs.JobItemBuilder;
import pers.ebr.cli.core.jobs.JobStateManagementBroker;
import pers.ebr.cli.util.AppLogger;
import pers.ebr.cli.util.bus.AsyncMessageBus;
import pers.ebr.cli.util.bus.MessageSubscriber;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pers.ebr.cli.core.Message.*;
import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 *
 * @author l.gong
 */
public class ExternalBatchRunner implements MessageSubscriber {
    /** 单例 */
    private static class RunnerHolder{
        static final ExternalBatchRunner RUNNER = new ExternalBatchRunner();
    }

    private static final int INIT_CAP = 8;
    /** 主线程循环停止标志 */
    private boolean terminated;
    /** 提供具体功能的处理类实例 */
    private final Map<Id, BaseBroker> servicePool;
    /** 事件执行线程池 （本应用同一时刻只有1个线程来执行具体处理，不考虑耗时操作的情况，因为没有打算写耗时处理） */
    private ExecutorService singleEventDispatcher;
    /** 执行队列 */
    private ExecutorService workerExecutor;
    /** 消息总线 */
    private AsyncMessageBus messageBus;

    private ExternalBatchRunner() {
        terminated = false;
        servicePool = new LinkedHashMap<>(INIT_CAP);
    }

    /**
     * get the instance of ExternalBatchRunner
     * @return A instance of ExternalBatchRunner
     */
    public static ExternalBatchRunner getInstance() {
        return RunnerHolder.RUNNER;
    }

    /**
     * <pre>
     * 应用程序初始化
     * </pre>
     */
    public ExternalBatchRunner init() {
        synchronized (RunnerHolder.RUNNER) {
            try {
                singleEventDispatcher = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(1024), new EbrThreadFactory("ebr-message-"), new ThreadPoolExecutor.AbortPolicy());
                messageBus = new AsyncMessageBus(singleEventDispatcher);
                // workerThreadPool
                int minNum = Runtime.getRuntime().availableProcessors();
                int maxNum = minNum * 2;
                workerExecutor = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(), new EbrThreadFactory("ebr-worker-"));

                // 在此按需要初始化的顺序来添加处理程序定义
                BaseBroker broker = new JobStateManagementBroker();
                broker.init();
                servicePool.put(Id.MANAGEMENT, broker);
                broker = new JobExecuteBroker();
                broker.init();
                servicePool.put(Id.EXECUTOR, broker);

                subscribeAllTopic();
            } catch (Exception ex) {
                throw new EbrException(ex);
            }
        }
        return this;
    }

    private void finish() {
        messageBus.unsubscribe(this);
        singleEventDispatcher.shutdown();
        workerExecutor.shutdown();
    }

    private void run() {
        mainLoop();
        finish();
    }

    private void subscribeAllTopic() {
        messageBus.subscribe(MSG_ACT_SERVICE_SHUTDOWN, this);
        messageBus.subscribe(MSG_ACT_LAUNCH_JOB_FLOW, this);
        messageBus.subscribe(MSG_ACT_LAUNCH_JOB_ITEM, this);
        messageBus.subscribe(MSG_ACT_JOB_STATE_CHANGED, this);
        messageBus.subscribe(MSG_ACT_ALL_JOB_FINISHED, this);
    }

    public void launchJobFlow(Task root) {
        checkNotNull(root);
        String url = JobItemBuilder.createJobs(root);
        HashMap<String, Object> param = new HashMap<>(1);
        param.put(MSG_FROM_BROKER_ID, Id.APP);
        param.put(MSG_TO_BROKER_ID, Id.APP);
        param.put(MSG_DATA_JOB_FLOW_URL, url);
        messageBus.publish(MSG_ACT_LAUNCH_JOB_FLOW, param);
        run();
    }

    /**
     * <pre>
     * 取得消息总线实例
     * </pre>
     *
     * @return AsyncMessageBus 消息总线实例
     */
    static AsyncMessageBus getMessageBus() {
        return RunnerHolder.RUNNER.messageBus;
    }

    /**
     * <pre>
     * 将一个任务提交至worker线程池排队执行
     * </pre>
     *
     * @param task    任务
     * @return Future 执行结果
     */
    CompletableFuture<JobState> deployTaskAsync(Supplier<JobState> task) {
        return CompletableFuture.supplyAsync(task, workerExecutor);
    }

    private void mainLoop() {
        AppLogger.info("EBR MAIN LOOP START!");
        while (!terminated) {
            boolean shouldStop = true;
            try {
                for (Map.Entry<Id, BaseBroker> entry : servicePool.entrySet()) {
                    BaseBroker service = entry.getValue();
                    if (Broker.Status.FINISHED != service.status() && shouldStop) {
                        shouldStop = false;
                    }
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

    /**
     * <pre>
     * 接受消息
     * </pre>
     * @param topic 主题
     * @param message 消息体
     */
    @Override
    public void onMessage(String topic, Map<String, Object> message) {
        AppLogger.debug(message.toString());

        if (MSG_ACT_SERVICE_SHUTDOWN.equals(topic)
                || MSG_ACT_ALL_JOB_FINISHED.equals(topic)) {
            servicePool.forEach((id, service) -> service.finish());
            return;
        }
        Id dst = (Id) message.getOrDefault(MSG_TO_BROKER_ID, Id.UNKNOWN);
        if (Id.APP == dst) {
            servicePool.forEach((id, service) -> service.receive(topic, message));
        } else {
            BaseBroker service = servicePool.get(dst);
            if (service != null) {
                service.receive(topic, message);
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
        namePrefix = prefix + "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
