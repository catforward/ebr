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

/**
 * <p>
 * 向本接口的实现者推送任务的新旧状态信息
 * </p>
 *
 * @author l.gong
 */
public interface ITaskStateWatcher {

    /**
     * 推送任务新状态信息
     * @param instanceId 任务所在工作流实例ID
     * @param path       任务逻辑路径
     * @param isRootTask 是否是根任务
     * @param src        旧状态
     * @param dst        新状态
     */
    void onStateChanged(String instanceId, String path, boolean isRootTask, TaskState src, TaskState dst);
}
