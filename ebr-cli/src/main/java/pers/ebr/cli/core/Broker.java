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

import pers.ebr.cli.core.Handler.HandlerChain;
import pers.ebr.cli.core.Handler.HandlerContext;
import pers.ebr.cli.core.Handler.HandlerDesc;
import pers.ebr.cli.core.Message.Receiver;
import pers.ebr.cli.core.types.JobState;
import pers.ebr.cli.util.AppLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static pers.ebr.cli.core.ExternalBatchRunner.getMessageBus;
import static pers.ebr.cli.core.Message.Symbols.*;

/**
 * <pre>
 * 抽象处理父类，真实处理逻辑由子类提供
 * 此抽象类只提供以下功能
 * - 保持处理类状态
 * - 向子类提供发送事件的能力
 *
 * </pre>
 *
 * @author l.gong
 */
public abstract class Broker {
    private static final int INIT_CAP = 16;
    /**
     * <pre>
     * 处理类ID定义
     * </pre>
     */
    public enum Id {
        /** 代表此程序本身，可以看成是广播地址 */
        APP,
        /** 专用于实际执行命令的服务 */
        EXECUTOR,
        /** 专用于管理任务状态的服务 */
        MANAGEMENT,
    }

    /**
     * <pre>
     * 处理状态枚举
     * </pre>
     */
    public enum Status {
        /** 服务被创建后的状态 */
        CREATED,
        /** 服务初始化完成后的状态 */
        RUNNING,
        /** 服务完成处理过程并释放资源后的状态 */
        FINISHED,
    }

    public abstract static class BaseBroker implements Receiver {
        /** 代理状态 */
        private Status brokerStatus;
        /** 处理器映射 */
        private final Map<String, HandlerChain> actionMap;
        /** 处理上下文 */
        private final HandlerContext context;

        protected BaseBroker() {
            actionMap = new HashMap<>(INIT_CAP);
            context = new HandlerContext();
            brokerStatus = Status.CREATED;
        }

        Status status() {
            return brokerStatus;
        }

        void init() {
            onInit();
            brokerStatus = Status.RUNNING;
        }

        void finish() {
            onFinish();
            brokerStatus = Status.FINISHED;
        }

        /**
         * <pre>
         * 注册一个消息处理器
         * </pre>
         *
         * @param act 注册对象动作
         * @param hndClass 处理类
         */
        @SafeVarargs
        protected final void registerActionHandler(String act, Class<? extends Handler>... hndClass) {
            try {
                HandlerChain chain = actionMap.get(act);
                if (hndClass != null && hndClass.length > 0 && chain == null) {
                    chain = new HandlerChain(act);
                    for (Class<? extends Handler> cls : hndClass) {
                        chain.addHandler(cls);
                    }
                }
                actionMap.put(act, chain);
            } catch (SecurityException e) {
                throw new EbrException(e);
            }
        }

        /**
         * <pre>
         * 注册消息处理器
         * </pre>
         *
         * @param act 注册对象动作
         */
        protected void register(String act) {
            actionMap.put(act, null);
        }

        /**
         * <pre>
         * 注销消息处理器
         * </pre>
         *
         * @param act 注销对象动作
         */
        protected void unregister(String act) {
            actionMap.remove(act);
        }

        /**
         * <pre>
         * 接受消息
         * </pre>
         *
         * @param message 消息对象
         */
        @Override
        public final void receive(Message message) {
            // 检查自己是否应该接受此事件
            if (Id.APP != message.dst && id() != message.dst) {
                return;
            }

            showConsoleMsg(message);

            // 检查自己是否关注此事件
            if (!actionMap.containsKey(message.act)) {
                return;
            }
            // 处理此事件
            try {
                if (actionMap.get(message.act) == null) {
                    onMessage(message);
                } else if (!doChain(message)) {
                    AppLogger.debug(String.format("处理[%s]事件失败...", message.act));
                }
            } catch (Exception ex) {
                AppLogger.dumpError(ex);
                notice(MSG_ACT_SERVICE_SHUTDOWN);
            }
        }

        private void showConsoleMsg(Message message) {
            switch (message.act) {
                case MSG_ACT_JOB_STATE_CHANGED:
                    String url = (String) message.param.getOrDefault(MSG_DATA_JOB_URL, "");
                    JobState state = (JobState) message.param.getOrDefault(MSG_DATA_NEW_JOB_STATE, "");
                    System.err.println(String.format("event:[JOB_STATE_CHANGED], new state:[%s], url:[%s]", state, url));
                    break;
                case MSG_ACT_ALL_JOB_FINISHED:
                    System.err.println(String.format("event:[ALL_JOB_FINISHED]"));
                    break;
                case MSG_ACT_SERVICE_SHUTDOWN:
                    System.err.println(String.format("event:[SERVICE_SHUTDOWN]"));
                    break;
                default: break;
            }
        }

        /**
         * <pre>
         * 消息处理
         * </pre>
         *
         * @param message dispatched message
         */
        private boolean doChain(Message message) {
            HandlerChain chain = actionMap.get(message.act);
            context.reset(message.act, message.param);
            try {
                for (var entry : chain.handlerPool.entrySet()) {
                    context.mergeParam();
                    HandlerDesc desc = entry.getValue();
                    if (!desc.newHandlerInstance().doHandle(context)) {
                        if (context.errorMsg != null) {
                            AppLogger.debug(context.errorMsg);
                            throw new EbrException(context.errorMsg);
                        } else {
                            AppLogger.info(String.format("Action(%s)处理完成后处理链结束", desc.handlerClass.getCanonicalName()));
                        }
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException | InstantiationException e) {
                AppLogger.dumpError(e);
                return false;
            }

            // 通知事件优先级高
            if (context.noticeAction != null) {
                notice(context.noticeAction);
            }

            if (context.nextAction != null) {
                post(context.nextAction, Map.copyOf(context.result));
            }
            return true;
        }

        /**
         * <pre>
         * 发送消息
         * </pre>
         *
         * @param act 发送对象动作
         * @param param 发送的负载数据
         */
        protected void post(String act, Map<String, Object> param) {
            getMessageBus().publish(new Message(act, id(), Id.APP, param));
        }

        /**
         * <pre>
         * 将一个任务提交至worker线程池排队执行
         * </pre>
         *
         * @param task    任务
         * @return Future 执行结果
         */
        protected CompletableFuture<JobState> deployTaskAsync(Supplier<JobState> task) {
            return ExternalBatchRunner.getInstance().deployTaskAsync(task);
        }

        /**
         * <pre>
         * 发送消息
         * </pre>
         *
         * @param act 发送对象动作
         * @param param 发送的负载数据
         */
        protected void send(String act, Id dst, Map<String, Object> param) {
            getMessageBus().publish(new Message(act, id(), dst, param));
        }

        /**
         * <pre>
         * 发送程序结束的消息广播
         * </pre>
         *
         * @param act 发送对象动作
         */
        void notice(String act) {
            getMessageBus().publish(new Message(act, id(), Id.APP));
        }

        /**
         * <pre>
         * 返回服务的识别ＩＤ
         * (子类实现)处理类ID
         * </pre>
         * @return  ServiceId
         */
        protected abstract Id id();

        /**
         * <pre>
         * (子类实现)服务初始化
         * </pre>
         */
        protected abstract void onInit();

        /**
         * <pre>
         * (子类实现)服务结束
         * </pre>
         */
        protected abstract void onFinish();

        /**
         * <pre>
         * (子类实现)处理事件
         * </pre>
         * @param message 事件
         */
        protected void onMessage(Message message) {
        }
    }
}
