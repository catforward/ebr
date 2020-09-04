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
package pers.ebr.server.application;

/**
 * <p>
 * 消息主题
 * <ul>
 * <li>MSG* 通过消息总线，应用内Verticle间的消息</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
public interface AppTopic {

    /**
     * <p>
     * 消息：获取调度器的执行统计
     * <ul>
     * <li>消息体：{空参数}</li>
     * <li>响应：{schd_name:{key:value}}}</li>
     * </ul>
     * </p>
     */
    String MSG_EXEC_STATISTICS = "msg.schd.GetExecStatistics";

    /**
     * <p>
     * 消息：通知运行Flow
     * <ul>
     * <li>消息体：{taskflow_id: string}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_LAUNCH_FLOW = "msg.schd.LaunchFlow";

    /**
     * <p>
     * 消息：通知指定ID的task的新状态
     * <ul>
     * <li>消息体：{instance_id: string, task_path: string, task_state: int}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_TASK_STATE_CHANGED = "msg.schd.TaskStateChanged";

    /**
     * <p>
     * 消息：通知Flow结束
     * <ul>
     * <li>消息体：{req: "msg.schd.FlowFinished", param: {"instance_id":string}}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_FLOW_FINISHED = "msg.schd.FlowFinished";


    /**
     * <p>
     * 消息：执行完成
     * <ul>
     * <li>消息体：{instance_id: string, task_path: string, task_state: int}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_EXEC_RESULT = "msg.exec.ExecuteResult";

}
