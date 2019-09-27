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
import ebr.core.data.JobState;
import ebr.core.data.JobType;
import ebr.core.Task;
import ebr.core.util.graph.DirectedGraph;
import ebr.core.util.graph.GraphBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <pre>
 * the implements of Job
 * </pre>
 * @author catforward
 */
public class JobImpl implements Job {
    private final static int INIT_CAP = 8;
    /** 任务基本属性定义 */
    private final Task task;
    /** 任务间的关系定义 */
    private final JobImpl parent;
    final ArrayList<JobImpl> children;
    final ArrayList<Job> preconditions;
    /** 任务扩展属性 */
    private final String url;
    private final JobType type;
    /** 任务状态 */
    private volatile JobState state;
    /** 当单元类型为FLOW时，记录子任务的完成数 */
    private AtomicInteger unfinishedCount;

    JobImpl(JobImpl parent, Task task, String url, JobType type) {
        this.children = new ArrayList<>(INIT_CAP);
        this.preconditions = new ArrayList<>(INIT_CAP);
        this.parent = parent;
        this.task = task;
        this.url = url;
        this.type = type;
    }

    @Override
    public String toString() {
        return id();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), desc(), command(), url, type);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JobImpl)) {
            return false;
        }
        JobImpl job = (JobImpl) o;
        return Objects.equals(id(), job.id()) &&
                Objects.equals(desc(), job.desc()) &&
                Objects.equals(command(), job.command()) &&
                Objects.equals(url, job.url) &&
                Objects.equals(type, job.type) &&
                Objects.equals(parent, job.parent) &&
                Objects.equals(children, job.children) &&
                Objects.equals(preconditions, job.preconditions) &&
                Objects.equals(state, job.state) &&
                Objects.equals(unfinishedCount, job.unfinishedCount);
    }

    /**
     * <pre>
     * MODULE单元
     * 当子单元成功执行后，子调用此函数，将未完成子单元数减1
     * 当数量为零时，视为此MODULE执行成功
     * </pre>
     */
    private void childJobCompleted() {
        if (JobType.TASK != type && unfinishedCount.decrementAndGet() <= 0) {
            updateState(JobState.COMPLETE);
        }
    }

    /**
     * <pre>
     * update the status of this job
     * </pre>
     *
     * @param newState 新的状态
     */
    @Override
    public void updateState(JobState newState) {
        switch (newState) {
            case INACTIVE: {
                state = JobState.INACTIVE;
                unfinishedCount = new AtomicInteger(children.size());
                break;
            }
            case COMPLETE: {
                state = JobState.COMPLETE;
                if (parent != null && JobType.TASK != parent.type) {
                    parent.childJobCompleted();
                }
                break;
            }
            case ACTIVE: {
                state = JobState.ACTIVE;
                break;
            }
            case FAILED: {
                state = JobState.FAILED;
                if (parent != null) {
                    // 传递到root job
                    parent.updateState(newState);
                }
            }
            default: {
                state = JobState.FAILED;
                break;
            }
        }
        state = newState;
    }

    /**
     * <pre>
     * is this job's state equals complete
     * </pre>
     * @return boolean
     */
    @Override
    public boolean isComplete() {
        return state == JobState.COMPLETE;
    }

    /**
     * <pre>
     * job's url which can present the Parent-Child relationship between jobs
     * combine the id of jobs like this below
     * sample:
     *  parent id : job1 -> url : /job1
     *  child1 id : job2 -> url : /job1/job2
     *  child2 id : job3 -> url : /job1/job3
     *  son1(child of child1) id : job2-1 -> url : /job1/job2/job2-1
     * </pre>
     *
     * @return url
     */
    @Override
    public String url() {
        return this.url;
    }

    /**
     * <pre>
     * job's type
     * </pre>
     *
     * @return type
     */
    @Override
    public JobType type() {
        return this.type;
    }

    /**
     * <pre>
     * status of this job
     * </pre>
     *
     * @return status
     */
    @Override
    public JobState state() {
        return this.state;
    }

    /**
     * <pre>
     * parent job of this job
     * </pre>
     *
     * @return parent job
     */
    @Override
    public Job parentJob() {
        return this.parent;
    }

    /**
     * <pre>
     * job preconditions of this job
     * </pre>
     *
     * @return preconditions
     */
    @Override
    public List<Job> getPreconditions() {
        return this.preconditions;
    }

    /**
     * <pre>
     * task's id
     * </pre>
     *
     * @return id
     */
    @Override
    public String id() {
        return this.task.id();
    }

    /**
     * <pre>
     * task's description
     * </pre>
     *
     * @return desc
     */
    @Override
    public String desc() {
        return this.task.desc();
    }

    /**
     * <pre>
     * the command of task
     * </pre>
     *
     * @return command
     */
    @Override
    public String command() {
        return this.task.command();
    }

    /**
     * <pre>
     * the pre condition tasks of this task
     * </pre>
     *
     * @return define str of pre tasks
     */
    @Override
    public List<String> preTasks() {
        return this.task.preTasks();
    }

    /**
     * <pre>
     * the sub tasks of this task
     * </pre>
     *
     * @return task's list of children
     */
    @Override
    public List<Task> children() {
        return this.task.children();
    }

    /**
     * <pre>
     * the parent of this task
     * </pre>
     *
     * @return parent task
     */
    @Override
    public Task parentTask() {
        return this.task.parentTask();
    }
}

class JobFlow {
    final JobImpl rootJob;
    private final DirectedGraph<Job> flowGraph;

    private JobFlow(JobImpl job) {
        rootJob = job;
        flowGraph = createEmptyGraph();
    }

    @Override
    public String toString() {
        return rootJob.toString() + ": " + flowGraph.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootJob.hashCode(), flowGraph.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JobFlow)) {
            return false;
        }
        JobFlow flow = (JobFlow) o;
        return Objects.equals(rootJob, flow.rootJob) &&
                Objects.equals(flowGraph, flow.flowGraph);
    }

    public void standby() {
        rootJob.updateState(JobState.INACTIVE);
    }

    public boolean isComplete() {
        return rootJob.isComplete();
    }

    public void start() {
        // 所有unit进入standby状态
        flowGraph.vertexes().forEach(unit -> unit.updateState(JobState.INACTIVE));
        rootJob.updateState(JobState.ACTIVE);
    }

    public Set<Job> getTopLevelJobs() {
        return flowGraph.vertexes().stream()
                .filter(job -> rootJob.url().equals(job.parentJob().url()))
                .collect(Collectors.toSet());
    }

    public Set<Job> getSuccessorsOf(Job job) {
        return flowGraph.successors(job);
    }

    public Set<Job> getPredecessorsOf(Job job) {
        return flowGraph.predecessors(job);
    }

    static JobFlow makeFrom(JobImpl job) {
        return new JobFlow(job).build();
    }

    private JobFlow build() {
        for (JobImpl child : rootJob.children) {
            createTaskGraph(child);
        }
        return this;
    }

    private void createTaskGraph(Job job) {
        flowGraph.addVertex(job);
        if (!job.getPreconditions().isEmpty()) {
            for (Job predecessor : job.getPreconditions()) {
                flowGraph.putEdge(predecessor, job);
            }
        }
    }

    private DirectedGraph<Job> createEmptyGraph() {
        // 指定为有向图
        return GraphBuilder.directed()
                //.setInsertionOrder(true)
                // 不允许自环
                .setAllowsSelfLoops(false)
                .build();
    }
}
