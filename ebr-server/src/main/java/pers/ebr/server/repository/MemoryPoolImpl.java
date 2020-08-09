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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.model.IExternalCommandTask;
import pers.ebr.server.model.IWorkflow;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static pers.ebr.server.common.Utils.checkNotNull;

/**
 * The InMemory TaskPool Implementation
 *
 * @author l.gong
 */
public final class MemoryPoolImpl implements IPool {
    private final static Logger logger = LoggerFactory.getLogger(MemoryPoolImpl.class);
    final static String TYPE = "memory";
    /**
     * active task flow pool
     * key : flow's instanceId
     * value : flow instance
     */
    private final Map<String, IWorkflow> allFlows = new ConcurrentHashMap<>();
    private final Queue<IExternalCommandTask> taskQueue = new ConcurrentLinkedQueue<>();


    MemoryPoolImpl() {
    }

    @Override
    public IPool init() {
        return this;
    }

    @Override
    public void close() {
        allFlows.clear();
        taskQueue.clear();
    }

    @Override
    public IWorkflow getRunningWorkflowByPath(String url) {
        checkNotNull(url);
        for (var entry : allFlows.entrySet()) {
            IWorkflow workflow = entry.getValue();
            if (workflow.getRootTask().getPath().equals(url)) {
                return workflow;
            }
        }
        return null;
    }

    @Override
    public IWorkflow getRunningWorkflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        return allFlows.get(instanceId);
    }

    @Override
    public void addRunningWorkflow(IWorkflow workflow) {
        checkNotNull(workflow);
        allFlows.put(workflow.getInstanceId(), workflow);
    }

    @Override
    public void removeRunningWorkflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        IWorkflow workflow = allFlows.get(instanceId);
        if (workflow != null) {
            workflow.release();
            allFlows.remove(instanceId, workflow);
        }
    }

    @Override
    public void addRunnableTaskQueue(IExternalCommandTask task) {
        checkNotNull(task);
        taskQueue.add(task);
    }

    @Override
    public IExternalCommandTask pollRunnableTaskQueue() {
        // 从队列的头部取出并删除该条数据
        return taskQueue.poll();
    }

}
