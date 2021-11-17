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
package pers.ebr.base;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import pers.ebr.data.Flow;
import pers.ebr.data.Task;

import static java.util.Objects.isNull;

/**
 * <pre>Service's worker</pre>
 *
 * @author l.gong
 */
public class BaseVerticle extends AbstractVerticle {

    protected void emitMsg(String msg, JsonObject param) {
        vertx.eventBus().publish(msg, param);
    }

    protected void notice(String msg, Task task) {
        String flowUrl = isNull(task.getRoot()) ? task.getUrl() : task.getRoot().getUrl();
        JsonObject param = new JsonObject();
        param.put(AppSymbols.FLOW, flowUrl);
        param.put(AppSymbols.TASK, task.getUrl());
        emitMsg(msg, param);
    }

    protected void notice(String msg, Flow flow) {
        JsonObject param = new JsonObject();
        param.put(AppSymbols.FLOW, flow.getUrl());
        emitMsg(msg, param);
    }
}
