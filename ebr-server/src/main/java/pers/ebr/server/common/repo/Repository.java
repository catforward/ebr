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
package pers.ebr.server.common.repo;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.Configs;

import java.io.IOException;

/**
 * <pre>
 * The Database Storage utility
 * </pre>
 *
 * @author l.gong
 */
public final class Repository {
    private final static Logger logger = LoggerFactory.getLogger(Repository.class);
    private IRepositoryManager mng;

    private static class Holder {
        static final Repository INSTANCE = new Repository();
    }

    private Repository() {}

    public static void init(JsonObject config) throws IOException, RepositoryException {
        synchronized (Holder.INSTANCE) {
            if (Holder.INSTANCE.mng == null) {
                Holder.INSTANCE.mng = Holder.INSTANCE.build(config);
            }
            Holder.INSTANCE.mng.init();
        }
        logger.info("DBStore Init Success...");
    }

    public static void finish() throws RepositoryException {
        synchronized (Holder.INSTANCE) {
            if (Holder.INSTANCE.mng != null) {
                Holder.INSTANCE.mng.finish();
            }
        }
    }

    public static IRepository get() {
        if (Holder.INSTANCE.mng == null) {
            throw new RuntimeException("database is not initialized...");
        }
        return Holder.INSTANCE.mng.getRepository();
    }

    IRepositoryManager build(JsonObject config) throws IOException {
        String type = config.getString(Configs.KEY_MANAGER_REPO, SQLiteRepositoryManager.TYPE);
        switch (type) {
            case SQLiteRepositoryManager.TYPE : {
                return new SQLiteRepositoryManager();
            }
            default: {
                logger.error("unknown db connection type:{}", type);
                throw new RuntimeException(String.format("unknown db connection type: %s", type));
            }
        }
    }

}
