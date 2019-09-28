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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <pre>
 * 业务处理器
 * - 处理器的作用是当某一个内部事件触发时，由注册此事件的处理器来进行具体处理
 * - 同一事件可以有多个处理器按照注册时的生命顺序进行链式处理
 * </pre>
 * @author catforward
 */
public abstract class Handler {

    public interface IHandler {
        /**
         * <pre>
         * 进行实际处理
         * 当返回false时表示处理链将被终止
         * </pre>
         * @param context 上下文
         * @return true: succeeded false: failed
         */
        boolean doHandle(HandlerContext context);
    }

    /**
     * <pre>
     * 处理器的描述
     * 用于缓存处理其class对象
     * </pre>
     */
    public static class HandlerDesc {
        final Class<? extends IHandler> handlerClass;

        HandlerDesc(Class<? extends IHandler> clazz) throws SecurityException {
            handlerClass = clazz;
        }

        IHandler newHandlerInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return handlerClass.getDeclaredConstructor().newInstance();
        }

    }

    /**
     * <pre>
     * 处理上下文
     * 用来保存每一个事件的参数及发送源，发送目的信息
     * </pre>
     */
    public static class HandlerContext {
        private final static Logger logger = Logger.getLogger(HandlerContext.class.getCanonicalName());
        /** 事件所携带的参数 */
        final HashMap<String, Object> param = new HashMap<>();
        /** 处理器执行后的处理结果 */
        final HashMap<String, Object> result = new HashMap<>();
        String errorMsg = null;
        String nextAction = null;
        String currentAction = null;
        String noticeAction = null;

        /**
         * <pre>
         * 重置所有数据至初始状态
         * </pre>
         * @param newParam 新的事件所携带的参数
         */
        void reset(String action, Map<String, Object> newParam) {
            currentAction = action;
            errorMsg = null;
            nextAction = null;
            noticeAction = null;
            result.clear();
            param.clear();
            param.putAll(newParam);
        }

        /**
         * <pre>
         * 当有处理链中有多个处理器时
         * 每个处理器执行前
         * 合并事件携带的参数
         * 达到上一个处理器的结果能够作为下一个处理器的参数
         * </pre>
         */
        void mergeParam() {
            param.putAll(result);
        }

        public Object getParam(String name) {
            return param.get(name);
        }

        public String getCurrentAction() {
            return currentAction;
        }

        public void setNoticeAction(String newAction) {
            noticeAction = newAction;
        }

        public void setNextAction(String newAction) {
            if (nextAction != null) {
                logger.warning(String.format("nextAction将被替换(%s)<-(%s)", nextAction, newAction));
            }
            nextAction = newAction;
        }

        public void addHandlerResult(String newKey, Object newObj) {
            if (result.containsKey(newKey)) {
                logger.warning(String.format("handlerResult(%s)将被替换", newKey));
            }
            result.put(newKey, newObj);
        }

        public HandlerContext setErrorMessage(String msg) {
            if (errorMsg != null) {
                logger.warning(String.format("errorMsg将被替换(%s)<-(%s)", errorMsg, msg));
            }
            errorMsg = msg;
            return this;
        }
    }

    /**
     * <pre>
     * 处理链
     * </pre>
     */
    static class HandlerChain {
        /**  */
        final Map<Class<? extends IHandler>, HandlerDesc> handlerPool;
        final String action;

        HandlerChain(String newAct) {
            action = newAct;
            handlerPool = new LinkedHashMap<>();
        }

         void addHandler(Class<? extends IHandler> clazz) throws SecurityException {
            handlerPool.put(clazz, new HandlerDesc(clazz));
        }
    }
}
