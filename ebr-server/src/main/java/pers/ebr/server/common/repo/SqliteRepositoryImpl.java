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
import pers.ebr.server.common.Paths;

import java.io.File;
import java.sql.*;
import java.util.*;

import static pers.ebr.server.common.repo.SqliteRepositoryConst.*;

/**
 * <pre>
 * The SQLITE Database Repository Implementation
 * </pre>
 *
 * @author l.gong
 */
final class SqliteRepositoryImpl implements IRepository {
    private final static Logger logger = LoggerFactory.getLogger(SqliteRepositoryImpl.class);
    private final Properties sqlTpl;
    private Connection connection = null;

    SqliteRepositoryImpl(Properties sqlTpl) {
        this.sqlTpl = sqlTpl;
    }

    @Override
    public void connect() throws RepositoryException {
        String connStr = String.format("jdbc:sqlite:%s%s%s",
                Paths.getDataPath(), File.separator, SqliteRepositoryManager.SCHEMA);
        try {
            connection = DriverManager.getConnection(connStr);
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public void release() throws RepositoryException {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
             logger.error("database error occurred...", ex);
             throw new RepositoryException(ex);
        }
    }

    @Override
    public void setFlowItem(String key, String value) throws RepositoryException {
        String saveSql = sqlTpl.getProperty(SAVE_FLOW);
        try {
            execute(String.format(saveSql, key, value));
            commit();
        } catch (SQLException ex) {
            rollback();
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public String getFlowItem(String flowId) throws RepositoryException {
        String loadSql = sqlTpl.getProperty(LOAD_FLOW);
        try {
            List<Map<String, String>> result = query(String.format(loadSql, flowId));
            if (result.isEmpty()) {
                return "";
            }
            return result.get(0).getOrDefault("DETAIL", "");
        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public List<String> loadAllFlowId() throws RepositoryException {
        List<String> flows = new ArrayList<>();
        String loadAllSql = sqlTpl.getProperty(LOAD_ALL_FLOW);
        try {
            List<Map<String, String>> result = query(loadAllSql);
            for (var row : result) {
                flows.add(row.getOrDefault("NAME", ""));
            }
        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
        return flows;
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
        try (Statement statement = connection.createStatement()) {
            // set timeout to 30 sec.
            statement.setQueryTimeout(30);
            statement.executeUpdate(sql);
        } finally {
            commit();
        }
    }

    public List<Map<String, String>> query(String tplSql, String... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(tplSql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setString(i, params[i]);
            }
            return innerQuery(statement, tplSql);
        }
    }

    public List<Map<String, String>> query(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return innerQuery(statement, sql);
        }
    }

    private List<Map<String, String>> innerQuery(Statement statement, String sql) throws SQLException {
        // set timeout to 30 sec.
        statement.setQueryTimeout(30);
        List<Map<String, String>> rows = new ArrayList<>();
        try (ResultSet rs = (statement instanceof PreparedStatement) ?
                ((PreparedStatement) statement).executeQuery() : statement.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                HashMap<String, String> row = new HashMap<>(columns);
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
