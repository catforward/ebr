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
package pers.ebr.server.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static pers.ebr.server.repository.SqliteDatabaseImpl.*;

/**
 * <p>
 * 本地存储模式下的仓储管理器
 * </p>
 *
 * @author l.gong
 */
public class LocalRepositoryManager implements IRepositoryManager {
    private final static Logger logger = LoggerFactory.getLogger(LocalRepositoryManager.class);

    final static String TYPE = "local";
    final static String SQLITE_SCHEMA = "flows.db";
    final Properties tableVer = new Properties();
    final Properties sqlTpl = new Properties();
    final SqliteDatabaseImpl db;
    final MemoryPoolImpl pool;

    LocalRepositoryManager() throws IOException {
        try (
                InputStream tableVerFile = getClass().getResourceAsStream("/db_table_ver.properties");
                InputStream sqlFile = getClass().getResourceAsStream("/db_sql_tpl.properties")
        ) {
            tableVer.load(tableVerFile);
            sqlTpl.load(sqlFile);
            db = new SqliteDatabaseImpl(sqlTpl);
            pool = new MemoryPoolImpl();
        }
    }

    /**
     * 仓储初始化
     *
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public void init() throws RepositoryException {
        db.connect();
        initOrUpdateSchema();
        pool.init();
    }

    /**
     * 仓储结束
     *
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public void finish() throws RepositoryException {
        pool.close();
        db.release();
    }

    /**
     * 获取数据库存储服务
     * @return IDatabase
     */
    @Override
    public IDatabase getDb() {
        return db;
    }

    /**
     * 获取对象池服务
     *
     * @return IPool
     */
    @Override
    public IPool getPool() {
        return pool;
    }

    /**
     * 初始化数据库schema
     */
    private void initOrUpdateSchema() {
        try {
            this.tableVer.forEach((key, value) -> {
                // 前提：prop中定义的表版t本为最新版
                String tableName = String.format("%s_ver%s", key, value);
                try {
                    // 表的最新版不存在时
                    if (!db.isTableExist(tableName)) {
                        execTableCreate(key.toString(), tableName);
                    }
                } catch (SQLException | IOException ex) {
                    logger.error("create table error [{}]", tableName);
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            logger.error("db init error...", ex);
            throw ex;
        }
    }

    /**
     * 创建数据库表
     *
     * @param viewName      [in] 视图名
     * @param tableName     [in] 表名
     * @throws IOException  读取文件发生错误时
     * @throws SQLException 执行SQL语句发生错误时
     */
    private void execTableCreate(String viewName, String tableName) throws IOException, SQLException {
        execTableCreateTmplFile(String.format("DDL_%s.sql", tableName));
        execMigrationTmplFile(String.format("MIGRATE_%s.sql", viewName));
        execDropOldTable(viewName, tableName);
        execViewCreate(viewName, tableName);
    }

    /**
     * 读取SQL文件并执行其中的SQL语句来创建数据库表
     *
     * @param ddlFileName [in] DDL文件名
     *
     * @throws IOException  读取文件发生错误时
     * @throws SQLException 执行SQL语句发生错误时
     */
    private void execTableCreateTmplFile(String ddlFileName) throws IOException, SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", ddlFileName))) {
            db.execute(Utils.toString(is));
        }
    }

    /**
     * 读取SQL文件并执行其中的SQL语句来迁移数据库表
     *
     * @param ddlFileName [in] DDL文件名
     * @throws SQLException 执行SQL语句发生错误时
     */
    private void execMigrationTmplFile(String ddlFileName) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", ddlFileName))) {
            if (is != null) {
                db.execute(Utils.toString(is));
            } else {
                logger.info("skip migration of {}", ddlFileName);
            }
        } catch (IOException ex) {
            logger.error("table migration error...", ex);
            logger.info("skip migration of {}", ddlFileName);
        }
    }

    /**
     * 删除数据库表
     *
     * @param viewName      [in] 视图名
     * @param tableName     [in] 表名
     * @throws SQLException 执行SQL语句发生错误时
     */
    private void execDropOldTable(String viewName, String tableName) throws SQLException {
        String getTableNameSql = sqlTpl.getProperty(SQL_GET_TABLE_NAME);
        String dropTableSql = sqlTpl.getProperty(SQL_DROP_TABLE);
        List<Map<String, String>> ret = db.query(String.format(getTableNameSql, String.format("%s_ver", viewName) + "%"));
        for (var row : ret) {
            String tbl = row.get("name");
            if (!tbl.equalsIgnoreCase(tableName)) {
                db.execute(String.format(dropTableSql, tbl));
            }
        }
    }

    /**
     * 创建视图
     *
     * @param viewName      [in] 视图名
     * @param tableName     [in] 表名
     * @throws SQLException 执行SQL语句发生错误时
     */
    private void execViewCreate(String viewName, String tableName) throws SQLException {
        if (db.isViewExist(viewName)) {
            String dropViewSql = sqlTpl.getProperty(SQL_DROP_VIEW);
            db.execute(String.format(dropViewSql, viewName));
        }
        String createViewSql = sqlTpl.getProperty(SQL_CREATE_VIEW);
        db.execute(String.format(createViewSql, viewName, tableName));
    }

}
