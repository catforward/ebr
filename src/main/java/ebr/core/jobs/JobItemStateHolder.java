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

import ebr.core.data.Job;
import ebr.core.data.JobType;
import ebr.core.util.AppLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 运行时统一管理任务执行状态
 * 现在只针对一个定义文件中的任务进行管理
 * </pre>
 * @author catforward
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
