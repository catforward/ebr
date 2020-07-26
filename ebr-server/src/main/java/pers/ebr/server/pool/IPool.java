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
package pers.ebr.server.pool;

import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.DAGWorkflow;

/**
 * The TaskPool Interface
 *
 * @author l.gong
 */
public interface IPool {
    IPool init();
    void close();

    DAGWorkflow getWorkflowByUrl(String url);
    DAGWorkflow getWorkflowByInstanceId(String instanceId);
    void setFlow(DAGWorkflow flow);
    DAGWorkflow removeWorkflowByInstanceId(String instanceId);

    void addRunnableTaskQueue(ITask task);
    ITask pollRunnableTaskQueue();
}
