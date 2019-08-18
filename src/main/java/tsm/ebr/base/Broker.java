/**
 * MIT License
 * <p>
 * Copyright (c) 2019 catforward
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tsm.ebr.base;

import tsm.ebr.base.Handler.HandlerChain;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.HandlerDesc;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Message.Receiver;
import tsm.ebr.util.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

<<<<<<< .mine
import static tsm.ebr.base.Application.getMessageBus;
import static tsm.ebr.base.Broker.Status.*;
=======
import static tsm.ebr.base.Application.getMessageBus;

>>>>>>> .theirs
import static tsm.ebr.base.Message.Symbols.MSG_ACT_SERVICE_SHUTDOWN;

/**
 * <pre>
 * 抽象处理父类，真实处理逻辑由子类提供
 * 此抽象类只提供以下功能
 * - 保持处理类状态
 * - 向子类提供发送事件的能力
 *
 * </pre>
 *
 * @author catforward
 */
public class Broker {

    /**
     * <pre>
     * 处理类ID定义
     * </pre>
     */
    public enum Id {
        /** 代表此程序本身，可以看成是广播地址 */
        APP,
        /** 专用于持久化任务定义的服务 */
        STORAGE,
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

    public static abstract class BaseBroker implements Receiver {
        private final Logger logger = Logger.getLogger(Broker.class.getCanonicalName());
        /** 代理状态 */
        protected volatile Status brokerStatus;
        /** 处理器映射 */
        private final Map<String, HandlerChain> actionMap;
        /** 处理上下文 */
        private final HandlerContext context;

        protected BaseBroker() {
            actionMap = new HashMap<>(Const.INIT_CAP);
            context = new HandlerContext();
            brokerStatus = CREATED;
        }

        public Status status() {
            return brokerStatus;
        }

        public void init() {
            onInit();
            brokerStatus = RUNNING;
        }

        public void finish() {
            onFinish();
            brokerStatus = FINISHED;
        }

        /**
         * <pre>
         * 注册一个消息处理器
         * </pre>
         *
         * @param act
         * @param hndClass
         */
        protected BaseBroker registerActionHandler(String act, Class<? extends IHandler>... hndClass) {
            try {
                HandlerChain chain = actionMap.get(act);
                if (hndClass != null && hndClass.length > 0 && chain == null) {
                    chain = new HandlerChain(act);
                    for (Class<? extends IHandler> cls : hndClass) {
                        chain.addHandler(cls);
                    }
                }
                actionMap.put(act, chain);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        /**
         * <pre>
         * 注册消息处理器
         * </pre>
         *
         * @param act
         */
        protected BaseBroker register(String act) {
            actionMap.put(act, null);
            return this;
        }

        /**
         * <pre>
         * 注销消息处理器
         * </pre>
         *
         * @param act
         */
        protected BaseBroker unregister(String act) {
            if (actionMap.containsKey(act)) {
                actionMap.remove(act);
            }
            return this;
        }

        /**
         * <pre>
         * 接受消息
         * </pre>
         *
         * @param message
         */
        @Override
        public final void receive(Message message) {
            logger.fine("Receive Event Start");
            // 检查自己是否应该接受此事件
            if (Id.APP != message.dst && id() != message.dst) {
                return;
            }
            // 检查自己是否关注此事件
            if (!actionMap.containsKey(message.act)) {
                return;
            }
            // 处理此事件
            try {
                if (actionMap.get(message.act) == null) {
                    onMessage(message);
                } else if (!doHandle(message)) {
                    logger.warning(String.format("处理[%s]事件失败...", message.act));
                }
            } catch (Exception ex) {
                LogUtils.dumpError(ex);
                notice(MSG_ACT_SERVICE_SHUTDOWN);
            }
            logger.fine("Receive Event End");
        }

        /**
         * <pre>
         * 消息处理
         * </pre>
         *
         * @param message
         */
        private boolean doHandle(Message message) {
            HandlerChain chain = actionMap.get(message.act);
            logger.fine("Handler Proc Start");
            context.reset(message.act, message.param);
            try {
                for (Map.Entry<Class<? extends IHandler>, HandlerDesc> entry : chain.handlerPool.entrySet()) {
                    context.mergeParam();
                    HandlerDesc desc = entry.getValue();
                    if (!desc.newHandlerInstance().doHandle(context)) {
                        if (context.errorMsg != null) {
                            logger.warning(context.errorMsg);
                            throw new RuntimeException(context.errorMsg);
                        } else if (context.nextAction == null) {
                            String msg = String.format("[%s]: unknown error occurred...", desc.handlerClass.getCanonicalName());
                            logger.warning(msg);
                            throw new RuntimeException(msg);
                        } else {
                            logger.info(String.format("Action(%s)处理完成后处理链结束", desc.handlerClass.getCanonicalName()));
                            break;
                        }
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }

            if (context.noticeAction != null) {
                notice(context.noticeAction);

            } else if (context.nextAction == null && context.result.isEmpty()) {
                throw new RuntimeException(String.format("Action(%s)'的处理结果不能为空", chain.action));
            } else {
                post(context.nextAction, Map.copyOf(context.result));
            }
            logger.fine("Handler Proc End");
            return true;
        }

        /**
         * <pre>
         * 发送消息
         * </pre>
         *
         * @param act
         * @param param
         */
        protected void post(String act, Map<String, Object> param) {
            getMessageBus().post(new Message(act, id(), Id.APP, param));
        }

        /**
         * <pre>
         * 发送消息
         * </pre>
         *
         * @param act
         * @param param
         */
        protected void send(String act, Id dst, Map<String, Object> param) {
            getMessageBus().post(new Message(act, id(), dst, param));
        }

        /**
         * <pre>
         * 发送程序结束的消息广播
         * </pre>
         *
         * @param act
         */
        protected void notice(String act) {
            getMessageBus().post(new Message(act, id(), Id.APP));
        }

        /**
         * <pre>
         * 返回服务的识别ＩＤ
         * (子类实现)处理类ID
         * </pre>
         * @return  ServiceId
         */
        public abstract Id id();

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
         * @@param event 事件
         */
        protected void onMessage(Message message) {
        }
    }
}
