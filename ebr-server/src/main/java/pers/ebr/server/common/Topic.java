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

    /**
     * 获取服务器信息
     * 请求：{req: "req.GetServerInfo", param: {空参数}}
     * 正常响应：{req: "req.GetServerInfo", result: {config:{key-value数据}, env:{key-value数据}}}
     */
    String REQ_GET_SERVER_INFO = "req.GetServerInfo";

    /**
     * 服务器端验证workflow的定义合法性
     * 请求：{req: "req.ValidateWorkflow", param: {workflow的json定义体}}
     * 正常响应：{req: "req.ValidateWorkflow", result: {空数据 }}
     * 异常响应：{req: "req.ValidateWorkflow", error: {空数据 }}
     */
    String REQ_VALIDATE_WORKFLOW = "req.ValidateWorkflow";

    /**
     * 保存workflow定义
     * 请求：{req: "req.SaveWorkFlow", param: {workflow的json定义体}}
     * 正常响应：{req: "req.SaveWorkFlow", result: {空数据 }}
     * 异常响应：{req: "req.SaveWorkFlow", error: {空数据 }}
     */
    String REQ_SAVE_WORKFLOW = "req.SaveWorkflow";

    /**
     * 获取所有workflow的定义以及状态
     * 请求：{req: "req.AllWorkflow", param: {空参数}}
     * 正常响应：{req: "req.AllWorkflow", result: {["workflow_id": value, "instance_id": value, "tasks": [task detail array]]}}
     * 异常响应：{req: "req.AllWorkflow", error: {空数据 }}
     */
    String REQ_ALL_WORKFLOW = "req.AllWorkflow";

    /**
     * 获取workflow运行状态概要
     * 请求：{req: "req.ExecStatistics", param: {空参数}}
     * 正常响应：{req: "req.ExecStatistics", result: {"complete":int,"failed":int,"active":int}}
     * 异常响应：{req: "req.ExecStatistics", error: {"info":string}}
     */
    String REQ_EXEC_STATISTICS = "req.ExecStatistics";

    /**
     * 启动指定ID的workflow
     * 请求：{req: "req.RunWorkflow", param: {"workflow_id":string}}
     * 正常响应：{req: "req.RunWorkflow", result: {"info":string}}
     * 异常响应：{req: "req.RunWorkflow", error: {"info":string}}
     */
    String REQ_RUN_WORKFLOW = "req.RunWorkflow";

    /**
     * 消息：通知运行Flow
     * 请求：{req: "msg.schd.RunFlow", param: {"workflow_id":string,"workflow_def":string}}
     * 无响应
     */
    String MSG_RUN_FLOW = "msg.schd.RunFlow";

    /**
     * 消息：通知指定ID的task的新状态
     * 请求：{req: "msg.schd.TaskStateChanged", param: {"task_url":string,"instance_id":string,"task_state":string}}
     * 无响应
     */
    String MSG_TASK_STATE_CHANGED = "msg.schd.TaskStateChanged";

    /**
     * 消息：通知Flow结束
     * 请求：{req: "msg.schd.FlowFinished", param: {"instance_id":string}}
     * 无响应
     */
    String MSG_WORKFLOW_FINISHED = "msg.schd.WorkflowFinished";

    /**
     * 消息：获取调度器的执行统计
     * 请求：{req: "msg.schd.GetSchdSummary", param: {空参数}}
     * 正常响应：{req: "req.GetSchdSummary", result: {schd_name:{key:value}}}
     * 异常响应：{req: "req.GetSchdSummary", error: {空数据 }}
     */
    String MSG_EXEC_STATISTICS = "msg.schd.GetExecStatistics";
    
}
