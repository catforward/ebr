/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.tsm.ebr.data;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.common.StringUtils;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.types.TaskAttrEnum;
import pers.tsm.ebr.types.TaskStateEnum;
import pers.tsm.ebr.types.TaskTypeEnum;

/**
 * <pre>
 * Flow启动后
 * UNKNOWN --> STANDBY
 * STANDBY --> RUNNING
 *         --> PAUSED
 *         --> SKIPPED
 * RUNNING --> FINISHED
 *         --> ERROR
 * PAUSED  --> STANDBY(RESTART)
 * ERROR   --> STANDBY(RERUN)
 * FINISHED--> (X)
 * SKIPPED --> (X)
 * 
 * </pre>
 *
 * @author l.gong
 */
public class Task {

    static class Meta {
        String id;
        String group;
        String desc;
        List<String> depends;
        String script;

        private Meta() {
            depends = new ArrayList<>();
        }

        public static Meta buildFrom(String id, JsonObject taskBody) {
            requireNonNull(id);
            requireNonNull(taskBody);
            Meta meta = new Meta();
            meta.id = id;
            meta.group = taskBody.getString(TaskAttrEnum.GROUP.getName(), Symbols.BLANK_STR);
            meta.desc = taskBody.getString(TaskAttrEnum.DESC.getName(), Symbols.BLANK_STR);
            meta.script = taskBody.getString(TaskAttrEnum.SCRIPT.getName(), Symbols.BLANK_STR);
            meta.script = StringUtils.warpIfEmbedScriptPath(meta.script);
            JsonArray array = taskBody.getJsonArray(TaskAttrEnum.DEPENDS.getName());
            if (!isNull(array) && !array.isEmpty()) {
                array.forEach(ids -> meta.depends.add((String) ids));
            }
            return meta;
        }
    }

    // meta data
    final Meta meta;
    // runtime prop
    String url;
    Task root;
    Task parent;
    List<Task> children;
    List<Task> predecessor;
    List<Task> successor;
    TaskTypeEnum type;
    volatile TaskStateEnum state;

    public Task(Meta meta) {
        this.meta = meta;
        this.url = Symbols.BLANK_STR;
        this.root = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.predecessor = new ArrayList<>();
        this.successor = new ArrayList<>();
        this.type = TaskTypeEnum.TASK;
        this.state = TaskStateEnum.UNKNOWN;
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

    public Task getRoot() {
        return this.root;
    }

    public Task getParent() {
        return this.parent;
    }

    public TaskStateEnum getState() {
        return state;
    }

    public List<Task> getChildren() {
        return children;
    }

    public List<Task> getPredecessor() {
        return predecessor;
    }

    public List<Task> getSuccessor() {
        return successor;
    }

    public String getCommandLine() {
        return meta.script;
    }

    public TaskTypeEnum getType() {
        return type;
    }

    public void reset() {
        synchronized(this) {
            state = TaskStateEnum.UNKNOWN;
        }
    }

    public void standby() {
    	synchronized(this) {
            state = TaskStateEnum.STANDBY;
        }
    }

    public void updateState(TaskStateEnum newState) {
        switch (state) {
            case UNKNOWN: {
                if (TaskStateEnum.STANDBY == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                break;
            }
            case STANDBY: {
                if (TaskStateEnum.RUNNING == newState
                        || TaskStateEnum.PAUSED == newState
                        || TaskStateEnum.SKIPPED == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                break;
            }
            case RUNNING: {
                if (TaskStateEnum.FINISHED == newState
                        || TaskStateEnum.ERROR == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                break;
            }
            case PAUSED:
            case ERROR: {
                if (TaskStateEnum.STANDBY == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                break;
            }
            case SKIPPED:
            case FINISHED:
            default: raiseStateException(newState);
        }
    }

    private void raiseStateException(TaskStateEnum newState) {
        throw new AppException(String.format("invalidate state :[%s] state:[%s]->[%s]",
                url, state.getName(), newState.getName()));
    }

}
