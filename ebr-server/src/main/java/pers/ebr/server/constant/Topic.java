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
package pers.ebr.server.constant;

/**
 * <pre>
 * The Defines of Topic
 * </pre>
 *
 * @author l.gong
 */
public interface Topic {

    /* 获取服务器信息 */
    String REQ_INFO_GET_SERVER_INFO = "req.info.GetServerInfo";
    /* 服务器端验证taskflow的定义合法性 */
    String REQ_TASK_VALIDATE_TASK_FLOW = "req.task.ValidateTaskFlow";
    /* 保存taskflow定义 */
    String REQ_TASK_SAVE_TASK_FLOW = "req.task.SaveTaskFlow";
    /* 获取所有taskflow的定义 */
    String REQ_TASK_GET_ALL_TASK_FLOW = "req.task.GetAllTaskFlow";
    /* 获取指定id的taskflow定义及运行状态 */
    String REQ_TASK_GET_TASK_FLOW_STATUS = "req.task.GetTaskFlowStatus";
}
