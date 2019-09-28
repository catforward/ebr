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
package ebr.core.data;

import ebr.core.Task;

import java.util.List;

/**
 * <pre>
 * meta info of job
 * </pre>
 * @author catforward
 */
public interface Job extends Task {

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
     * @return url
     */
    String url();

    /**
     * <pre>
     * job's type
     * </pre>
     * @return type
     */
    JobType type();

    /**
     * <pre>
     * status of this job
     * </pre>
     * @return status
     */
    JobState state();

    /**
     * <pre>
     * update the status of this job
     * </pre>
     * @param state the new status
     */
    void updateState(JobState state);

    /**
     * <pre>
     * is this job's state equals complete
     * </pre>
     * @return boolean
     */
    boolean isComplete();

    /**
     * <pre>
     * parent job of this job
     * </pre>
     * @return parent job
     */
    Job parentJob();

    /**
     * <pre>
     * job preconditions of this job
     * </pre>
     * @return preconditions
     */
    List<Job> getPreconditions();
}
