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
        private final static Logger logger = Logger.getLogger(HandlerContext.class.getName());
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
