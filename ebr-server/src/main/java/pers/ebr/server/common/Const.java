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
 * 定义应用内使用的常量
 * </p>
 * 
 * @author l.gong
 */
public interface Const {

    String ENV_EBR_ROOT = "EBR_ROOT";

    /** Http请求响应参数 */
    String REQUEST_PARAM_REQ = "req";
    String REQUEST_PARAM_PARAM = "param";
    String RESPONSE_ERROR = "error";
    String RESPONSE_RESULT = "result";
    String RESPONSE_INFO = "info";

    /** 内部消息参数 */
    String MSG_PARAM_WORKFLOW_ID = "workflow_id";
    String MSG_PARAM_INSTANCE_ID = "instance_id";
    String MSG_PARAM_TASK_PATH = "task_path";
    String MSG_PARAM_TASK_STATE = "task_state";
    String MSG_PARAM_TASKS = "tasks";

}