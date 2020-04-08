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
package pers.ebr.server.base.pool;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The TaskStore Interface
 *
 * @author l.gong
 */
public class TaskStore {

    private final static Logger logger = LoggerFactory.getLogger(TaskStore.class);
    private TaskPool pool;

    private static class TaskStoreHolder {
        static final TaskStore STORE = new TaskStore();
    }

    private TaskStore() {}

    public static void init(JsonObject config) {
        if (TaskStoreHolder.STORE.pool == null) {
            TaskStoreHolder.STORE.pool = new TaskPoolBuilder(config).build();
        }
        System.out.println("TaskStore Init Success...");
    }

    public static void finish() {
        if (TaskStoreHolder.STORE.pool != null) {
            // TODO
        }
    }

    public Optional<ModifiableTaskPool> getModifiableTaskPool() {
        return Optional.of((ModifiableTaskPool) TaskStoreHolder.STORE.pool);
    }

    public Optional<ReadOnlyTaskPool> getReadOnlyTaskPool() {
        return Optional.of((ReadOnlyTaskPool) TaskStoreHolder.STORE.pool);
    }
}
