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
 * Define the constant variables which can be used in this whole application.
 * </pre>
 * 
 * @author l.gong
 */
public interface Global {

    String ENV_EBR_ROOT = "EBR_ROOT";

    String REQUEST_PARAM_PATH = "path";
    String REQUEST_PARAM_PARAM = "param";
    String RESPONSE_ERROR = "error";
    String RESPONSE_RESULT = "result";

    String TASK_ID = "id";
    String TASK_GROUP = "group";
    String TASK_DESC = "desc";
    String TASK_CMD_LINE = "cmd";
    String TASK_DEPENDS_LIST = "depends";

}