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

import tsm.ebr.base.Event.EventReceiver;
import tsm.ebr.base.Handler.HandlerChain;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.HandlerDesc;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static tsm.ebr.base.Application.getEventBus;
import static tsm.ebr.base.Event.Symbols.EVT_ACT_SERVICE_SHUTDOWN;
import static tsm.ebr.base.Service.ServiceStatus.*;

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
public abstract class Service implements EventReceiver {

    /**
     * 处理类ID定义
     */
    public enum ServiceId {
        APP,
        TASK_META_PERSISTENCE,
        TASK_EXECUTOR,
        TASK_STATE_MANAGENT;
    }

    /**
     * <pre>
     * 处理状态枚举
     * </pre>
     */
    public enum ServiceStatus {
        CREATED(0),
        INITIALIZED(1),
        FINISHED(2);

        private int code;

        /**
         * 枚举构造函数
         *
         * @param newCode 枚举值
         */
        ServiceStatus(int newCode) {
            code = newCode;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", name(), code);
        }
    }

    private final Logger logger = Logger.getLogger(Service.class.getCanonicalName());
    // 服务状态
    protected ServiceStatus serviceStatus;
    // key: event_act value: handler_chain
    private final Map<String, HandlerChain> actionMap;
    private final HandlerContext context;

    protected Service() {
        actionMap = new HashMap<>();
        context = new HandlerContext();
        serviceStatus = CREATED;
    }

    /**
     * 服务状态
     *
     * @return ServiceStatus
     */
    public ServiceStatus status() {
        return serviceStatus;
    }

    /**
     * 服务初始化
     */
    public void init() {
        onInit();
        serviceStatus = INITIALIZED;
    }

    /**
     * 服务结束
     */
    public void finish() {
        onFinish();
        serviceStatus = FINISHED;
    }


    protected Service registerActionHandler(String act, Class<? extends IHandler>... hndClass) {
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

    protected Service register(String act) {
        actionMap.put(act, null);
        return this;
    }

    protected Service unregister(String act) {
        if (actionMap.containsKey(act)) {
            actionMap.remove(act);
        }
        return this;
    }

    @Override
    public final void receive(Event event) {
        logger.fine("Receive Event Start");
        // 检查自己是否应该接受此事件
        if (ServiceId.APP != event.dst && id() != event.dst) {
            return;
        }
        // 检查自己是否关注此事件
        if (!actionMap.containsKey(event.act)) {
            return;
        }
        // 处理此事件
        try {
            if (actionMap.get(event.act) == null) {
                onEvent(event);
            } else if (!doHandle(event)) {
                logger.warning(String.format("处理[%s]事件失败...", event.act));
            }
        } catch (Exception ex) {
            LogUtils.dumpError(ex);
            notice(EVT_ACT_SERVICE_SHUTDOWN);
        }
        logger.fine("Receive Event End");
    }

    private boolean doHandle(Event event) {
        HandlerChain chain = actionMap.get(event.act);
        logger.fine("Handler Proc Start");
        context.reset(event.act, event.param);
        try {
            for (Map.Entry<Class<? extends IHandler>, HandlerDesc> entry : chain.handlerPool.entrySet()) {
                context.mergeParam();
                HandlerDesc desc = entry.getValue();
                if (!desc.newHandlerInstance().doHandle(context)) {
                    if (context.errorMsg != null) {
                        logger.warning(context.errorMsg);
                        return false;
                    } else if (context.nextAction == null) {
                        logger.warning("unknown error occurred...");
                        return false;
                    } else {
                        logger.info(String.format("Action(%s)处理完成后处理链结束", desc.handlerClass.getName()));
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
            throw new RuntimeException(String.format("Action(%s)'的处理结果不能为空", chain.eventAction));
        } else {
            post(context.nextAction, Map.copyOf(context.result));
        }
        logger.fine("Handler Proc End");
        return true;
    }

    /**
     * 发送消息
     *
     * @param act
     * @param param
     */
    protected void post(String act, Map<String, Object> param) {
        getEventBus().post(new Event(act, id(), ServiceId.APP, param));
    }

    /**
     * 发送消息
     *
     * @param act
     * @param param
     */
    protected void send(String act, ServiceId dst, Map<String, Object> param) {
        getEventBus().post(new Event(act, id(), dst, param));
    }

    /**
     * 发送程序结束的消息广播
     *
     * @param act
     */
    protected void notice(String act) {
        getEventBus().post(new Event(act, id(), ServiceId.APP));
    }

    /**
     * (子类实现)处理类ID
     */
    public abstract ServiceId id();

    /**
     * (子类实现)服务初始化
     */
    protected abstract void onInit();

    /**
     * (子类实现)服务结束
     */
    protected abstract void onFinish();

    /**
     * (子类实现)处理事件
     */
    protected void onEvent(Event event) {}
}
