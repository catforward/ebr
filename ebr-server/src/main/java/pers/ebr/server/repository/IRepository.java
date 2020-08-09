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

import pers.ebr.server.model.IWorkflow;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.model.ExternalCommandWorkflowView;

import java.util.Collection;

/**
 * <p>
 * 任务仓库
 * </p>
 *
 * @author l.gong
 */
public interface IRepository {

    /**
     * 保存任务流对象
     * @param workflow [in] 待保存的任务流
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    void saveWorkflow(IWorkflow workflow) throws RepositoryException;

    /**
     * 使用id查找并读取一个任务流
     * @param flowId 任务流ID
     * @return IWorkflow
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    IWorkflow loadWorkflow(String flowId) throws RepositoryException;

    /**
     * 使用id删除一个任务流
     * @param flowId 任务流ID
     * @return int
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    int removeWorkflow(String flowId) throws RepositoryException;

    /**
     * 判断给定ID的任务流定义是否存在
     * @param flowId 任务流ID
     * @return boolean
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    boolean isWorkflowExists(String flowId) throws RepositoryException;

    /**
     * 保存一次任务执行记录
     * @param instanceId 任务实例ID
     * @param path       任务逻辑路径
     * @param newState   任务最终状态
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    void saveTaskExecHist(String instanceId, String path, TaskState newState) throws RepositoryException;

    /**
     * 获取所有已保存的任务详细数据
     * @return Collection
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    Collection<ExternalCommandWorkflowView> getAllWorkflowDetail() throws RepositoryException;
}
