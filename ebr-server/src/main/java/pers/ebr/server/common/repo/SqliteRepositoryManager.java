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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static pers.ebr.server.common.repo.SqliteRepositoryConst.*;

/**
 * <pre>
 * The SQLite RepositoryManager
 * </pre>
 *
 * @author l.gong
 */
final class SqliteRepositoryManager implements IRepositoryManager {
    private final static Logger logger = LoggerFactory.getLogger(SqliteRepositoryManager.class);
    final static String TYPE = "sqlite";
    final static String SCHEMA = "flows.db";
    final Properties tableVer = new Properties();
    final Properties sqlTpl = new Properties();
    final SqliteRepositoryImpl db;

    SqliteRepositoryManager() throws IOException {
        try (
            InputStream tableVerFile = getClass().getResourceAsStream("/db_table_ver.properties");
            InputStream sqlFile = getClass().getResourceAsStream("/db_sql_tpl.properties")
        ) {
            tableVer.load(tableVerFile);
            sqlTpl.load(sqlFile);
            db = new SqliteRepositoryImpl(sqlTpl);
        }
    }

    @Override
    public void init() throws RepositoryException {
        if (db != null) {
            db.connect();
            initOrUpdateSchema();
        }
    }

    @Override
    public void finish() throws RepositoryException {
        if (db != null) {
            db.release();
        }
    }

    @Override
    public IRepository getRepository() {
        return db;
    }

    private void initOrUpdateSchema() {
        try {
            this.tableVer.forEach((key, value) -> {
                // 前提：prop中定义的表版本为最新版
                String fullTableName = String.format("%s_ver%s", key, value);
                try {
                    // 表的最新版不存在时
                    if (!db.isTableExist(fullTableName)) {
                        execTableCreate(key.toString(), fullTableName);
                    }
                } catch (SQLException | IOException ex) {
                    logger.error("create table error [{}]", fullTableName);
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            logger.error("db init error...", ex);
            throw ex;
        }
    }

    private void execTableCreate(String tableName, String fullTableName) throws IOException, SQLException {
        execTableCreateTmplFile(String.format("DDL_%s.sql", fullTableName));
        execMigrationTmplFile(String.format("MIGRATE_%s.sql", tableName));
        execDropOldTable(tableName, fullTableName);
        execViewCreate(tableName, fullTableName);
    }

    private void execTableCreateTmplFile(String fileName) throws IOException, SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", fileName))) {
            db.execute(Utils.toString(is));
        }
    }

    private void execMigrationTmplFile(String fileName) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", fileName))) {
            if (is != null) {
                db.execute(Utils.toString(is));
            } else {
                logger.info("skip migration of {}", fileName);
            }
        } catch (IOException ex) {
            logger.error("table migration error...", ex);
            logger.info("skip migration of {}", fileName);
        }
    }

    private void execDropOldTable(String tableName, String fullTableName) throws SQLException {
        String getTableNameSql = sqlTpl.getProperty(GET_TABLE_NAME);
        String dropTableSql = sqlTpl.getProperty(DROP_TABLE);
        List<Map<String, String>> ret = db.query(String.format(getTableNameSql, String.format("%s_ver", tableName) + "%"));
        for (var row : ret) {
            String tbl = row.get("name");
            if (!tbl.equalsIgnoreCase(fullTableName)) {
                db.execute(String.format(dropTableSql, tbl));
            }
        }
    }

    private void execViewCreate(String viewName, String fullTableName) throws SQLException {
        if (db.isViewExist(viewName)) {
            String dropViewSql = sqlTpl.getProperty(DROP_VIEW);
            db.execute(String.format(dropViewSql, viewName));
        }
        String createViewSql = sqlTpl.getProperty(CREATE_VIEW);
        db.execute(String.format(createViewSql, viewName, fullTableName));
    }

}
