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
package pers.ebr.server.common.repository;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.Configs;

import java.io.IOException;

/**
 * <p>
 * 任务仓储
 * </p>
 *
 * @author l.gong
 */
public final class Repository {
    private final static Logger logger = LoggerFactory.getLogger(Repository.class);
    private IRepositoryManager mng;

    private static class Holder {
        static final Repository REPO = new Repository();
    }

    private Repository() {}

    /**
     * 初始化
     * @param config [in] 配置
     * @throws IOException 内置properties文件读取失败时
     * @throws RepositoryException 数据库处理错误时
     */
    public static void init(JsonObject config) throws IOException, RepositoryException {
        synchronized (Holder.REPO) {
            if (Holder.REPO.mng == null) {
                Holder.REPO.mng = Holder.REPO.build(config);
            }
            Holder.REPO.mng.init();
        }
        logger.info("Repository Init Success...");
    }

    /**
     * 仓储服务结束了
     * @throws RepositoryException 数据库处理错误时
     */
    public static void finish() throws RepositoryException {
        synchronized (Holder.REPO) {
            if (Holder.REPO.mng != null) {
                Holder.REPO.mng.finish();
            }
        }
    }

    /**
     * 获取数据库服务
     * @return IDatabase
     */
    public static IDatabase getDb() {
        if (Holder.REPO.mng == null) {
            throw new RuntimeException("database is not initialized...");
        }
        return Holder.REPO.mng.getDb();
    }

    /**
     * 获取对象池服务
     * @return IPool
     */
    public static IPool getPool() {
        if (Holder.REPO.mng == null) {
            throw new RuntimeException("pool is not initialized...");
        }
        return Holder.REPO.mng.getPool();
    }

    /**
     * 创建仓储管理器
     * @param config [in] 配置
     * @return IRepositoryManager
     * @throws IOException
     */
    IRepositoryManager build(JsonObject config) throws IOException {
        String type = config.getString(Configs.KEY_REPO_MODE, LocalRepositoryManager.TYPE);
        switch (type) {
            case LocalRepositoryManager.TYPE: {
                return new LocalRepositoryManager();
            }
            default: {
                logger.error("unknown db connection type:{}", type);
                throw new RuntimeException(String.format("unknown db connection type: %s", type));
            }
        }
    }

}
