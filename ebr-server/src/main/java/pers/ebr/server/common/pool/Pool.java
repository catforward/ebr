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
package pers.ebr.server.common.pool;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.Configs;

/**
 * The Task Scheduler Interface
 *
 * @author l.gong
 */
public final class Pool {
    private final static Logger logger = LoggerFactory.getLogger(Pool.class);
    private IPool pool;

    private static class Holder {
        static final Pool INSTANCE = new Pool();
    }

    private Pool() {}

    public static void init(JsonObject config) {
        synchronized (Holder.INSTANCE) {
            if (Holder.INSTANCE.pool == null) {
                Holder.INSTANCE.pool = Holder.INSTANCE.build(config);
            }
            Holder.INSTANCE.pool.init();
        }
        logger.info("TaskStore Init Success...");
    }

    public static void finish() {
        synchronized (Holder.INSTANCE) {
            if (Holder.INSTANCE.pool != null) {
                Holder.INSTANCE.pool.close();
            }
        }
    }

    public static IPool get() {
        return Holder.INSTANCE.pool;
    }

    IPool build(JsonObject config) {
        String type = config.getString(Configs.KEY_EXECUTOR_POOL_TYPE, InMemoryPoolImpl.TYPE);
        switch (type) {
            case InMemoryPoolImpl.TYPE : {
                return new InMemoryPoolImpl();
            }
            default: {
                logger.error("unknown task pool type:{}", type);
                throw new RuntimeException(String.format("unknown task pool type: %s", type));
            }
        }
    }

}
