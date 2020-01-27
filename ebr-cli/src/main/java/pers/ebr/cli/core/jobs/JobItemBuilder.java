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

import pers.ebr.cli.core.EbrException;
import pers.ebr.cli.core.types.JobType;
import pers.ebr.cli.core.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static pers.ebr.cli.util.MiscUtils.checkCommandBanList;
import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 * <pre>
 * the builder of JobFlow and Job
 * </pre>
 * @author l.gong
 */
public final class JobItemBuilder {
    private static final int INIT_CAP = 16;
    /**
     *  define the max depth between flow and task
     * sample
     *  depth(1): flow(root) -> task
     *  depth(2): flow(root) -> flow(sub) -> task
     *  depth(3): flow(root) -> flow(sub) -> flow(sub) ... NG!!
     */
    private static final int MAX_DEPTH = 2;
    /** internal symbols in app */
    private static final String KEY_ROOT_UNIT = "KEY_ROOT_UNIT";

    private JobItemBuilder() {}

    public static String createJobs(Task rootTask) {
        validateTask(rootTask);
        HashMap<String, JobImpl> idJobMap = new HashMap<>(INIT_CAP);
        createJobTree(rootTask, null, idJobMap);
        JobImpl rootJob = idJobMap.get(KEY_ROOT_UNIT);
        updatePredecessors(rootJob, idJobMap);
        createJobFlow(rootJob);
        return rootJob.url();
    }

    /**
     * <pre>
     * 验证生成的任务单元的基本结构是否正确
     * </pre>
     * @param task 需要检查的Task实例
     */
    private static void validateTask(Task task) {
        checkNotNull(task);
        // id
        if (task.id() == null || task.id().isBlank()) {
            throw new EbrException("the define of uid is not exist!");
        }
        // command
        if (task.children().isEmpty()
                && (task.command() == null || task.command().isBlank())) {
            throw new EbrException(String.format("[%s]: the define onf command is not exist!", task.id()));
        }
        // command ban list
        if (task.command() != null && !task.command().isBlank()) {
            checkCommandBanList(task.command());
        }
        // depth
        if (getDepth(task) > MAX_DEPTH) {
            throw new EbrException("the max depth can not over 2");
        }
        // children
        for (Task child : task.children()) {
            validateTask(child);
        }
    }

    private static int getDepth(Task task) {
        int depth = 0;
        if (task.parentTask() != null) {
            ++depth;
            depth += getDepth(task.parentTask());
        }
        return depth;
    }

    private static void createJobTree(Task task, JobImpl parent, HashMap<String, JobImpl> idJobMap) {
        JobImpl currentJob = Optional.ofNullable(idJobMap.get(task.id())).orElseGet(() -> {
            if (parent == null) {
                JobImpl job =new JobImpl(null, task,
                        String.format("/%s", task.id()),
                        JobType.FLOW);
                idJobMap.put(KEY_ROOT_UNIT, job);
                return job;
            } else {
                String url = String.format("%s/%s", parent.url(), task.id());
                JobType type = task.children().isEmpty() ? JobType.TASK : JobType.FLOW;
                JobImpl job = new JobImpl(parent, task, url, type);
                parent.children.add(job);
                return job;
            }
        });

        JobItemStateHolder.addJob(currentJob);
        idJobMap.put(currentJob.id(), currentJob);

        for (Task child : task.children()) {
            createJobTree(child, currentJob, idJobMap);
        }
    }

    private static void updatePredecessors(JobImpl job, HashMap<String, JobImpl> idJobMap) {
        if (job == null) {
            return;
        }
        List<String> pIds = job.preTasks();
        if (pIds != null && !pIds.isEmpty()) {
            pIds.forEach(id -> {
                JobImpl preJob = idJobMap.get(id);
                if (preJob != null) {
                    job.preconditions.add(preJob);
                }
            });
        }

        for (JobImpl child : job.children) {
            updatePredecessors(child, idJobMap);
        }
    }

    private static void createJobFlow(JobImpl job) {
        JobFlow flow = JobFlow.makeFrom(job);
        flow.standby();
        JobItemStateHolder.addJobFlow(flow);
        for (JobImpl child : job.children) {
            if (JobType.FLOW == child.type()) {
                createJobFlow(child);
            }
        }
    }
}
