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

import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskStateEnum;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>task's flow</pre>
 *
 * @author l.gong
 */
public class Flow {
    final Task root;
    final Map<String, Task> urlTaskMapping;

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

    public void standby() {
        this.root.standby();
        this.urlTaskMapping.forEach((url, task) -> task.standby());
    }

}
