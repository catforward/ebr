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
public class TaskImpl implements ITask {

    private final static Logger logger = LoggerFactory.getLogger(TaskImpl.class);
    /** defined attributes */
    private String taskId;
    private String groupId;
    private String cmdLine;
    private String taskDesc;
    private final List<String> depIdList = new ArrayList<>();

    /** runtime attributes */
    private String taskUrl = "";
    private String instanceId = "";
    private volatile TaskState taskState = INACTIVE;
    private TaskType taskType = UNIT;
    private ITask groupTask = null;
    private final Set<ITask> depTaskSet = new HashSet<>();
    private final List<ITask> subTaskList = new ArrayList<>();

    TaskImpl(String id) {
        setId(id);
        setGroupId("");
        setCmdLine("");
        setDesc("");
    }

    @Override
    public String toString() {
        return this.getId();
    }

    @Override
    public String getId() {
        return taskId;
    }

    @Override
    public String getCmdLine() {
        return cmdLine;
    }

    @Override
    public String getDesc() {
        return taskDesc;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public List<String> getDependIdList() {
        return depIdList;
    }

    @Override
    public String getUrl() {
        return taskUrl;
    }

    @Override
    public TaskState getStatus() {
        return taskState;
    }

    @Override
    public TaskType getType() {
        return taskType;
    }

    @Override
    public ITask getGroup() {
        return groupTask;
    }

    @Override
    public Set<ITask> getDependTaskSet() {
        return depTaskSet;
    }

    @Override
    public List<ITask> getSubTaskList() {
        return subTaskList;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public void setId(String id) {
        taskId = id;
    }

    @Override
    public void setCmdLine(String cmd) {
        cmdLine = cmd;
    }

    @Override
    public void setDesc(String desc) {
        taskDesc = desc;
    }

    @Override
    public void setGroupId(String id) {
        groupId = id;
    }

    @Override
    public void addDependId(String id) {
        depIdList.add(id);
    }

    @Override
    public void setUrl(String url) {
        taskUrl = url;
    }

    @Override
    public void setStatus(TaskState status) {
        logger.info("state changed->task:[{}::{}] state:[{} -> {}]",
                instanceId, taskId, taskState, status);
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
                throw new RuntimeException(String.format("invalidate state task:[%s::%s] state:[%s]->[%s]",
                        instanceId, taskId, taskState, status));
            }
        }
    }

    @Override
    public void setType(TaskType type) {
        if (taskType != type) {
            taskType = type;
        }
    }

    @Override
    public void setGroup(ITask other) {
        groupTask = other;
    }

    @Override
    public void addDependTask(ITask other) {
        depTaskSet.add(other);
    }

    @Override
    public void addSubTask(ITask other) {
        subTaskList.add(other);
    }

    @Override
    public void setInstanceId(String newId) {
        instanceId = newId;
    }

}
