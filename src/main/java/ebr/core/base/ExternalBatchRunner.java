/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.core.base;

import ebr.core.ExternalBatchRunnerService;
import ebr.core.ServiceBuilder;
import ebr.core.ServiceEventListener;
import ebr.core.base.Broker.BaseBroker;
import ebr.core.base.Broker.Id;
import ebr.core.data.JobState;
import ebr.core.Task;
import ebr.core.jobs.JobExecuteBroker;
import ebr.core.jobs.JobItemBuilder;
import ebr.core.jobs.JobStateManagementBroker;
import ebr.core.util.AppLogger;
import ebr.core.util.bus.AsyncMessageBus;
import ebr.core.util.bus.MessageSubscriber;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static ebr.core.base.Message.Symbols.*;
import static ebr.core.util.MiscUtils.checkNotNull;

/**
 *
 * @author catforward
 */
public final class ExternalBatchRunner implements ExternalBatchRunnerService, MessageSubscriber<Message> {
    private final static int INIT_CAP = 8;
    private ServiceBuilder builder;
    private ServiceEventListener listener;
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
    /** 单例 */
    private final static ExternalBatchRunner INSTANCE = new ExternalBatchRunner();

    /**
     * <pre>
     * ExternalBatchRunner
     * </pre>
     * @return ExternalBatchRunner ExternalBatchRunner
     */
    public static ExternalBatchRunner getInstance() {
        return INSTANCE;
    }

    private ExternalBatchRunner() {
        builder = null;
        terminated = false;
        servicePool = new LinkedHashMap<>(INIT_CAP);
    }

    /**
     * <pre>
     * 应用程序初始化
     * </pre>
     */
    public ExternalBatchRunner init(ServiceBuilder builder) {
        if (this.builder != null) {
            return this;
        }
        try {
            this.builder = builder;

            singleEventDispatcher = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024), new ThreadPoolExecutor.AbortPolicy());
            messageBus = new AsyncMessageBus(singleEventDispatcher);
            // workerThreadPool
            int minNum = (this.builder.getMinWorkerNum() == 0) ? Runtime.getRuntime().availableProcessors() : this.builder.getMinWorkerNum();
            int maxNum = (this.builder.getMaxWorkerNum() == 0) ? minNum * 2 : this.builder.getMaxWorkerNum();
            workerExecutor = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

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
            throw new RuntimeException(ex);
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
        if (!builder.isServiceMode()) {
            finish();
        }
    }

    /**
     * <pre>
     * 取得消息总线实例
     * </pre>
     *
     * @return AsyncMessageBus 消息总线实例
     */
    static AsyncMessageBus getMessageBus() {
        return INSTANCE.messageBus;
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

        if (listener != null) {
            // TODO
        }
    }

    @Override
    public void setServiceEventListener(ServiceEventListener listener) {
        this.listener = listener;
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
}
