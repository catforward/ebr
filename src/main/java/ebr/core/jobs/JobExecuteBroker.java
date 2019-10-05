/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.core.jobs;

import ebr.core.EbrException;
import ebr.core.base.Broker.BaseBroker;
import ebr.core.base.Broker.Id;
import ebr.core.base.Message;
import ebr.core.base.Message.Symbols;
import ebr.core.data.Job;
import ebr.core.data.JobState;
import ebr.core.util.AppLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 * 外部程序执行监视处理类
 * </pre>
 *
 * @author catforward
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
        register(Symbols.MSG_ACT_LAUNCH_JOB_ITEM);
    }

    @Override
    protected void onFinish() {
        unregister(Symbols.MSG_ACT_LAUNCH_JOB_ITEM);
    }

    @Override
    protected void onMessage(Message message) {
        if(Symbols.MSG_ACT_LAUNCH_JOB_ITEM.equals(message.act)) {
            List<String> urls = (List<String>) message.param.getOrDefault(Symbols.MSG_DATA_PERFORMABLE_JOB_ITEM_LIST, List.of());
            performJobs(urls);
        } else {
            throw new EbrException(String.format("[%s]:送错地方了老兄...", message.act));
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
        param.put(Symbols.MSG_DATA_JOB_URL, url);
        param.put(Symbols.MSG_DATA_NEW_JOB_STATE, newState);
        post(Symbols.MSG_ACT_JOB_STATE_CHANGED, param);
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
