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
package pers.ebr.server.common;

/**
 * <pre>
 * The Defines of Topic
 * </pre>
 *
 * @author l.gong
 */
public interface Topic {

    /** 获取服务器信息 */
    String REQ_GET_SERVER_INFO = "req.info.GetServerInfo";
    /** 服务器端验证task flow的定义合法性 */
    String REQ_VALIDATE_FLOW = "req.flow.ValidateFlow";
    /** 保存task flow定义 */
    String REQ_SAVE_FLOW = "req.flow.SaveFlow";
    /** 获取所有task flow的定义 */
    String REQ_GET_ALL_FLOW = "req.flow.GetAllFlow";
    /** 获取指定id的task flow定义及运行状态 */
    String REQ_GET_FLOW_STATUS = "req.flow.GetFlowStatus";
    /** 启动指定ID的task */
    String REQ_RUN_FLOW = "req.flow.RunFlow";
    /** 获取指定ID的task的日志信息 */
    String REQ_SHOW_FLOW_LOG = "req.flow.ShowLog";

    /** 消息：通知运行Flow */
    String MSG_RUN_FLOW = "msg.schd.RunFlow";
    /** 消息：通知指定ID的task的新状态 */
    String MSG_TASK_STATE_CHANGED = "msg.schd.TaskStateChanged";
    /** 消息：通知Flow结束 */
    String MSG_FLOW_FINISHED = "msg.schd.FlowFinished";
    
}
