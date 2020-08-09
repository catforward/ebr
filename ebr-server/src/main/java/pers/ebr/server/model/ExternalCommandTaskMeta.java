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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static pers.ebr.server.model.IExternalCommandTask.*;

/**
 * <p>
 * 任务(Task)的静态描述
 * 描述项目与JSON的定义一对一对应
 * </p>
 *
 * @author l.gong
 */
public final class ExternalCommandTaskMeta implements IObjectConverter {
    String id;
    String group;
    String cmd;
    String desc;
    final ArrayList<String> depends = new ArrayList<>();

    ExternalCommandTaskMeta() {}

    /**
     * 获取任务ID
     *
     * @return String
     */
    public String getId() {
        return id;
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
     * 获取任务所在组的ID
     *
     * @return String
     */
    public String getGroup() {
        return group;
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
     * 获取任务的目标命令
     *
     * @return String
     */
    public String getCmd() {
        return cmd;
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
     * 获取任务描述
     *
     * @return String
     */
    public String getDesc() {
        return desc;
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
     * 获取任务的依赖任务列表
     *
     * @return List
     */
    public List<String> getDepends() {
        return depends;
    }

    /**
     * 增加任务所依赖的其他任务Id定义
     *
     * @param id [in] 待设置其他任务Id
     */
    public void addDepends(String id) {
        this.depends.add(id);
    }

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject taskObject = new JsonObject();
        if (group != null && !group.isEmpty()) {
            taskObject.put(TASK_GROUP, group);
        }
        if (desc != null && !desc.isEmpty()) {
            taskObject.put(TASK_DESC, desc);
        }
        if (cmd != null && !cmd.isEmpty()) {
            taskObject.put(TASK_CMD_LINE, cmd);
        }
        if (!depends.isEmpty()) {
            JsonArray dep = new JsonArray(depends);
            taskObject.put(TASK_DEPENDS_LIST, dep);
        }
        return taskObject   ;
    }
}
