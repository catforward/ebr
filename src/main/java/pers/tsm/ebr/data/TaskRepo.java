/**
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
package pers.tsm.ebr.data;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;

import io.vertx.core.json.JsonObject;

/**
 *
 *
 * @author l.gong
 */
public class TaskRepo {
    private static final Logger logger = LoggerFactory.getLogger(TaskRepo.class);
    private final ReentrantLock poolLock;
    /** key: flow_url, value: task class*/
    private final Map<String, Flow> runningFlowPool;
    /** key: flow_url, value: task class*/
    private Cache<String, Flow> idleFlowPool;
    private final Queue<Task> taskQueue;

    public static final RemovalListener<String, JsonObject> removalListener = notification -> {
        logger.debug("define file content cache: Key {} was removed ({})",
                notification.getKey(), notification.getCause());
    };

    private static class InstanceHolder {
        private static final TaskRepo INSTANCE = new TaskRepo();
    }

    private TaskRepo() {
        poolLock = new ReentrantLock();
        runningFlowPool = new ConcurrentHashMap<>();
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    public static void release() {
        InstanceHolder.INSTANCE.taskQueue.clear();
        InstanceHolder.INSTANCE.runningFlowPool.clear();
    }

    public static void setIdleFlowPoolCache(Cache<String, Flow> cache) {
        requireNonNull(cache);
        synchronized(InstanceHolder.INSTANCE) {
            if (!isNull(InstanceHolder.INSTANCE.idleFlowPool)) {
                InstanceHolder.INSTANCE.idleFlowPool.invalidateAll();
            }
            InstanceHolder.INSTANCE.idleFlowPool = cache;
        }
    }

    public static Flow getFlow(String flowUrl) {
        requireNonNull(flowUrl);
        Flow flow = InstanceHolder.INSTANCE.runningFlowPool.get(flowUrl);
        if (isNull(flow)) {
            flow = InstanceHolder.INSTANCE.idleFlowPool.getIfPresent(flowUrl);
        }
        if (isNull(flow)) {
            flow = InstanceHolder.INSTANCE.createFlowFromDefine(flowUrl);
        }
        return flow;
    }

    public static Task getTask(String flowUrl, String taskUrl) {
        requireNonNull(flowUrl);
        requireNonNull(taskUrl);
        Flow flow = getFlow(flowUrl);
        if (isNull(flow)) {
            return null;
        } else {
            return flow.getTask(taskUrl);
        }
    }

    public static void pushRunnableFlow(Flow flow) {
        requireNonNull(flow);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.idleFlowPool.invalidate(flow.getUrl());
            InstanceHolder.INSTANCE.runningFlowPool.remove(flow.getUrl());
            InstanceHolder.INSTANCE.runningFlowPool.put(flow.getUrl(), flow);
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    public static void removeRunnableFlow(Flow flow) {
        requireNonNull(flow);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.runningFlowPool.remove(flow.getUrl());
            InstanceHolder.INSTANCE.idleFlowPool.put(flow.getUrl(), flow);
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    public static void pushRunnableTask(Task task) {
        requireNonNull(task);
        InstanceHolder.INSTANCE.taskQueue.add(task);
    }

    public static Task pollRunnableTask() {
        return InstanceHolder.INSTANCE.taskQueue.poll();
    }

    private Flow createFlowFromDefine(String flowUrl) {
        try {
            JsonObject content = TaskDefineRepo.getDefineFileContent(flowUrl);
            FlowMaker maker = new FlowMaker(flowUrl, content);
            return maker.makeAndValidate();
        } catch (ExecutionException e) {
            logger.error("", e);
            return null;
        }
    }

}
