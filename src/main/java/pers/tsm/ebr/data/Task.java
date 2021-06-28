/**
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
package pers.tsm.ebr.data;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.types.TaskAttrEnum;
import pers.tsm.ebr.types.TaskStateEnum;
import pers.tsm.ebr.types.TaskTypeEnum;

/**
 *
 *
 * @author l.gong
 */
public class Task {

    static class Meta {
        String id;
        String group;
        String desc;
        List<String> depends;
        String cmd;

        private Meta() {
            depends = new ArrayList<>();
        }

        public static Meta buildFrom(String id, JsonObject obj) {
            requireNonNull(id);
            requireNonNull(obj);
            Meta meta = new Meta();
            meta.id = id;
            meta.group = obj.getString(TaskAttrEnum.GROUP.getName(), Symbols.BLANK_STR);
            meta.desc = obj.getString(TaskAttrEnum.DESC.getName(), Symbols.BLANK_STR);
            meta.cmd = obj.getString(TaskAttrEnum.COMMAND.getName(), Symbols.BLANK_STR);
            JsonArray array = obj.getJsonArray(TaskAttrEnum.DEPENDS.getName());
            if (!isNull(array) && !array.isEmpty()) {
                array.forEach(ids -> meta.depends.add((String) ids));
            }
            return meta;
        }
    }

    final Meta meta;
    String url;
    Task parent;
    List<Task> children;
    List<Task> depends;
    TaskTypeEnum type;
    volatile TaskStateEnum state;
    
    public Task(Meta meta) {
        this.meta = meta;
        this.children = new ArrayList<>();
        this.depends = new ArrayList<>();
        this.state = TaskStateEnum.UNKNOWN;
        this.type = TaskTypeEnum.TASK;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.url).append("\n");
        children.forEach(child -> str.append(child.toString()));
        return str.toString();
    }

    public String getUrl() {
        return this.url;
    }

    public TaskStateEnum getState() {
        return state;
    }

}
