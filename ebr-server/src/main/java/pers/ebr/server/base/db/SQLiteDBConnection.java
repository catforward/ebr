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
import pers.ebr.server.base.Paths;

import java.io.File;
import java.sql.*;
import java.util.*;

import static pers.ebr.server.constant.DBConst.TABLE_EXISTS;
import static pers.ebr.server.constant.DBConst.VIEW_EXISTS;

/**
 * <pre>
 * The SQLITE Database Storage Implementation
 * </pre>
 *
 * @author l.gong
 */
public class SQLiteDBConnection implements DBConnection {
    private final static Logger logger = LoggerFactory.getLogger(SQLiteDBConnection.class);
    private final Properties sqlTpl;
    private Connection connection = null;

    SQLiteDBConnection(Properties sqlTpl) {
        this.sqlTpl = sqlTpl;
    }

    @Override
    public void connect() {
        String connStr = String.format("jdbc:sqlite:%s%s%s",
                Paths.getDataPath(), File.separator, SQLiteDBManager.SCHEMA);
        try {
            connection = DriverManager.getConnection(connStr);
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void release() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
             logger.error("database error occurred...", ex);
        }
    }

    @Override
    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate(sql);
        } finally {
            commit();
        }
    }

    public List<Map<String, String>> query(String tplSql, String... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(tplSql);) {
            for (int i = 0; i < params.length; i++) {
                statement.setString(i, params[i]);
            }
            return innerQuery(statement, tplSql);
        }
    }

    public List<Map<String, String>> query(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            return innerQuery(statement, sql);
        }
    }

    private List<Map<String, String>> innerQuery(Statement statement, String sql) throws SQLException {
        statement.setQueryTimeout(30); // set timeout to 30 sec.
        List<Map<String, String>> rows = new ArrayList<>();
        try (ResultSet rs = (statement instanceof PreparedStatement) ?
                ((PreparedStatement) statement).executeQuery() : statement.executeQuery(sql);) {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                HashMap<String, String> row = new HashMap<>();
                for (int i = 1; i <= columns; i++) {
                    row.put(md.getColumnLabel(i), rs.getString(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    boolean isTableExist(String tableName) throws SQLException {
        String existSql = sqlTpl.getProperty(TABLE_EXISTS);
        var result = query(String.format(existSql, tableName));
        if (result == null || result.isEmpty()) {
            return false;
        }
        int cnt = Integer.parseInt(result.get(0).get("cnt"));
        return cnt >= 1;
    }

    boolean isViewExist(String viewName) throws SQLException {
        String existSql = sqlTpl.getProperty(VIEW_EXISTS);
        var result = query(String.format(existSql, viewName));
        if (result == null || result.isEmpty()) {
            return false;
        }
        int cnt = Integer.parseInt(result.get(0).get("cnt"));
        return cnt >= 1;
    }

}
