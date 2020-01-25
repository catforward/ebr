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
package pers.ebr.cli.core.jobs;

import pers.ebr.cli.core.data.Job;
import pers.ebr.cli.core.data.JobType;
import pers.ebr.cli.core.util.AppLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 运行时统一管理任务执行状态
 * 现在只针对一个定义文件中的任务进行管理
 * </pre>
 * @author l.gong
 */
enum JobItemStateHolder {
    /** 单例 */
    STATE_HOLDER;

    /** 任务流集合 */
    private final Map<String, JobFlow> urlJobFlowMap = new ConcurrentHashMap<>();
    /** 任务单元集合 */
    private final Map<String, Job> urlJobMap = new ConcurrentHashMap<>();
    /** 根任务流 */
    private JobFlow rootFlow = null;

    JobItemStateHolder() {
    }

    /**
     * <pre>
     * 添加一个任务流
     * </pre>
     * @param newJobFlow 新的Flow实例
     */
    static void addJobFlow(JobFlow newJobFlow) {
        if (!STATE_HOLDER.urlJobFlowMap.containsKey(newJobFlow.rootJob.url())) {
            STATE_HOLDER.urlJobFlowMap.put(newJobFlow.rootJob.url(), newJobFlow);
        }
        if (STATE_HOLDER.rootFlow == null && JobType.FLOW == newJobFlow.rootJob.type()) {
            STATE_HOLDER.rootFlow = newJobFlow;
        }
        AppLogger.info(newJobFlow.toString());
    }

    /**
     * <pre>
     * 添加一个任务单元
     * </pre>
     * @param newJob 新的Job实例
     */
    static void addJob(Job newJob) {
        if (!STATE_HOLDER.urlJobMap.containsKey(newJob.url())) {
            STATE_HOLDER.urlJobMap.put(newJob.url(), newJob);
        }
    }

    static JobFlow getRootJobFlow() {
        return STATE_HOLDER.rootFlow;
    }

    static JobFlow getJobFlow(String url) {
        return STATE_HOLDER.urlJobFlowMap.get(url);
    }

    static Job getJob(String url) {
        return STATE_HOLDER.urlJobMap.get(url);
    }

    static void clear() {
        STATE_HOLDER.rootFlow = null;
        STATE_HOLDER.urlJobFlowMap.clear();
        STATE_HOLDER.urlJobMap.clear();
    }
}
