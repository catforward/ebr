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
package pers.ebr.server.common.repo;

/**
 * <pre>
 * The Defines of DataBase
 * </pre>
 *
 * @author l.gong
 */
interface SqliteRepositoryConst {
    String NULL_OBJ = "none";

    String VIEW_EXISTS = "VIEW_EXISTS";
    String CREATE_VIEW = "CREATE_VIEW";
    String DROP_VIEW = "DROP_VIEW";
    String TABLE_EXISTS = "TABLE_EXISTS";
    String DROP_TABLE = "DROP_TABLE";
    String RENAME_TABLE = "RENAME_TABLE";
    String GET_TABLE_NAME = "GET_TABLE_NAME";
    String SAVE_FLOW = "SAVE_FLOW";
    String LOAD_FLOW = "LOAD_FLOW";
    String LOAD_ALL_FLOW = "LOAD_ALL_FLOW";
    String SAVE_TASK = "SAVE_TASK";

    String FLOW_HIST_EXISTS = "FLOW_HIST_EXISTS";
    String SAVE_FLOW_HIST = "SAVE_FLOW_HIST";
    String UPDATE_FLOW_HIST_STATE = "UPDATE_FLOW_HIST_STATE";
    String UPDATE_FLOW_HIST_RESULT = "UPDATE_FLOW_HIST_RESULT";
}
