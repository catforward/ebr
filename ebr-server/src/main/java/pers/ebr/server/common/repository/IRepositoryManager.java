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

/**
 * <p>
 * 仓储管理
 * </p>
 *
 * @author l.gong
 */
interface IRepositoryManager {

    /**
     * 仓储初始化
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    void init() throws RepositoryException;

    /**
     * 仓储结束
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    void finish() throws RepositoryException;

    /**
     * 获取数据库存储服务
     * @return IDatabase
     */
    IDatabase getDb();

    /**
     * 获取对象池服务
     * @return IPool
     */
    IPool getPool();
}
