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
package pers.ebr.server.common.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.model.ITask;
import pers.ebr.server.common.model.DagFlow;

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
     * key : url ex。uuid::flowId
     * value : instance of task flow
     */
    private final Map<String, DagFlow> flowPool = new ConcurrentHashMap<>();
    private final Queue<ITask> taskQueue = new ConcurrentLinkedQueue<>();


    InMemoryPoolImpl() {
    }

    @Override
    public IPool init() {
        return this;
    }

    @Override
    public void close() {
        flowPool.clear();
        taskQueue.clear();
    }

    @Override
    public DagFlow getFlowItem(String url) {
        checkNotNull(url);
        return flowPool.get(url);
    }

    @Override
    public DagFlow getFlowItemOf(String url) {
        checkNotNull(url);
        String[] urlArr = url.split("/");
        String flowUrl = urlArr.length > 0 ? urlArr[0] : "";
        return flowPool.get(flowUrl);
    }

    @Override
    public void setFlowItem(DagFlow flow) {
        checkNotNull(flow);
        flowPool.put(flow.url(), flow);
    }

    @Override
    public ITask getTaskItem(String url) {
        checkNotNull(url);
        String[] urlArr = url.split("/");
        String flowUrl = urlArr.length > 0 ? urlArr[0] : "";
        DagFlow flow = flowPool.get(flowUrl);
        String taskId = urlArr.length > 1 ? urlArr[urlArr.length - 1] : flow.flowId();
        return flow.getTask(taskId);
    }

    @Override
    public void addTaskQueue(ITask task) {
        checkNotNull(task);
        taskQueue.add(task);
    }

    @Override
    public ITask pollTaskQueue() {
        // 从队列的头部取出并删除该条数据
        return taskQueue.poll();
    }

}
