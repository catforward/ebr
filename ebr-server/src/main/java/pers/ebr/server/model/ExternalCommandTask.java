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

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 封装任务对象
 * 一个任务对象由以下部分组合而成
 * <ul>
 *     <li>描述任务的静态属性(ExternalTaskMeta)</li>
 *     <li>描述运行时任务的属性，与其他任务之间关系(ExternalTaskProp)</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
public final class ExternalCommandTask implements IExternalCommandTask {
    final ExternalCommandTaskMeta meta = new ExternalCommandTaskMeta();
    final ExternalCommandTaskProp prop = new ExternalCommandTaskProp();

    ExternalCommandTask(String id) {
        meta.setId(id);
    }

    /**
     * 获取任务ID
     *
     * @return String
     */
    @Override
    public String getId() {
        return meta.getId();
    }

    /**
     * 获取任务的目标命令
     *
     * @return String
     */
    @Override
    public String getCmdLine() {
        return meta.getCmd();
    }

    /**
     * 获取任务描述
     *
     * @return String
     */
    @Override
    public String getDesc() {
        return meta.getDesc();
    }

    /**
     * 获取任务所在组的ID
     *
     * @return String
     */
    @Override
    public String getGroupId() {
        return meta.getGroup();
    }

    /**
     * 获取任务的依赖任务列表
     *
     * @return List
     */
    @Override
    public List<String> getDepends() {
        return meta.getDepends();
    }

    /**
     * 获取任务运行时url
     *
     * @return String
     */
    @Override
    public String getPath() {
        return prop.getPath();
    }

    /**
     * 获取任务运行时状态
     *
     * @return TaskState
     */
    @Override
    public TaskState getState() {
        return prop.getState();
    }

    /**
     * 获取任务运行时类型
     *
     * @return TaskType
     */
    @Override
    public TaskType getType() {
        return prop.getType();
    }

    /**
     * 获取任务依赖任务集合
     *
     * @return Set
     */
    @Override
    public Set<? extends IExternalCommandTask> getDependTaskSet() {
        return prop.getDepTaskSet();
    }

    /**
     * 获取任务的子任务集合
     *
     * @return Set
     */
    @Override
    public Set<? extends IExternalCommandTask> getSubTaskSet() {
        return prop.getSubTaskSet();
    }

    /**
     * 获取任务的运行时实例Id
     *
     * @return String
     */
    @Override
    public String getInstanceId() {
        return prop.getInstanceId();
    }

    /**
     * 判断是否是根任务
     *
     * @return boolean
     */
    @Override
    public boolean isRootTask() {
        return meta.getId() == null || meta.getId().strip().isEmpty() || meta.getId().equalsIgnoreCase(meta.getGroup());
    }

    /**
     * 释放任务内容
     */
    @Override
    public void release() {
        prop.release();
    }
}
