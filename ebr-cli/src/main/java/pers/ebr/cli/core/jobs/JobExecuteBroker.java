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
import pers.ebr.cli.core.Broker.BaseBroker;
import pers.ebr.cli.core.Broker.Id;
import pers.ebr.cli.core.types.Job;
import pers.ebr.cli.core.types.JobState;
import pers.ebr.cli.util.AppLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static pers.ebr.cli.core.Message.*;

/**
 * <pre>
 * 外部程序执行监视处理类
 * </pre>
 *
 * @author l.gong
 */
public class JobExecuteBroker extends BaseBroker {

    public JobExecuteBroker() {
        super();
    }

    @Override
    public Id id() {
        return Id.EXECUTOR;
    }

    @Override
    protected void onInit() {
        register(MSG_ACT_LAUNCH_JOB_ITEM);
    }

    @Override
    protected void onFinish() {
        unregister(MSG_ACT_LAUNCH_JOB_ITEM);
    }

    @Override
    protected void onMessage(String topic, Map<String, Object> message) {
        if(MSG_ACT_LAUNCH_JOB_ITEM.equals(topic)) {
            List<String> urls = (List<String>) message.getOrDefault(MSG_DATA_PERFORMABLE_JOB_ITEM_LIST, List.of());
            performJobs(urls);
        } else {
            throw new EbrException(String.format("[%s]:送错地方了老兄...", topic));
        }
    }

    private void performJobs(List<String> urls) {
        for (String url : urls) {
            Job job = JobItemStateHolder.getJob(url);
            if (job.state() == JobState.INACTIVE) {
                perform(job);
            }
        }
    }

    /**
     * <pre>
     * 通知其他服务一个Task的新状态
     * </pre>
     * @param url task的识别url
     * @param newState task的新状态
     */
    private void noticeNewState(String url, JobState newState) {
        HashMap<String, Object> param = new HashMap<>(2);
        param.put(MSG_DATA_JOB_URL, url);
        param.put(MSG_DATA_NEW_JOB_STATE, newState);
        post(MSG_ACT_JOB_STATE_CHANGED, param);
    }

    /**
     * <pre>
     * 启动一个Job
     * </pre>
     * @param job 启动对象Job
     */
    private void perform(Job job) {
        noticeNewState(job.url(), JobState.ACTIVE);
        AppLogger.debug(String.format("Task启动%s", job.url()));
        CompletableFuture<JobState> future = deployTaskAsync(() -> {
            try {
                Process process = Runtime.getRuntime().exec(job.command());
                process.getOutputStream().close();

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        AppLogger.debug(line);
                    }
                }

                int exitCode = process.waitFor();
                AppLogger.debug(String.format("url = %s exitCode = %s", job.url(), exitCode));
                return (exitCode == 0) ? JobState.COMPLETE : JobState.FAILED;
            } catch (IOException | InterruptedException e) {
                AppLogger.dumpError(e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                return JobState.FAILED;
            }
        });

        future.whenComplete((retValue, exception) -> noticeNewState(job.url(), retValue));
    }
}
