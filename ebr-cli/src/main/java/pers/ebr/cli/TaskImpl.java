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
package pers.ebr.cli;

import pers.ebr.cli.core.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Task实现类
 * 保存任务定义的基本属性
 * @author l.gong
 */
public final class TaskImpl implements Task {
    private static final int INIT_CAP = 8;

    private final String id;
    private final TaskImpl parent;
    final List<Task> children = new ArrayList<>(INIT_CAP);
    final List<String> preTask = new ArrayList<>(INIT_CAP);
    String desc;
    String command;

    TaskImpl(String id, TaskImpl parent) {
        this.id = id;
        this.parent = parent;
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
        return id;
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
        return desc;
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
        return command;
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
        return preTask;
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
        return children;
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
        return parent;
    }
}
