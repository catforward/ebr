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
     * 服务器端验证workflow的定义合法性
     * <ul>
     * <li>请求：{req: "req.ValidateWorkflow", param: {workflow的json定义体}}</li>
     * <li>正常响应：{req: "req.ValidateWorkflow", result: {空数据 }}</li>
     * <li>异常响应：{req: "req.ValidateWorkflow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_VALIDATE_WORKFLOW = "req.ValidateWorkflow";

    /**
     * <p>
     * 保存workflow定义
     * <ul>
     * <li>请求：{req: "req.SaveWorkFlow", param: {workflow的json定义体}}</li>
     * <li>正常响应：{req: "req.SaveWorkFlow", result: {空数据 }}</li>
     * <li>异常响应：{req: "req.SaveWorkFlow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_SAVE_WORKFLOW = "req.SaveWorkflow";

    /**
     * <p>
     * 获取所有workflow的定义以及状态
     * <ul>
     * <li>请求：{req: "req.AllWorkflow", param: {空参数}}</li>
     * <li>正常响应：{req: "req.AllWorkflow", result: {["workflow_id": value, "instance_id": value, "tasks": [task detail array]]}}</li>
     * <li>异常响应：{req: "req.AllWorkflow", error: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String REQ_ALL_WORKFLOW = "req.AllWorkflow";

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
     * 启动指定ID的workflow
     * <ul>
     * <li>请求：{req: "req.RunWorkflow", param: {"workflow_id":string}}</li>
     * <li>正常响应：{req: "req.RunWorkflow", result: {"info":string}}</li>
     * <li>异常响应：{req: "req.RunWorkflow", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_RUN_WORKFLOW = "req.RunWorkflow";

    /**
     * <p>
     * 下载指定ID的workflow定义
     * <ul>
     * <li>请求：{req: "req.DumpWorkflowDef", param: {"workflow_id":string}}</li>
     * <li>正常响应：{req: "req.DumpWorkflowDef", result: {"workflow_id":string}}</li>
     * <li>异常响应：{req: "req.DumpWorkflowDef", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_DUMP_WORKFLOW_DEF = "req.DumpWorkflowDef";

    /**
     * <p>
     * 删除指定ID的workflow
     * <ul>
     * <li>请求：{req: "req.DeleteWorkflow", param: {"workflow_id":string}}</li>
     * <li>正常响应：{req: "req.DeleteWorkflow", result: {"info":string}}</li>
     * <li>异常响应：{req: "req.DeleteWorkflow", error: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String REQ_DEL_WORKFLOW = "req.DeleteWorkflow";

    /**
     * <p>
     * 消息：通知运行Flow
     * <ul>
     * <li>消息体：{workflow_id: string}</li>
     * <li>无响应</li>
     * </ul>
     * </p>
     */
    String MSG_RUN_FLOW = "msg.schd.RunFlow";

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
    String MSG_WORKFLOW_FINISHED = "msg.schd.WorkflowFinished";

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
