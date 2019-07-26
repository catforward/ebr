package tsm.ebr.base;

import tsm.ebr.base.Service.ServiceId;

import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 * 消息体定义
 * </pre>
 * @author catforward
 */
public final class Event {

    interface EventReceiver {
        void receive(Event event);
    }

    /**
     * <pre>
     * 预定义名称
     * </pre>
     */
    public static class Symbols {

        /** 服务事件中传送动作的预定义名称 */
        public final static String EVT_ACT_SERVICE_INIT = "EVT_ACT_SERVICE_INIT";
        public final static String EVT_ACT_SERVICE_SHUTDOWN = "EVT_ACT_SERVICE_SHUTDOWN";
        public final static String EVT_ACT_LOAD_DEF_FILE = "EVT_ACT_LOAD_DEF_FILE";
        public final static String EVT_ACT_TASK_META_CREATED = "EVT_ACT_TASK_META_CREATED";
        public final static String EVT_ACT_LAUNCH_TASK_FLOW = "EVT_ACT_LAUNCH_TASK_FLOW";
        public final static String EVT_ACT_LAUNCH_TASK_UNIT = "EVT_ACT_LAUNCH_TASK_UNIT";
        public final static String EVT_ACT_LAUNCH_TASK_UNITS = "EVT_ACT_LAUNCH_TASK_UNITS";
        public final static String EVT_ACT_TASK_UNIT_STATE_CHANGED = "EVT_ACT_TASK_UNIT_STATE_CHANGED";
        /** 服务事件中传送参数的预定义名称 */
        public final static String EVT_DATA_PATH = "EVT_DATA_PATH";
        public final static String EVT_DATA_TASK_FLOW_URL = "EVT_DATA_TASK_FLOW_URL";
        public final static String EVT_DATA_TASK_UNIT_URL = "EVT_DATA_TASK_UNIT_URL";
        public final static String EVT_DATA_TASK_UNIT_COMMAND = "EVT_DATA_TASK_UNIT_COMMAND";
        public final static String EVT_DATA_TASK_PERFORMABLE_UNITS_LIST = "EVT_DATA_TASK_PERFORMABLE_UNITS_LIST";
        public final static String EVT_DATA_TASK_UNIT_NEW_STATE = "EVT_DATA_TASK_UNIT_NEW_STATE";

    }

    public final String act;
    public final ServiceId src;
    public final ServiceId dst;
    // unmodifiable
    public final Map<String, Object> param;

    public Event(String newAct,
                 ServiceId newSrc,
                 ServiceId newDst) {
        this(newAct, newSrc, newDst, Map.of());
    }

    public Event(String newAct,
                 ServiceId newSrc,
                 ServiceId newDst,
                 Map<String, Object> newParam) {
        act = newAct;
        src = newSrc;
        dst = newDst;
        param = Map.copyOf(newParam);
    }

    /**
     * 获得指定名字的传送参数
     *
     * @param name 参数名
     * @return Object 参数值
     */
    public Optional<Object> getParam(String name) {
        return Optional.ofNullable(param.get(name));
    }

    /**
     * 判断消息体中是否包含指定名称的参数
     *
     * @param name 参数名
     * @return boolean true：存在 false：不存在
     */
    public boolean hasParam(String name) {
        return param.containsKey(name);
    }

    @Override
    public String toString() {
        return String.format("[%s -> %s]::[%s]", src, dst, act);
    }
}