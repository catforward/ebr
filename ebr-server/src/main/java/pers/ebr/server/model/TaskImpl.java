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
package pers.ebr.server.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.constant.TaskState;
import pers.ebr.server.constant.TaskType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pers.ebr.server.constant.TaskState.INACTIVE;
import static pers.ebr.server.constant.TaskType.UNIT;

/**
 * <pre>
 * The implement of Task Interface
 * </pre>
 *
 * @author l.gong
 */
public class TaskImpl implements Task {

    private final static Logger logger = LoggerFactory.getLogger(TaskImpl.class);

    private String taskId;
    private String groupId;
    private String cmdLine;
    private String taskDesc;
    private Set<String> dependSet;
    private volatile TaskState taskState;
    private TaskType taskType;

    public TaskImpl(String newId) {
        dependSet = new HashSet<>();
        id(newId);
        groupId("");
        cmdLine("");
        desc("");
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
    public String desc() {
        return this.taskDesc;
    }

    @Override
    public String groupId() {
        return this.groupId;
    }

    @Override
    public Set<String> depends() {
        return this.dependSet;
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
    public void depends(String id) {
        this.dependSet.add(id);
    }

    @Override
    public void status(TaskState status) {
        this.taskState = status;
    }

    @Override
    public void type(TaskType newType) {
        if (this.taskType != newType) {
            this.taskType = newType;
        }
    }

    public boolean isFlowItem() {
        return id() == null || id().strip().isEmpty() || id().equalsIgnoreCase(groupId());
    }

}
