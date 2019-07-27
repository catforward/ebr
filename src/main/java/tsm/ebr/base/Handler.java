/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.base;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Handler {

    public interface IHandler {
        /**
         * @param context 上下文
         * @return true: succeeded false: failed
         */
        boolean doHandle(HandlerContext context);
    }

    public static class HandlerDesc {
        final Class<? extends IHandler> handlerClass;

        HandlerDesc(Class<? extends IHandler> clazz) throws SecurityException {
            handlerClass = clazz;
        }

        IHandler newHandlerInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return handlerClass.getDeclaredConstructor().newInstance();
        }

    }

    public static class HandlerContext {
        private final static Logger logger = Logger.getLogger(HandlerContext.class.getCanonicalName());
        final HashMap<String, Object> param = new HashMap<>();
        final HashMap<String, Object> result = new HashMap<>();
        String errorMsg = null;
        String nextAction = null;
        String currentAction = null;
        String noticeAction = null;

        HandlerContext() {}

        void reset(String action, Map<String, Object> newParam) {
            currentAction = action;
            errorMsg = null;
            nextAction = null;
            noticeAction = null;
            result.clear();
            param.clear();
            param.putAll(newParam);
        }

        void mergeParam() {
            param.putAll(result);
        }

        public Object getParam(String key) {
            return param.get(key);
        }

        public Object getParam(String key, Object defaultValue) {
            return param.getOrDefault(key, defaultValue);
        }

        public String getCurrentAction() {
            return currentAction;
        }

        public HandlerContext setNoticeAction(String newAction) {
            noticeAction = newAction;
            return this;
        }

        public HandlerContext setNextAction(String newAction) {
            if (nextAction != null) {
                logger.warning(String.format("nextAction将被替换(%s)<-(%s)", nextAction, newAction));
            }
            nextAction = newAction;
            return this;
        }

        public HandlerContext addHandlerResult(String newKey, Object newObj) {
            if (result.containsKey(newKey)) {
                logger.warning(String.format("handlerResult(%s)将被替换", newKey));
            }
            result.put(newKey, newObj);
            return this;
        }

        public HandlerContext setErrorMessage(String msg) {
            if (errorMsg != null) {
                logger.warning(String.format("errorMsg将被替换(%s)<-(%s)", errorMsg, msg));
            }
            errorMsg = msg;
            return this;
        }
    }

    static class HandlerChain {
        final String eventAction;
        final Map<Class<? extends IHandler>, HandlerDesc> handlerPool;

        HandlerChain(String newAct) {
            eventAction = newAct;
            handlerPool = new LinkedHashMap<>();
        }

        HandlerChain addHandler(Class<? extends IHandler> clazz) throws SecurityException {
            HandlerDesc desc = new HandlerDesc(clazz);
            if (!handlerPool.containsKey(clazz)) {
                handlerPool.put(clazz, desc);
            }
            return this;
        }
    }
}
