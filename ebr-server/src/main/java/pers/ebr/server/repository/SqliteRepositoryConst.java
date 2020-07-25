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
 * <pre>
 * The Defines of DataBase
 * </pre>
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

    String SQL_SAVE_WORKFLOW = "SAVE_WORKFLOW";
    String SQL_LOAD_WORKFLOW = "LOAD_WORKFLOW";
    String SQL_LOAD_ALL_WORKFLOW_DEFINE = "LOAD_ALL_WORKFLOW_DEFINE";

    String SQL_SAVE_TASK_DETAIL = "SAVE_TASK_DETAIL";
    String SQL_LOAD_ALL_TASK_DETAIL = "LOAD_ALL_TASK_DETAIL";

    String SQL_WORKFLOW_HIST_EXISTS = "WORKFLOW_HIST_EXISTS";
    String SQL_SAVE_WORKFLOW_HIST = "SAVE_WORKFLOW_HIST";
    String SQL_UPDATE_WORKFLOW_HIST_STATE = "UPDATE_WORKFLOW_HIST_STATE";
    String SQL_UPDATE_WORKFLOW_HIST_RESULT = "UPDATE_WORKFLOW_HIST_RESULT";
}
