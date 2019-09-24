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
 * 模块内运行时统一管理任务执行状态
 * 现在只针对一个定义文件中的任务进行管理
 * </pre>
 * @author catforward
 */
class JobItemStateHolder {
    private final static JobItemStateHolder INSTANCE = new JobItemStateHolder();
    /** 任务流集合 */
    private final Map<String, JobFlow> urlJobFlowMap = new ConcurrentHashMap<>();
    /** 任务单元集合 */
    private final Map<String, Job> urlJobMap = new ConcurrentHashMap<>();
    /** 根任务流 */
    private JobFlow rootFlow = null;

    private JobItemStateHolder() {
    }

    /**
     * <pre>
     * 添加一个任务流
     * </pre>
     * @param newJobFlow
     */
    static void addJobFlow(JobFlow newJobFlow) {
        if (!INSTANCE.urlJobFlowMap.containsKey(newJobFlow.rootJob.url())) {
            INSTANCE.urlJobFlowMap.put(newJobFlow.rootJob.url(), newJobFlow);
        }
        if (INSTANCE.rootFlow == null && JobType.FLOW == newJobFlow.rootJob.type()) {
            INSTANCE.rootFlow = newJobFlow;
        }
        AppLogger.info(newJobFlow.toString());
    }

    /**
     * <pre>
     * 添加一个任务单元
     * </pre>
     * @param newJob
     */
    static void addJob(Job newJob) {
        if (!INSTANCE.urlJobMap.containsKey(newJob.url())) {
            INSTANCE.urlJobMap.put(newJob.url(), newJob);
        }
    }

    static JobFlow getRootJobFlow() {
        return INSTANCE.rootFlow;
    }

    static JobFlow getJobFlow(String url) {
        return INSTANCE.urlJobFlowMap.get(url);
    }

    static Job getJob(String url) {
        return INSTANCE.urlJobMap.get(url);
    }

    static void clear() {
        INSTANCE.rootFlow = null;
        INSTANCE.urlJobFlowMap.clear();
        INSTANCE.urlJobMap.clear();
    }
}
