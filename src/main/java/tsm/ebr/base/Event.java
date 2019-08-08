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

import tsm.ebr.base.Service.ServiceId;

import java.util.Map;

/**
 * <pre>
 * 消息体定义
 * </pre>
 *
 * @author catforward
 */
public final class Event {

    /**
     * <pre>
     * 事件接受者
     * </pre>
     */
    interface EventReceiver {
        /**
         * 接受事件并处理此事件
         * @param event 接受的事件对象
         */
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
        public final static String EVT_ACT_LAUNCH_TASK_FLOWS = "EVT_ACT_LAUNCH_TASK_FLOWS";
        public final static String EVT_ACT_LAUNCH_TASK_UNIT = "EVT_ACT_LAUNCH_TASK_UNIT";
        public final static String EVT_ACT_LAUNCH_TASK_UNITS = "EVT_ACT_LAUNCH_TASK_UNITS";
        public final static String EVT_ACT_TASK_UNIT_STATE_CHANGED = "EVT_ACT_TASK_UNIT_STATE_CHANGED";
        public final static String EVT_ACT_ALL_TASK_FINISHED = "EVT_ACT_ALL_TASK_FINISHED";
        /** 服务事件中传送参数的预定义名称 */
        public final static String EVT_DATA_META_MAP = "EVT_DATA_META_POOL";
        public final static String EVT_DATA_TASK_FLOW_URL = "EVT_DATA_TASK_FLOW_URL";
        public final static String EVT_DATA_TASK_FLOW_URL_LIST = "EVT_DATA_TASK_FLOW_URL_LIST";
        public final static String EVT_DATA_TASK_UNIT_URL = "EVT_DATA_TASK_UNIT_URL";
        public final static String EVT_DATA_TASK_UNIT_COMMAND = "EVT_DATA_TASK_UNIT_COMMAND";
        public final static String EVT_DATA_TASK_PERFORMABLE_UNITS_LIST = "EVT_DATA_TASK_PERFORMABLE_UNITS_LIST";
        public final static String EVT_DATA_TASK_UNIT_NEW_STATE = "EVT_DATA_TASK_UNIT_NEW_STATE";

    }

    /** The Action of event */
    public final String act;
    public final ServiceId src;
    public final ServiceId dst;
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

    @Override
    public String toString() {
        return String.format("[%s -> %s]::[%s]", src, dst, act);
    }
}