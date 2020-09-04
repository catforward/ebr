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
package pers.ebr.server.facade;

/**
 * <p>
 * 消息主题
 * <ul>
 * <li>REQ* 通过HTTP服务与客户端之间的消息</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
public interface FacadeTopic {

    /**
     * <p>
     * 获取服务器信息
     * <ul>
         * <li>请求：{msg: "api.GetServerInfo", data: {空参数}}</li>
     * <li>正常响应：{msg: "api.GetServerInfo", code: 10000, ret: true, data: {config:{key-value数据}, env:{key-value数据}}}</li>
     * </ul>
     * </p>
     */
    String API_GET_SERVER_INFO = "api.GetServerInfo";

    /**
     * <p>
     * 获取workflow运行状态概要
     * <ul>
     * <li>请求：{msg: "api.ExecStatistics", data: {空参数}}</li>
     * <li>正常响应：{msg: "api.ExecStatistics", code: 10000, ret: true, data: {"complete":int,"failed":int,"active":int}}</li>
     * <li>异常响应：{msg: "api.ExecStatistics", code: 10001, ret: false, data: {"error":string}}</li>
     * </ul>
     * </p>
     */
    String API_EXEC_STATISTICS = "api.ExecStatistics";

    /**
     * <p>
     * 服务器端验证taskflow的定义合法性
     * <ul>
     * <li>请求：{msg: "api.ValidateFlow", data: { flow_define: {taskflow的json定义体}}}</li>
     * <li>正常响应：{msg: "api.ValidateFlow", code: 10000, ret: true, data: {空数据 }}</li>
     * <li>异常响应：{msg: "api.ValidateFlow", code: 10001, ret: false, data: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String API_VALIDATE_FLOW = "api.ValidateFlow";

    /**
     * <p>
     * 保存taskflow定义
     * <ul>
     * <li>请求：{msg: "api.SaveFlow", data: {flow_define: {taskflow的json定义体}}}</li>
     * <li>正常响应：{msg: "api.SaveFlow", code: 10000, ret: true, data: {空数据 }}</li>
     * <li>异常响应：{msg: "api.SaveFlow", code: 10001, ret: false, data: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String API_SAVE_FLOW = "api.SaveFlow";

    /**
     * <p>
     * 删除指定ID的taskflow
     * <ul>
     * <li>请求：{msg: "api.DeleteFlow", data: {"taskflow_id":string}}</li>
     * <li>正常响应：{msg: "api.DeleteFlow", code: 10000, ret: true, data: {"info":string}}</li>
     * <li>异常响应：{msg: "api.DeleteFlow", code: 10001, ret: false, data: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String API_DELETE_FLOW = "api.DeleteFlow";

    /**
     * <p>
     * 下载指定ID的taskflow定义
     * <ul>
     * <li>请求：{msg: "api.DumpFlowDef", data: {"taskflow_id":string}}</li>
     * <li>正常响应：{msg: "api.DumpFlowDef", code: 10000, ret: true, data: data: {"taskflow_id":string}}</li>
     * <li>异常响应：{msg: "api.DumpFlowDef", code: 10001, ret: false, data: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String API_DUMP_FLOW_DEF = "api.DumpFlowDef";

    /**
     * <p>
     * 获取所有taskflow的定义以及状态
     * <ul>
     * <li>请求：{msg: "api.QueryAllFlow", data: {空参数}}</li>
     * <li>正常响应：{msg: "api.QueryAllFlow", code: 10000, ret: true, data: {flow_array : {["taskflow_id": value, "instance_id": value, "tasks": [task detail array]]}}}</li>
     * <li>异常响应：{msg: "api.QueryAllFlow", code: 10001, ret: false, data: {空数据 }}</li>
     * </ul>
     * </p>
     */
    String API_QUERY_ALL_FLOW = "api.QueryAllFlow";

    /**
     * <p>
     * 启动指定ID的taskflow
     * <ul>
     * <li>请求：{msg: "api.LaunchFlow", data: {"taskflow_id":string}}</li>
     * <li>正常响应：{msg: "api.LaunchFlow", code: 10000, ret: true, data: {"info":string}}</li>
     * <li>异常响应：{msg: "api.LaunchFlow", code: 10001, ret: false, data: {"info":string}}</li>
     * </ul>
     * </p>
     */
    String API_LAUNCH_FLOW = "api.LaunchFlow";
}
