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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.base.MiscUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import static pers.ebr.server.constant.DBConst.CREATE_VIEW;
import static pers.ebr.server.constant.DBConst.DROP_VIEW;

/**
 * <pre>
 * The SQLite SQLiteDBManager
 * </pre>
 *
 * @author l.gong
 */
class SQLiteDBManager implements DBManager {
    private final static Logger logger = LoggerFactory.getLogger(SQLiteDBManager.class);
    final static String TYPE = "sqlite";
    final static String SCHEMA = "ebr.dat";
    final Properties tableVer = new Properties();
    final Properties sqlTpl = new Properties();
    final SQLiteDBConnection db;

    SQLiteDBManager() throws IOException {
        try (
            InputStream tableVerFile = getClass().getResourceAsStream("/db_table_ver.properties");
            InputStream sqlFile = getClass().getResourceAsStream("/db_sql_tpl.properties");
        ) {
            tableVer.load(tableVerFile);
            sqlTpl.load(sqlFile);
            db = new SQLiteDBConnection(sqlTpl);
        }
    }

    @Override
    public void init() {
        if (db != null) {
            db.connect();
            initOrUpdateSchema();
        }
    }

    @Override
    public void finish() {
        if (db != null) {
            db.release();
        }
    }

    @Override
    public DBConnection getConnection() {
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
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            logger.error("db init error...", ex);
            throw ex;
        }
    }

    private void execTableCreate(String tableName, String fullTableName) throws IOException, SQLException {
        execTableCreateDDLFile(String.format("DDL_%s.sql", fullTableName));
        execMigrationDDLFile(String.format("MIGRATE_%s.sql", fullTableName));
        execViewCreate(tableName, fullTableName);
    }

    private void execTableCreateDDLFile(String fileName) throws IOException, SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", fileName));) {
            db.execute(MiscUtils.toString(is));
        }
    }

    private void execMigrationDDLFile(String fileName) throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(String.format("/sql/%s", fileName));) {
            if (is != null) {
                db.execute(MiscUtils.toString(is));
            } else {
                logger.info("skip migration of {}", fileName);
            }
        } catch (IOException ex) {
            logger.error("table migration error...", ex);
            logger.info("skip migration of {}", fileName);
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
