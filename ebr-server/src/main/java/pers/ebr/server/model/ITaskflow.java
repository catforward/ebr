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

import java.util.Set;

/**
 * <p>
 * 工作流，描述有指定关系的任务的集合
 * </p>
 *
 * @author l.gong
 */
public interface ITaskflow extends IObjectConverter {

    /**
     * 工作流执行结束后释放资源
     */
    void release();

    /**
     * 启动工作流
     */
    void standby();

    /**
     * 判断此工作流中是否包含任务
     * @return boolean
     */
    boolean isEmpty();

    /**
     * 获取根任务
     * @return IExternalTask
     */
    IExternalCommandTask getRootTask();

    /**
     * 设置运行时实例ID
     * @param newId 待设置的实例ID
     */
    void setInstanceId(String newId);

    /**
     * 获取运行时实例ID
     * @return String
     */
    String getInstanceId();

    /**
     * 获取工作流的状态
     * @return TaskState
     */
    TaskState getState();

    /**
     * 获取所有任务的集合
     * @return Set
     */
    Set<IExternalCommandTask> getAllExternalTask();

    /**
     * 更新任务状态
     *
     * @param path 任务逻辑路径
     * @param newState 任务新状态
     */
    void setTaskState(String path, TaskState newState);

    /**
     * <p>
     * 当有满足执行条件的任务对象存在时，向接口实现者推送可执行的引用
     * </p>
     *
     * @param  appender IRunnableTaskAppender接口的实现者
     */
    void setRunnableTaskAppender(IRunnableTaskAppender appender);

    /**
     * <p>
     * 当有任务对象状态改变时，向接口实现者推送新状态
     * </p>
     *
     * @param  watcher ITaskStateWatcher接口的实现类
     */
    void setTaskStateWatcher(ITaskStateWatcher watcher);
}
