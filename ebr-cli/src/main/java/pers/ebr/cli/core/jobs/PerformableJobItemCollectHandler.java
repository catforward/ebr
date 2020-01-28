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
import pers.ebr.cli.core.Handler;
import pers.ebr.cli.core.types.Job;
import pers.ebr.cli.core.types.JobState;
import pers.ebr.cli.core.types.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static pers.ebr.cli.core.Message.*;

/**
 * <pre>
 * 发生Message.Symbols.MSG_ACT_LAUNCH_JOB_FLOW
 * 和Message.Symbols.MSG_ACT_JOB_STATE_CHANGED事件时
 * 收集所有可启动的目标job或flow
 * 查找策略
 * 1.查找直接后继节点，并检查后继节点的前躯是否全部结束
 *  - 如果后继节点是flow型则直接取出flow的top_level_job作为启动备选
 *  - 如果后继节点的前躯全部完成则作为启动备选
 * 2.查找所在flow的父flow，并查找所在flow的直接后继节点
 *  - 与1相同的策略
 * </pre>
 * @author l.gong
 */
public class PerformableJobItemCollectHandler implements Handler {
    private static final int INIT_CAP = 16;
    private final ArrayList<String> performableUrls;

    public PerformableJobItemCollectHandler() {
        super();
        performableUrls = new ArrayList<>(INIT_CAP);
    }

    /**
     * <pre>
     * 进行实际处理
     * 当返回false时表示处理链将被终止
     * </pre>
     *
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(Handler.HandlerContext context) {
        String act = context.getCurrentAction();
        if(MSG_ACT_LAUNCH_JOB_FLOW.equals(act)) {
            String url = (String) context.getParam(MSG_DATA_JOB_FLOW_URL);
            collectTopLevelJobs(url);
        } else if (MSG_ACT_JOB_STATE_CHANGED.equals(act)) {
            JobState state = (JobState) context.getParam(MSG_DATA_NEW_JOB_STATE);
            if (JobState.COMPLETE == state) {
                String url = (String) context.getParam(MSG_DATA_JOB_URL);
                searchPerformableJobs(url);
            }
        } else {
            throw new EbrException(String.format("虽然不太可能，但是，送错地方了老兄...[%s]:", act));
        }

        if (!performableUrls.isEmpty()) {
            context.setNextAction(MSG_ACT_LAUNCH_JOB_ITEM);
            context.addHandlerResult(MSG_DATA_PERFORMABLE_JOB_ITEM_LIST, List.copyOf(performableUrls));
        }

        return true;
    }

    private void collectTopLevelJobs(String url) {
        JobFlow flow = JobItemStateHolder.getJobFlow(url);
        Set<Job> jobs = flow.getTopLevelJobs();
        for (Job job : jobs) {
            if (flow.getPredecessorsOf(job).stream().allMatch(Job::isComplete)) {
                if (JobType.FLOW == job.type()) {
                    collectTopLevelJobs(job.url());
                } else {
                    performableUrls.add(job.url());
                }
            }
        }
        flow.start();
    }

    /**
     * <pre>
     * 查找顺序
     * 1.查找出此url的直接后集结点
     * 2.针对1的结果，检查每个后继节点的前提节点是否完成， 都完成了就加入待启动列表
     * 3.如果1没有找到任何直接的后继节点，则检查此url所在父节点是否为FLOW节点
     * 4.针对3的结果，如果父节点完成了则检索出父节点的直接后继节点，重复1的处理
     * </pre>
     * @param url 状态变更了的Job的URL
     */
    private void searchPerformableJobs(String url) {
        Job changedJob = JobItemStateHolder.getJob(url);
        Job parentJob = changedJob.parentJob();
        if (parentJob == null) {
            return;
        }
        JobFlow parentFlow = JobItemStateHolder.getJobFlow(parentJob.url());
        Set<Job> sucSet = parentFlow.getSuccessorsOf(changedJob);

        // 向后查找
        if (!sucSet.isEmpty()) {
            collectPerformableJobs(parentFlow, sucSet);
        }

        // 向前查找
        Job grandpaJob = parentJob.parentJob();
        if (parentFlow.isComplete() && grandpaJob != null) {
            JobFlow grandpaFlow = JobItemStateHolder.getJobFlow(grandpaJob.url());
            Set<Job> grandpaSucSet = grandpaFlow.getSuccessorsOf(parentJob);
            collectPerformableJobs(grandpaFlow, grandpaSucSet);
        }
    }

    /**
     * 查找给出的任务单元中前提条件满足可以启动的对象
     * @param flow 次节点集合所属的flow
     * @param sucSet 后继节点集合
     */
    private void collectPerformableJobs(JobFlow flow, Set<Job> sucSet) {
        for (Job suc : sucSet) {
            if(flow.getPredecessorsOf(suc).stream().allMatch(Job::isComplete)) {
                if (JobType.FLOW == suc.type()) {
                    collectTopLevelJobs(suc.url());
                } else {
                    performableUrls.add(suc.url());
                }
            }
        }
    }
}
