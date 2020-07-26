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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.DAGWorkflow;

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
public final class InMemoryPoolImpl implements IPool {
    private final static Logger logger = LoggerFactory.getLogger(InMemoryPoolImpl.class);
    final static String TYPE = "memory";
    /**
     * active task flow pool
     * key : flow's instanceId
     * value : flow instance
     */
    private final Map<String, DAGWorkflow> allFlows = new ConcurrentHashMap<>();
    private final Queue<ITask> taskQueue = new ConcurrentLinkedQueue<>();


    InMemoryPoolImpl() {
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
    public DAGWorkflow getWorkflowByUrl(String url) {
        checkNotNull(url);
        for (var entry : allFlows.entrySet()) {
            DAGWorkflow flow = entry.getValue();
            if (flow.getRootTask().getUrl().equals(url)) {
                return flow;
            }
        }
        return null;
    }

    @Override
    public DAGWorkflow getWorkflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        return allFlows.get(instanceId);
    }

    @Override
    public void setFlow(DAGWorkflow flow) {
        checkNotNull(flow);
        allFlows.put(flow.getInstanceId(), flow);
    }

    @Override
    public DAGWorkflow removeWorkflowByInstanceId(String instanceId) {
        checkNotNull(instanceId);
        DAGWorkflow flow = allFlows.get(instanceId);
        allFlows.remove(instanceId);
        return flow;
    }

    @Override
    public void addRunnableTaskQueue(ITask task) {
        checkNotNull(task);
        taskQueue.add(task);
    }

    @Override
    public ITask pollRunnableTaskQueue() {
        // 从队列的头部取出并删除该条数据
        return taskQueue.poll();
    }

}
