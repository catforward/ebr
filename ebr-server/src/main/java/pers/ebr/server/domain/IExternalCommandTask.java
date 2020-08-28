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
package pers.ebr.server.domain;

import pers.ebr.server.common.TaskState;
import pers.ebr.server.common.TaskType;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 任务实体的定义
 * </p>
 *
 * @author l.gong
 */
public interface IExternalCommandTask {

    String TASK_ID = "id";
    String TASK_GROUP = "group";
    String TASK_DESC = "desc";
    String TASK_CMD_LINE = "cmd";
    String TASK_DEPENDS_LIST = "depends";
    String TASK_PATH = "path";
    String TASK_STATE = "state";

    /**
     * 获取任务ID
     * @return String
     */
    String getId();

    /**
     * 获取任务的目标命令
     * @return String
     */
    String getCmdLine();

    /**
     * 获取任务描述
     * @return String
     */
    String getDesc();

    /**
     * 获取任务所在组的ID
     * @return String
     */
    String getGroupId();

    /**
     * 获取任务的依赖任务列表
     * @return List
     */
    List<String> getDepends();

    /**
     * 获取任务运行时url
     * @return String
     */
    String getPath();

    /**
     * 获取任务运行时状态
     * @return TaskState
     */
    TaskState getState();

    /**
     * 获取任务运行时类型
     * @return TaskType
     */
    TaskType getType();

    /**
     * 获取任务依赖任务集合
     * @return Set
     */
    Set<? extends IExternalCommandTask> getDependTaskSet();

    /**
     * 获取任务的子任务集合
     * @return Set
     */
    Set<? extends IExternalCommandTask> getSubTaskSet();

    /**
     * 获取任务的运行时实例Id
     * @return String
     */
    String getInstanceId();

    /**
     * 判断是否是根任务
     * @return boolean
     */
    boolean isRootTask();

    /**
     * 释放任务内容
     */
    void release();

}
