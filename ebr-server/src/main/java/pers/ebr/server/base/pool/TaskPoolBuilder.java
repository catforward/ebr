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
import pers.ebr.server.base.Configs;

/**
 * The Builder of TaskPool
 *
 * @author l.gong
 */
class TaskPoolBuilder {
    private final static Logger logger = LoggerFactory.getLogger(TaskPoolBuilder.class);
    private final JsonObject config;

    TaskPoolBuilder(JsonObject config) {
        this.config = config;
    }

    TaskPool build() {
        String type = config.getString(Configs.KEY_REPO_POOL, InMemoryTaskPool.TYPE);
        switch (type) {
            case InMemoryTaskPool.TYPE : {
                return new InMemoryTaskPool(this).init();
            }
            default: {
                logger.error("unknown task pool type:{}", type);
                throw new RuntimeException(String.format("unknown task pool type: %s", type));
            }
        }
    }
}
