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

import com.cronutils.model.Cron;
import pers.ebr.base.AppConfigs;
import pers.ebr.base.AppException;
import pers.ebr.types.ResultEnum;
import pers.ebr.types.TaskStateEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>Task's flow</pre>
 *
 * @author l.gong
 */
public class Flow {
    final Task root;
    final Map<String, Task> urlTaskMapping;
    private Cron cron;
    private LocalDateTime latestResetDateTime;

    Flow(Task root) {
        this.root = root;
        this.urlTaskMapping = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(root.url).append("\n");
        root.children.forEach(child -> str.append(child.toString()));
        return str.toString();
    }

    public Task getRootTask() {
        return this.root;
    }

    public String getUrl() {
        requireNonNull(this.root);
        return this.root.getUrl();
    }

    public TaskStateEnum getState() {
        if (isNull(this.root)) {
            throw new AppException(ResultEnum.ERROR);
        }
        return this.root.state;
    }

    public Task getTask(String url) {
        if (this.root.getUrl().equals(url)) {
            return this.root;
        } else {
            return this.urlTaskMapping.get(url);
        }
    }

    public LocalDateTime getLatestResetDateTime() {
        return this.latestResetDateTime;
    }

    public Cron getCron() {
        return this.cron;
    }

    public void setCron(Cron cron) {
        this.cron = cron;
    }

    public Map<String, Task> getAllTaskRef() {
        return this.urlTaskMapping;
    }

    public void standby() {
        this.root.standby();
        this.urlTaskMapping.forEach((url, task) -> task.standby());
    }

    public void reset() {
        this.root.reset();
        this.urlTaskMapping.forEach((url, task) -> task.reset());
        this.latestResetDateTime = LocalDateTime.now(AppConfigs.getZoneId());
    }

    public void abort() {
        this.urlTaskMapping.forEach((url, task) -> {
            TaskStateEnum taskState = task.getState();
            if (TaskStateEnum.STANDBY == taskState
                    || TaskStateEnum.PAUSED == taskState
                    || TaskStateEnum.ERROR == taskState
                    || TaskStateEnum.SKIPPED == taskState) {
                task.updateState(TaskStateEnum.ABORTED);
            }
        });
    }

}
