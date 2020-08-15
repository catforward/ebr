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
 * <p>
 * 消息主题
 * <ul>
 * <li>REQ* 通过HTTP服务与客户端之间的消息</li>
 * <li>MSG* 通过消息总线，应用内Verticle间的消息</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
public interface Topic {

    /**
     * <p>
     * 获取服务器信息
     * <ul>
     * <li>请求：{req: "req.GetServerInfo", param: {空参数}}</li>
     * <li>正常响应：{req: "req.GetServerInfo", result: {config:{key-value数据}, env:{key-value数据}}}</li>
     * </ul>
     * </p>
     */
    String REQ_GET_SERVER_INFO = "req.GetServerInfo";

    /**
     * <p>
     * 服务器端验证taskflow的定义合法性
     * <ul>
     * <li>请求：{req: "req.ValidateFlow", param: {taskflow的json定义体}}</li>
     * <li>正常响应：{req: "req.ValidateFlow", result: {空数据 }}</li>
     * <li>异常响应：{req: "req.ValidateFlow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_VALIDATE_FLOW = "req.ValidateFlow";

    /**
     * <p>
     * 保存taskflow定义
     * <ul>
     * <li>请求：{req: "req.SaveFlow", param: {taskflow的json定义体}}</li>
     * <li>正常响应：{req: "req.SaveFlow", result: {空数据 }}</li>
     * <li>异常响应：{req: "req.SaveFlow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_SAVE_FLOW = "req.SaveFlow";

    /**
     * <p>
     * 获取所有taskflow的定义以及状态
     * <ul>
     * <li>请求：{req: "req.AllFlow", param: {空参数}}</li>
     * <li>正常响应：{req: "req.AllFlow", result: {["taskflow_id": value, "instance_id": value, "tasks": [task detail array]]}}</li>
     * <li>异常响应：{req: "req.AllFlow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_ALL_FLOW = "req.AllFlow";

    /**
     * <p>
     * 获取workflow运行状态概要
     * <ul>
     * <li>请求：{req: "req.ExecStatistics", param: {空参数}}</li>
     * <li>正常响应：{req: "req.ExecStatistics", result: {"complete":int,"failed":int,"active":int}}</li>
     * <li>异常响应：{req: "req.ExecStatistics", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_EXEC_STATISTICS = "req.ExecStatistics";

    /**
     * <p>
     * 启动指定ID的taskflow
     * <ul>
     * <li>请求：{req: "req.LaunchFlow", param: {"taskflow_id":string}}</li>
     * <li>正常响应：{req: "req.LaunchFlow", result: {"info":string}}</li>
     * <li>异常响应：{req: "req.LaunchFlow", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_LAUNCH_FLOW = "req.LaunchFlow";

    /**
     * <p>
     * 下载指定ID的taskflow定义
     * <ul>
     * <li>请求：{req: "req.DumpFlowDef", param: {"taskflow_id":string}}</li>
     * <li>正常响应：{req: "req.DumpFlowDef", result: {"taskflow_id":string}}</li>
     * <li>异常响应：{req: "req.DumpFlowDef", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_DUMP_FLOW_DEF = "req.DumpFlowDef";

    /**
     * <p>
     * 删除指定ID的taskflow
     * <ul>
     * <li>请求：{req: "req.DeleteFlow", param: {"taskflow_id":string}}</li>
     * <li>正常响应：{req: "req.DeleteFlow", result: {"info":string}}</li>
     * <li>异常响应：{req: "req.DeleteFlow", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_DEL_FLOW = "req.DeleteFlow";

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
     * 消息：执行完成
     * <ul>
     * <li>消息体：{instance_id: string, task_path: string, task_state: int}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_EXEC_RESULT = "msg.exec.ExecuteResult";
    
}
