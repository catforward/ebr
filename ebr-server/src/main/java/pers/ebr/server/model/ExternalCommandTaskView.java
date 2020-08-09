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

import io.vertx.core.json.JsonObject;
import pers.ebr.server.common.TaskState;

import static pers.ebr.server.model.IExternalCommandTask.*;
import static pers.ebr.server.common.TaskState.INACTIVE;

/**
 * <p>
 * 任务(Task)的视图数据，与客户端交互时使用
 * </p>
 *
 * @author l.gong
 */
public final class ExternalCommandTaskView implements IObjectConverter {
    String path;
    String id;
    String group;
    String cmd;
    String desc;
    String deps;
    TaskState state = INACTIVE;

    ExternalCommandTaskView() {}

    /**
     * 获取任务逻辑路径
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取任务ID
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * 获取任务所在组的ID
     *
     * @return String
     */
    public String getGroup() {
        return group;
    }

    /**
     * 获取任务的目标命令
     *
     * @return String
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * 获取任务描述
     *
     * @return String
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 获取任务的依赖任务列表
     *
     * @return String
     */
    public String getDepends() {
        return deps;
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
     * 设置任务的逻辑路径
     *
     * @param path [in] 待设置任务的逻辑路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 设置任务ID
     *
     * @param id [in] 待设置任务id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 设置任务所在组的Id
     *
     * @param group [in] 待设置所在组的Id
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 设置任务目标命令
     *
     * @param cmd [in] 待设置命令行字符
     */
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    /**
     * 设置任务描述
     *
     * @param desc [in] 待设置任务描述
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 增加任务所依赖的其他任务Id定义
     *
     * @param id [in] 待设置其他任务Id
     */
    public void setDepends(String id) {
        this.deps = id;
    }

    /**
     * 设置任务状态
     *
     * @param newState [in] 待设置任务的状态
     */
    public void setState(TaskState newState) {
        this.state = newState;
    }

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(TASK_PATH, path);
        jsonObject.put(TASK_ID, id);
        jsonObject.put(TASK_GROUP, group);
        jsonObject.put(TASK_DESC, desc);
        jsonObject.put(TASK_CMD_LINE, cmd);
        jsonObject.put(TASK_DEPENDS_LIST, deps);
        jsonObject.put(TASK_STATE, state.ordinal());
        return jsonObject;
    }
}
