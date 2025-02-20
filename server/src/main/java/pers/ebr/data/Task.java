/*
  Copyright 2021 liang gong

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
package pers.ebr.data;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.ebr.base.AppSymbols;
import pers.ebr.base.AppException;
import pers.ebr.base.StringUtils;
import pers.ebr.types.TaskAttrEnum;
import pers.ebr.types.TaskStateEnum;
import pers.ebr.types.TaskTypeEnum;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>
 * Flow:
 * STORED  --> STANDBY(READY)
 * STANDBY --> RUNNING
 *         --> PAUSED
 *         --> SKIPPED
 *         --> ABORTED
 * RUNNING --> FINISHED
 *         --> ERROR
 * PAUSED  --> STANDBY(RESTART)
 *         --> ABORTED
 * ERROR   --> STANDBY(RETRY)
 *         --> ABORTED
 * FINISHED--> (X)
 * SKIPPED --> ABORTED
 * ABORTED --> (X)
 * 
 * </pre>
 *
 * @author l.gong
 */
public class Task {

    static class Meta {
        String id;
        String cron;
        String group;
        String desc;
        final List<String> depends;
        String script;

        private Meta() {
            depends = new ArrayList<>();
        }

        public static Meta buildFrom(String id, JsonObject taskBody) {
            requireNonNull(id);
            requireNonNull(taskBody);
            Meta meta = new Meta();
            meta.id = id;
            meta.cron = taskBody.getString(TaskAttrEnum.CRON.getName(), null);
            meta.group = taskBody.getString(TaskAttrEnum.GROUP.getName(), AppSymbols.BLANK_STR);
            meta.desc = taskBody.getString(TaskAttrEnum.DESC.getName(), AppSymbols.BLANK_STR);
            meta.script = taskBody.getString(TaskAttrEnum.SCRIPT.getName(), AppSymbols.BLANK_STR);
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
    final List<Task> children;
    final List<Task> predecessor;
    final List<Task> successor;
    TaskTypeEnum type;
    volatile TaskStateEnum state;

    public Task(Meta meta) {
        this.meta = meta;
        this.url = AppSymbols.BLANK_STR;
        this.root = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.predecessor = new ArrayList<>();
        this.successor = new ArrayList<>();
        this.type = TaskTypeEnum.TASK;
        this.state = TaskStateEnum.STORED;
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

    public String getScript() {
        return meta.script;
    }

    public String getCronStr() {
        return meta.cron;
    }

    public TaskTypeEnum getType() {
        return type;
    }

    public void reset() {
        synchronized(this) {
            state = TaskStateEnum.STORED;
        }
    }

    public void standby() {
    	synchronized(this) {
            state = TaskStateEnum.STANDBY;
        }
    }

    public synchronized void updateState(TaskStateEnum newState) {
        switch (state) {
            case STANDBY: {
                if (TaskStateEnum.RUNNING == newState
                        || TaskStateEnum.PAUSED == newState
                        || TaskStateEnum.SKIPPED == newState
                        || TaskStateEnum.ABORTED == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                return;
            }
            case RUNNING: {
                if (TaskStateEnum.FINISHED == newState
                        || TaskStateEnum.ERROR == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                return;
            }
            case STORED: {
                if (TaskStateEnum.STANDBY == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                return;
            }
            case PAUSED:
            case ERROR: {
                if (TaskStateEnum.STANDBY == newState
                        || TaskStateEnum.ABORTED == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                return;
            }
            case SKIPPED:{
                if (TaskStateEnum.ABORTED == newState) {
                    state = newState;
                } else {
                    raiseStateException(newState);
                }
                return;
            }
            case FINISHED:
            case ABORTED:
            default: {
                // do nothing
                break;
            }
        }
    }

    private void raiseStateException(TaskStateEnum newState) {
        throw new AppException(String.format("invalidate state :[%s] state:[%s]->[%s]",
                url, state.getName(), newState.getName()));
    }

}
