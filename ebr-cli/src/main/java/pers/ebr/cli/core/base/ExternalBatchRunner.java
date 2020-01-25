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
package pers.ebr.cli.core.base;

import pers.ebr.cli.core.*;
import pers.ebr.cli.core.base.Broker.BaseBroker;
import pers.ebr.cli.core.base.Broker.Id;
import pers.ebr.cli.core.data.JobState;
import pers.ebr.cli.core.jobs.JobExecuteBroker;
import pers.ebr.cli.core.jobs.JobItemBuilder;
import pers.ebr.cli.core.jobs.JobStateManagementBroker;
import pers.ebr.cli.core.util.AppLogger;
import pers.ebr.cli.core.util.bus.AsyncMessageBus;
import pers.ebr.cli.core.util.bus.MessageSubscriber;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pers.ebr.cli.core.ServiceEvent.Symbols.JOB_STATE;
import static pers.ebr.cli.core.ServiceEvent.Symbols.JOB_URL;
import static pers.ebr.cli.core.ServiceEvent.Type.*;
import static pers.ebr.cli.core.base.Message.Symbols.*;
import static pers.ebr.cli.core.util.MiscUtils.checkNotNull;

/**
 *
 * @author l.gong
 */
public class ExternalBatchRunner implements ExternalBatchRunnerService, MessageSubscriber<Message> {
    /** 单例 */
    private static class RunnerHolder{
        static final ExternalBatchRunner RUNNER = new ExternalBatchRunner();
    }

    private static final int INIT_CAP = 8;
    private ServiceBuilder builder;
    private ServiceEventListener listener;
    private ServiceEventImpl serviceEvent;
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
        builder = null;
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
    public ExternalBatchRunner init(ServiceBuilder builder) {
        synchronized (RunnerHolder.RUNNER) {
            if (this.builder != null) {
                return this;
            }
            try {
                this.builder = builder;

                singleEventDispatcher = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(1024), new EbrThreadFactory("ebr-message-"), new ThreadPoolExecutor.AbortPolicy());
                messageBus = new AsyncMessageBus(singleEventDispatcher);
                // workerThreadPool
                int minNum = Runtime.getRuntime().availableProcessors();
                int maxNum = minNum * 2;
                workerExecutor = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(), new EbrThreadFactory("ebr-worker-"));

                if (this.builder.getDevMode()) {
                    AppLogger.init();
                }

                // 在此按需要初始化的顺序来添加处理程序定义
                BaseBroker broker = new JobStateManagementBroker();
                broker.init();
                servicePool.put(Id.MANAGEMENT, broker);
                broker = new JobExecuteBroker();
                broker.init();
                servicePool.put(Id.EXECUTOR, broker);

                messageBus.subscribe(Message.class, this);
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
     *
     * @param message 消息体
     */
    @Override
    public void onMessage(Message message) {
        AppLogger.debug(message.toString());

        if (listener != null) {
            putServiceEvent(message);
        }

        if (MSG_ACT_SERVICE_SHUTDOWN.equals(message.act)
                || MSG_ACT_ALL_JOB_FINISHED.equals(message.act)) {
            servicePool.forEach((id, service) -> service.finish());
            return;
        }

        if (Id.APP == message.dst) {
            servicePool.forEach((id, service) -> service.receive(message));
        } else {
            BaseBroker service = servicePool.get(message.dst);
            if (service != null) {
                service.receive(message);
            }
        }
    }

    @Override
    public void setServiceEventListener(ServiceEventListener listener) {
        this.listener = listener;
        if (this.listener != null) {
            this.serviceEvent = new ServiceEventImpl();
        }
    }

    @Override
    public String createJobFlow(Task root) {
        checkNotNull(root);
        return JobItemBuilder.createJobs(root);
    }

    @Override
    public void launchJobFlow(String url) {
        HashMap<String, Object> param = new HashMap<>(1);
        param.put(Message.Symbols.MSG_DATA_JOB_FLOW_URL, url);
        messageBus.publish(new Message(MSG_ACT_LAUNCH_JOB_FLOW, Id.APP, Id.APP, param));
        run();
    }

    private void putServiceEvent(Message message) {
        switch (message.act) {
            case MSG_ACT_JOB_STATE_CHANGED:
                onJobStateChanged(message);
                break;
            case MSG_ACT_ALL_JOB_FINISHED:
                onAllJobFinished();
                break;
            case MSG_ACT_SERVICE_SHUTDOWN:
                onServiceShutdown();
                break;
            default: break;
        }
    }

    private void onJobStateChanged(Message message) {
        String url = (String) message.param.getOrDefault(MSG_DATA_JOB_URL, "");
        JobState state = (JobState) message.param.getOrDefault(MSG_DATA_NEW_JOB_STATE, "");
        serviceEvent.serviceType = JOB_STATE_CHANGED;
        serviceEvent.payLoad = Map.of(JOB_URL, url, JOB_STATE, state);
        listener.onServiceEvent(serviceEvent);
    }

    private void onAllJobFinished() {
        serviceEvent.serviceType = ALL_JOB_FINISHED;
        serviceEvent.payLoad = Map.of();
        listener.onServiceEvent(serviceEvent);
    }

    private void onServiceShutdown() {
        serviceEvent.serviceType = SERVICE_SHUTDOWN;
        serviceEvent.payLoad = Map.of();
        listener.onServiceEvent(serviceEvent);
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
