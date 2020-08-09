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
package pers.ebr.server.repository;

/**
 * <p>
 * SQLite实现的仓储服务中使用的常量
 * </p>
 *
 * @author l.gong
 */
interface SqliteRepositoryConst {
    String VAL_NONE = "none";

    String COL_COUNT = "cnt";
    String COL_WORKFLOW_ID = "workflow_id";
    String COL_WORKFLOW_DEFINE = "workflow_define";

    String SQL_VIEW_EXISTS = "VIEW_EXISTS";
    String SQL_CREATE_VIEW = "CREATE_VIEW";
    String SQL_DROP_VIEW = "DROP_VIEW";
    String SQL_TABLE_EXISTS = "TABLE_EXISTS";
    String SQL_DROP_TABLE = "DROP_TABLE";
    String SQL_RENAME_TABLE = "RENAME_TABLE";
    String SQL_GET_TABLE_NAME = "GET_TABLE_NAME";

    String SQL_WORKFLOW_EXISTS = "WORKFLOW_EXISTS";
    String SQL_SAVE_WORKFLOW = "SAVE_WORKFLOW";
    String SQL_LOAD_WORKFLOW = "LOAD_WORKFLOW";
    String SQL_LOAD_ALL_WORKFLOW_DEFINE = "LOAD_ALL_WORKFLOW_DEFINE";
    String SQL_DEL_WORKFLOW = "DEL_WORKFLOW";

    String SQL_SAVE_TASK_DETAIL = "SAVE_TASK_DETAIL";
    String SQL_LOAD_ALL_TASK_DETAIL = "LOAD_ALL_TASK_DETAIL";
    String SQL_DEL_TASK_DETAIL = "DEL_TASK_DETAIL";

    String SQL_EXEC_HIST_HIST_EXISTS = "TASK_EXEC_HIST_EXISTS";
    String SQL_SAVE_EXEC_HIST_HIST = "SAVE_TASK_EXEC_HIST";
    String SQL_UPDATE_EXEC_HIST_HIST_STATE = "UPDATE_TASK_EXEC_HIST_STATE";
    String SQL_UPDATE_EXEC_HIST_HIST_RESULT = "UPDATE_TASK_EXEC_HIST_RESULT";
    String SQL_DEL_EXEC_HIST_HIST = "DEL_TASK_EXEC_HIST";
}
