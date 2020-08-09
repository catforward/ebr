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
import pers.ebr.server.common.TaskState;
import pers.ebr.server.common.TaskType;

import java.util.HashSet;
import java.util.Set;

import static pers.ebr.server.common.TaskState.*;
import static pers.ebr.server.common.TaskState.FAILED;
import static pers.ebr.server.common.TaskType.UNIT;

/**
 * <p>
 * 任务(Task)的运行时属性
 * 包含以下属性
 * <ul>
 *     <li>任务的逻辑路径(url)</li>
 *     <li>任务的实例ID(instanceId)</li>
 *     <li>任务的状态(state)</li>
 *     <li>任务的类型(type)</li>
 *     <li>任务的所在组任务引用(groupTask)</li>
 *     <li>任务的依赖任务集合(depTaskSet)</li>
 *     <li>任务的子任务集合(subTaskList)</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
final class ExternalCommandTaskProp {
    private final static Logger logger = LoggerFactory.getLogger(ExternalCommandTaskProp.class);
    String path;
    String instanceId;
    volatile TaskState state = UNKNOWN;
    TaskType type = UNIT;

    ExternalCommandTask groupTask = null;
    final Set<ExternalCommandTask> depTaskSet = new HashSet<>();
    final Set<ExternalCommandTask> subTaskSet = new HashSet<>();

    ExternalCommandTaskProp() {}

    /**
     * 获取任务逻辑路径
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置任务的逻辑路径
     *
     * @param path [in] 待设置任务的逻辑路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取任务实例ID
     *
     * @return String
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 设置任务的实例ID
     *
     * @param instanceId [in] 待设置任务的实例ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * 获取任务状态
     *
     * @return TaskState
     */
    public TaskState getState() {
        return state;
    }

    /**
     * 设置任务状态
     *
     * @param newState [in] 待设置任务的状态
     */
    public void setState(TaskState newState) {
        switch (state) {
            case UNKNOWN: {
                if (INACTIVE == newState) {
                    state = newState;
                }
                break;
            }
            case INACTIVE: {
                if (ACTIVE == newState) {
                    state = newState;
                }
                break;
            }
            case ACTIVE: {
                if (COMPLETE == newState || FAILED == newState) {
                    state = newState;
                }
                break;
            }
            case COMPLETE:
            case FAILED:
            default: {
                throw new RuntimeException(String.format("invalidate state :[%s::%s] state:[%s]->[%s]",
                        instanceId, path, state, newState));
            }
        }
    }

    /**
     * 获取任务类型
     *
     * @return TaskType
     */
    public TaskType getType() {
        return type;
    }

    /**
     * 设置任务类型
     *
     * @param type [in] 待设置任务的类型
     */
    public void setType(TaskType type) {
        if (this.type != type) {
            this.type = type;
        }
    }

    /**
     * 获取任务的所在组
     *
     * @return ITask
     */
    public IExternalCommandTask getGroupTask() {
        return groupTask;
    }

    /**
     * 设置任务所在组
     *
     * @param groupTask [in] 待设置任务的所在组
     */
    public void setGroupTask(ExternalCommandTask groupTask) {
        this.groupTask = groupTask;
    }

    /**
     * 获取任务依赖任务集合
     *
     * @return Set
     */
    public Set<ExternalCommandTask> getDepTaskSet() {
        return depTaskSet;
    }

    /**
     * 添加任务的依赖任务
     *
     * @param task [in] 待设置任务的依赖任务
     */
    public void addDepTask(ExternalCommandTask task) {
        depTaskSet.add(task);
    }

    /**
     * 获取任务的子任务集合
     *
     * @return Set
     */
    public Set<ExternalCommandTask> getSubTaskSet() {
        return subTaskSet;
    }

    /**
     * 添加任务的子任务
     *
     * @param task [in] 待设置任务的子任务
     */
    public void addSubTask(ExternalCommandTask task) {
        subTaskSet.add(task);
    }

    void release() {
        depTaskSet.clear();
        subTaskSet.clear();
        groupTask = null;
    }

}
