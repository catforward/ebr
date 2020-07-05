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

import java.util.List;
import java.util.Set;

/**
 * <pre>
 * Task Entity in EBR
 * </pre>
 *
 * @author l.gong
 */
public interface ITask {

    String TASK_ID = "id";
    String TASK_GROUP = "group";
    String TASK_DESC = "desc";
    String TASK_CMD_LINE = "cmd";
    String TASK_DEPENDS_LIST = "depends";

    String getId();
    String getCmdLine();
    String getDesc();
    String getGroupId();
    List<String> getDependIdList();

    void setId(String id);
    void setCmdLine(String cmd);
    void setDesc(String desc);
    void setGroupId(String id);
    void addDependId(String id);

    String getUrl();
    TaskState getStatus();
    TaskType getType();
    ITask getGroup();
    Set<ITask> getDependTaskSet();
    List<ITask> getSubTaskList();
    String getInstanceId();

    void setUrl(String url);
    void setStatus(TaskState status);
    void setType(TaskType type);
    void setGroup(ITask other);
    void addDependTask(ITask other);
    void addSubTask(ITask other);
    void setInstanceId(String newId);
    boolean isRootTask();

    void release();

}
