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

import io.vertx.core.json.JsonObject;

import static pers.ebr.server.common.model.ITask.*;

/**
 * <pre>
 * TaskDetail Object
 *
 * </pre>
 *
 * @author l.gong
 */
public final class TaskDetail implements IDetail {
    String url;
    String id;
    String group;
    String cmd;
    String desc;
    String deps;

    TaskDetail() {}

    @Override
    public int type() {
        return IDetail.TASK;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDesc() {
        return desc;
    }

    public String getDeps() {
        return deps;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDeps(String deps) {
        this.deps = deps;
    }

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(TASK_URL, url);
        jsonObject.put(TASK_ID, id);
        jsonObject.put(TASK_GROUP, group);
        jsonObject.put(TASK_DESC, desc);
        jsonObject.put(TASK_CMD_LINE, cmd);
        jsonObject.put(TASK_DEPENDS_LIST, deps);
        return jsonObject;
    }
}
