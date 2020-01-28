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
package pers.ebr.cli.core;

import java.util.List;

/**
 * <pre>
 * meta info of task
 * </pre>
 * @author l.gong
 */
public interface Task {

    /**
     * <pre>
     * task's id
     * </pre>
     * @return id
     */
    String id();

    /**
     * <pre>
     * task's description
     * </pre>
     * @return desc
     */
    String desc();

    /**
     * <pre>
     * the command of task
     * </pre>
     * @return command
     */
    String command();

    /**
     * <pre>
     * the pre condition tasks of this task
     * </pre>
     * @return define str of pre tasks
     */
    List<String> preTasks();

    /**
     * <pre>
     * the sub tasks of this task
     * </pre>
     * @return task's list of children
     */
    List<Task> children();

    /**
     * <pre>
     * the parent of this task
     * </pre>
     * @return parent task
     */
    Task parentTask();
}
