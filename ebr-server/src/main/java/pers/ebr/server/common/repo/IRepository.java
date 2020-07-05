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

import pers.ebr.server.common.model.DAGFlow;
import pers.ebr.server.common.model.TaskState;

import java.util.List;

/**
 * <pre>
 * The Repository Interface
 * </pre>
 *
 * @author l.gong
 */
public interface IRepository {
    void setFlow(String flowId, String flowDetail) throws RepositoryException;
    void setTaskDetail(DAGFlow flow) throws RepositoryException;
    void setTaskState(String instanceId, String taskUrl, TaskState newState) throws RepositoryException;
    String getFlow(String flowId) throws RepositoryException;
    List<String> getAllFlowId() throws RepositoryException;
}
