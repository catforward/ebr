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
package pers.ebr.cli.core.tasks;

import pers.ebr.cli.core.EbrException;

import java.util.ArrayList;

import static pers.ebr.cli.core.tasks.TaskState.*;
import static pers.ebr.cli.core.tasks.TaskType.UNIT;

/**
 * <pre>
 * The Task Class
 * </pre>
 *
 * @author l.gong
 */
public class Task {
    // basic prop
    final String id;
    String desc;
    String command;
    final ArrayList<String> depends = new ArrayList<>();
    // management prop
    final String parentId;
    final String location;
    final String url;
    TaskType type;
    TaskState state;

    Task(String id, String location, String parentId) {
        this.id = id;
        this.location = location;
        this.parentId = parentId;
        this.type = UNIT;
        this.state = INACTIVE;
        this.url = String.format("/".equals(location) ? "%s%s" : "%s/%s", location, id);
    }

    void updateState(TaskState newState) {
        switch (state) {
            case INACTIVE: {
                if (ACTIVE == newState) {
                    state = newState;
                }
                break;
            }
            case ACTIVE: {
                if (COMPLETE == newState || FAILED == newState) {
                    state = newState;
                }
                break;
            }
            case COMPLETE:
            case FAILED:
            default: {
                throw new EbrException(String.format("invalidate state task:[%s] state:[%s]->[%s]", id, state, newState));
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getCommand() {
        return command;
    }
}
