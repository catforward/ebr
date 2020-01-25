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
package pers.ebr.cli.core.data;

import pers.ebr.cli.core.Task;

import java.util.List;

/**
 * <pre>
 * meta info of job
 * </pre>
 * @author l.gong
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
