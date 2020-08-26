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

import pers.ebr.server.common.TaskState;
import pers.ebr.server.common.TaskType;
import pers.ebr.server.common.graph.DirectedGraph;

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
 *     <li>任务的所在组DAG图引用(groupGraph)</li>
 *     <li>任务的依赖任务集合(depTaskSet)</li>
 *     <li>任务的子任务集合(subTaskSet)</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
final class ExternalCommandTaskProp {
    /** 任务逻辑路径 */
    String path;
    /** 任务实例ID */
    String instanceId;
    /** 任务状态 */
    private volatile TaskState state = UNKNOWN;
    /** 任务类型 */
    private TaskType type = UNIT;
    /** 当任务类型为GROUP时，存在DAG图数据结构 */
    private DirectedGraph<ExternalCommandTask> graph;

    /** 任务的所在组 */
    ExternalCommandTask groupTask;
    /** 任务依赖任务集合 */
    final Set<ExternalCommandTask> depTaskSet = new HashSet<>();
    /** 任务的子任务集合 */
    final Set<ExternalCommandTask> subTaskSet = new HashSet<>();

    ExternalCommandTaskProp() {}

    /**
     * 获取任务状态
     * @return TaskState
     */
    TaskState getState() {
        return state;
    }

    /**
     * 设置任务状态
     * @param newState [in] 待设置任务的状态
     */
    void setState(TaskState newState) {
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
     * @return TaskType
     */
    TaskType getType() {
        return type;
    }

    /**
     * 设置任务类型
     * @param type [in] 待设置任务的类型
     */
    void setType(TaskType type) {
        if (this.type != type) {
            this.type = type;
        }
    }

    /**
     * 获取任务的DAG图数据结构
     * @return DirectedGraph
     */
    public DirectedGraph<ExternalCommandTask> getGraph() {
        return graph;
    }

    /**
     * 设置任务的DAG图数据结构
     * @param graph [in] 待设置任务的DAG图数据结构
     */
    public void setGraph(DirectedGraph<ExternalCommandTask> graph) {
        this.graph = graph;
    }


    void release() {
        depTaskSet.clear();
        subTaskSet.clear();
        groupTask = null;
    }

}
