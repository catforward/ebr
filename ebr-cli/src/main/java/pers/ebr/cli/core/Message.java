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

import pers.ebr.cli.core.Broker.Id;

import java.util.Map;

/**
 * <pre>
 * 消息体定义
 * </pre>
 *
 * @author l.gong
 */
public final class Message {

    /**
     * <pre>
     * 事件接受者
     * </pre>
     */
    interface Receiver {
        /**
         * 接受事件并处理此事件
         * @param topic 主题
         * @param message 接受的事件数据
         */
        void receive(String topic, Map<String, Object> message);
    }

    /** 服务事件中传送动作的预定义名称 */
    public static final String MSG_ACT_SERVICE_SHUTDOWN = "MSG_ACT_SERVICE_SHUTDOWN";
    public static final String MSG_ACT_LAUNCH_JOB_FLOW = "MSG_ACT_LAUNCH_JOB_FLOW";
    public static final String MSG_ACT_LAUNCH_JOB_ITEM = "MSG_ACT_LAUNCH_JOB_ITEM";
    public static final String MSG_ACT_JOB_STATE_CHANGED = "MSG_ACT_JOB_STATE_CHANGED";
    public static final String MSG_ACT_ALL_JOB_FINISHED = "MSG_ACT_ALL_TASK_FINISHED";
    /** 服务事件中传送参数的预定义名称 */
    public static final String MSG_DATA_JOB_FLOW_URL = "MSG_DATA_JOB_FLOW_URL";
    public static final String MSG_DATA_JOB_URL = "MSG_DATA_JOB_URL";
    public static final String MSG_DATA_PERFORMABLE_JOB_ITEM_LIST = "MSG_DATA_PERFORMABLE_JOB_LIST";
    public static final String MSG_DATA_NEW_JOB_STATE = "MSG_DATA_NEW_JOB_STATE";

    /** 数据KEY名 */
    public static final String MSG_FROM_BROKER_ID = "BROKER_ID_FROM";
    public static final String MSG_TO_BROKER_ID = "BROKER_ID_TO";

}
