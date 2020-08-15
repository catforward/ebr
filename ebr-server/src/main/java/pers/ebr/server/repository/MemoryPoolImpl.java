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
import pers.ebr.server.model.ITaskflow;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static pers.ebr.server.common.Utils.checkNotNull;

/**
 * <p>
 * 任务对象池的纯内存实现
 * </p>
 *
 * @author l.gong
 */
public final class MemoryPoolImpl implements IPool {

    /**
     * 运行中taskflow对象
     * key : 实例ID
     * value : taskflow对象
     */
    private final Map<String, ITaskflow> allFlows = new ConcurrentHashMap<>();
    private final Queue<IExternalCommandTask> taskQueue = new ConcurrentLinkedQueue<>();


    MemoryPoolImpl() {}

    /**
     * 初始化对象池
     *
     * @return IPool
     */
    @Override
    public IPool init() {
        return this;
    }

    /**
     * 关闭对象池
     */
    @Override
    public void close() {
        allFlows.clear();
        taskQueue.clear();
    }

    /**
     * 使用任务流的逻辑路径来获取任务流对象
     * @param path 任务流的逻辑路径
     * @return IWorkflow
     */
    @Override
    public ITaskflow getRunningTaskflowByPath(String path) {
        checkNotNull(path);
        for (var entry : allFlows.entrySet()) {
            ITaskflow taskflow = entry.getValue();
            if (taskflow.getRootTask().getPath().equals(path)) {
                return taskflow;
            }
        }
        return null;
    }

    /**
     * 使用实例ID来获取任务流对象
     * @param instanceId [in] 实例ID
     * @return IWorkflow
     */
    @Override
    public ITaskflow getRunningTaskflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        return allFlows.get(instanceId);
    }

    /**
     * 添加一个任务流对象至对象池
     * @param taskflow [in] 任务流对象
     */
    @Override
    public void addRunningTaskflow(ITaskflow taskflow) {
        checkNotNull(taskflow);
        allFlows.put(taskflow.getInstanceId(), taskflow);
    }

    /**
     * 使用实例ID从对象池中删除一个任务流对象
     * @param instanceId [in] 实例ID
     */
    @Override
    public void removeRunningTaskflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        ITaskflow taskflow = allFlows.get(instanceId);
        if (taskflow != null) {
            taskflow.release();
            allFlows.remove(instanceId, taskflow);
        }
    }

    /**
     * 增加一个任务至可执行任务队列
     * @param task [in] 待添加的任务
     */
    @Override
    public void addRunnableTaskQueue(IExternalCommandTask task) {
        checkNotNull(task);
        taskQueue.add(task);
    }

    /**
     * 从可执行队列头获取一个待执行的任务
     * @return IExternalCommandTask
     */
    @Override
    public IExternalCommandTask pollRunnableTaskQueue() {
        // 从队列的头部取出并删除该条数据
        return taskQueue.poll();
    }

}
