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

import pers.ebr.server.model.IExternalCommandTask;
import pers.ebr.server.model.IWorkflow;

/**
 * <p>
 * 应用内对象池
 * </p>
 *
 * @author l.gong
 */
public interface IPool {
    /**
     * 初始化对象池
     *
     * @return IPool
     */
    IPool init();

    /**
     * 关闭对象池
     */
    void close();

    /**
     * 使用任务流的逻辑路径来获取任务流对象
     * @param path 任务流的逻辑路径
     * @return IWorkflow
     */
    IWorkflow getRunningWorkflowByPath(String path);

    /**
     * 使用实例ID来获取任务流对象
     * @param instanceId 实例ID
     * @return IWorkflow
     */
    IWorkflow getRunningWorkflowByInstanceId(String instanceId);

    /**
     * 添加一个任务流对象至对象池
     * @param workflow 任务流对象
     */
    void addRunningWorkflow(IWorkflow workflow);

    /**
     * 使用实例ID从对象池中删除一个任务流对象
     * @param instanceId 实例ID
     */
    void removeRunningWorkflowByInstanceId(String instanceId);

    /**
     * 增加一个任务至可执行任务队列
     * @param task 待添加的任务
     */
    void addRunnableTaskQueue(IExternalCommandTask task);

    /**
     * 从可执行队列头获取一个待执行的任务
     * @return IExternalCommandTask
     */
    IExternalCommandTask pollRunnableTaskQueue();
}
