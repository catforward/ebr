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
package pers.ebr.server.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pers.ebr.server.common.model.TaskState.*;
import static pers.ebr.server.common.model.TaskType.UNIT;

/**
 * <pre>
 * The implement of Task Interface
 * </pre>
 *
 * @author l.gong
 */
public class TaskImpl implements Task {

    private final static Logger logger = LoggerFactory.getLogger(TaskImpl.class);
    /* defined attributes */
    private String taskId;
    private String groupId;
    private String cmdLine;
    private String taskDesc;
    private Set<String> depSet;
    private List<Task> subList;
    /* runtime attributes */
    private String taskUrl;
    private volatile TaskState taskState;
    private TaskType taskType;

    TaskImpl(String newId) {
        depSet = new HashSet<>();
        subList = new ArrayList<>();
        id(newId);
        groupId("");
        cmdLine("");
        desc("");
        url("");
        status(INACTIVE);
        type(UNIT);
    }

    @Override
    public String toString() {
        return this.id();
    }

    @Override
    public String id() {
        return this.taskId;
    }

    @Override
    public String cmdLine() {
        return this.cmdLine;
    }

    @Override
    public String url() {
        return this.taskUrl;
    }

    @Override
    public String desc() {
        return this.taskDesc;
    }

    @Override
    public String groupId() {
        return this.groupId;
    }

    @Override
    public Set<String> deps() {
        return this.depSet;
    }

    @Override
    public List<Task> subs() {
        return this.subList;
    }

    @Override
    public TaskState status() {
        return this.taskState;
    }

    @Override
    public TaskType type() {
        return taskType;
    }

    @Override
    public void url(String newUrl) {
        this.taskUrl = newUrl;
    }

    @Override
    public void id(String newId) {
        this.taskId = newId;
    }

    @Override
    public void cmdLine(String cmd) {
        this.cmdLine = cmd;
    }

    @Override
    public void desc(String value) {
        this.taskDesc = value;
    }

    @Override
    public void groupId(String id) {
        this.groupId = id;
    }

    @Override
    public void deps(String id) {
        this.depSet.add(id);
    }

    @Override
    public void subs(Task other) {
        subList.add(other);
    }

    @Override
    public void status(TaskState status) {
        logger.info("state changed->task:[{}] state:[{} -> {}]", taskId, taskState, status);
        switch (taskState) {
            case INACTIVE: {
                if (ACTIVE == status) {
                    taskState = status;
                }
                break;
            }
            case ACTIVE: {
                if (COMPLETE == status || FAILED == status) {
                    taskState = status;
                }
                break;
            }
            case COMPLETE:
            case FAILED:
            default: {
                throw new RuntimeException(String.format("invalidate state task:[%s] state:[%s]->[%s]", taskId, taskState, status));
            }
        }
    }

    @Override
    public void type(TaskType newType) {
        if (this.taskType != newType) {
            this.taskType = newType;
        }
    }

    public boolean isRootTask() {
        return id() == null || id().strip().isEmpty() || id().equalsIgnoreCase(groupId());
    }

}
