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
package pers.ebr.server.base.db;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <pre>
 * The Database Storage utility
 * </pre>
 *
 * @author l.gong
 */
public final class DBStore {
    private final static Logger logger = LoggerFactory.getLogger(DBStore.class);
    private DBManager mng;

    private static class DBStoreHolder {
        static final DBStore STORE = new DBStore();
    }

    private DBStore() {}

    public static void init(JsonObject config) throws IOException, DBException {
        synchronized (DBStoreHolder.STORE) {
            if (DBStoreHolder.STORE.mng == null) {
                DBStoreHolder.STORE.mng = new DBBuilder(config).build();
            }
            DBStoreHolder.STORE.mng.init();
        }
        logger.info("DBStore Init Success...");
    }

    public static void finish() throws DBException {
        synchronized (DBStoreHolder.STORE) {
            if (DBStoreHolder.STORE.mng != null) {
                DBStoreHolder.STORE.mng.finish();
            }
        }
    }

    public static DBConnection getConnection() {
        if (DBStoreHolder.STORE.mng == null) {
            throw new RuntimeException("database is not initialized...");
        }
        return DBStoreHolder.STORE.mng.getConnection();
    }

}
