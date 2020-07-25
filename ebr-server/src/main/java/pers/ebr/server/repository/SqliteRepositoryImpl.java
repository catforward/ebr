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
import pers.ebr.server.common.Paths;
import pers.ebr.server.common.model.*;

import java.io.File;
import java.sql.*;
import java.util.*;

import static pers.ebr.server.common.model.TaskState.COMPLETE;
import static pers.ebr.server.common.model.TaskState.FAILED;
import static pers.ebr.server.repository.SqliteRepositoryConst.*;

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
    public void setWorkflow(String flowId, String flowDetail) throws RepositoryException {
        String saveSql = sqlTpl.getProperty(SQL_SAVE_WORKFLOW);
        try (PreparedStatement statement = connection.prepareStatement(saveSql);) {
            statement.setString(1, flowId);
            statement.setString(2, flowDetail);
            statement.executeUpdate();
            commit();
        } catch (SQLException ex) {
            rollback();
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public void setTaskDetail(DAGWorkflow flow) throws RepositoryException {
        Set<ITask> tasks = flow.getAllTask();
        if (tasks.isEmpty()) {
            return;
        }
        String saveSql = sqlTpl.getProperty(SQL_SAVE_TASK_DETAIL);
        try (PreparedStatement statement = connection.prepareStatement(saveSql);){
            for (ITask task : tasks) {
                statement.setString(1, task.getUrl());
                statement.setString(2, task.getGroupId());
                statement.setString(3, task.getId());
                String objStr = Optional.ofNullable(task.getDesc()).orElse(VAL_NONE);
                if (objStr.isEmpty()) {
                    statement.setString(4, VAL_NONE);
                } else {
                    statement.setString(4, objStr);
                }
                objStr = Optional.ofNullable(task.getCmdLine()).orElse(VAL_NONE);
                if (objStr.isEmpty()) {
                    statement.setString(5, VAL_NONE);
                } else {
                    statement.setString(5, objStr);
                }
                if (task.getDependIdList().isEmpty()) {
                    statement.setString(6, VAL_NONE);
                } else {
                    statement.setString(6, task.getDependIdList().toString());
                }
                statement.addBatch();
            }
            statement.executeBatch();
            commit();
        } catch (SQLException ex) {
            rollback();
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public void setTaskState(String instanceId, String taskUrl, TaskState newState) throws RepositoryException {
        // TODO
        String sql = sqlTpl.getProperty(SQL_WORKFLOW_HIST_EXISTS);
        boolean histRecExist = false;
        try {
            var result = query(sql, instanceId, taskUrl);
            if (!result.isEmpty()) {
                histRecExist = Integer.parseInt(result.get(0).get(COL_COUNT)) >= 1;
            }
        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }

        try {
            if (histRecExist) {
                if (COMPLETE == newState || FAILED == newState) {
                    sql = sqlTpl.getProperty(SQL_UPDATE_WORKFLOW_HIST_RESULT);
                    try (PreparedStatement statement = connection.prepareStatement(sql);) {
                        statement.setLong(1, System.currentTimeMillis());
                        statement.setInt(2, newState.ordinal());
                        statement.setString(3, instanceId);
                        statement.setString(4, taskUrl);
                        statement.executeUpdate();
                        commit();
                    }
                } else {
                    sql = sqlTpl.getProperty(SQL_UPDATE_WORKFLOW_HIST_STATE);
                    try (PreparedStatement statement = connection.prepareStatement(sql);) {
                        statement.setInt(1, newState.ordinal());
                        statement.setString(2, instanceId);
                        statement.setString(3, taskUrl);
                        statement.executeUpdate();
                        commit();
                    }
                }
            } else {
                sql = sqlTpl.getProperty(SQL_SAVE_WORKFLOW_HIST);
                try (PreparedStatement statement = connection.prepareStatement(sql);) {
                    statement.setString(1, instanceId);
                    statement.setString(2, taskUrl);
                    statement.setLong(3, System.currentTimeMillis());
                    statement.setInt(4, newState.ordinal());
                    statement.executeUpdate();
                    commit();
                }
            }
        } catch (SQLException ex) {
            rollback();
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public String getWorkflow(String flowId) throws RepositoryException {
        String loadSql = sqlTpl.getProperty(SQL_LOAD_WORKFLOW);
        try {
            List<Map<String, String>> result = query(loadSql, flowId);
            if (result.isEmpty()) {
                return "";
            }
            return result.get(0).getOrDefault(COL_WORKFLOW_DEFINE, "");
        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
    }

    @Override
    public Collection<WorkflowDetail> getAllWorkflowDetail() throws RepositoryException {
        HashMap<String, WorkflowDetail> flows = new HashMap<>(16);
        String sql = sqlTpl.getProperty(SQL_LOAD_ALL_TASK_DETAIL);
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql);
        ) {
            String currentUrl = VAL_NONE;
            WorkflowDetail workflow = null;
            while (rs.next()) {
                TaskDetail task = ModelItemBuilder.buildTaskDetail();
                task.setUrl(rs.getString(1));
                task.setGroup(rs.getString(2));
                task.setId(rs.getString(3));
                task.setDesc(rs.getString(4));
                task.setCmd(rs.getString(5));
                task.setDeps(rs.getString(6));

                if (!task.getUrl().startsWith(currentUrl)) {
                    if (workflow != null && !flows.containsKey(workflow.getRootDetail().getId())) {
                        flows.put(workflow.getRootDetail().getId(), workflow);
                    }
                    currentUrl = task.getUrl();
                    workflow = ModelItemBuilder.buildWorkflowDetail(task);
                }

                if (workflow != null) {
                    workflow.addTaskDetail(task);
                }
            }

            if (workflow != null && !flows.containsKey(workflow.getRootDetail().getId())) {
                flows.put(workflow.getRootDetail().getId(), workflow);
            }

        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
        return flows.values();
    }

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

    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

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
                statement.setString(i + 1, params[i]);
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
        String existSql = sqlTpl.getProperty(SQL_TABLE_EXISTS);
        var result = query(String.format(existSql, tableName));
        if (result.isEmpty()) {
            return false;
        }
        int cnt = Integer.parseInt(result.get(0).get(COL_COUNT));
        return cnt >= 1;
    }

    boolean isViewExist(String viewName) throws SQLException {
        String existSql = sqlTpl.getProperty(SQL_VIEW_EXISTS);
        var result = query(String.format(existSql, viewName));
        if (result.isEmpty()) {
            return false;
        }
        int cnt = Integer.parseInt(result.get(0).get(COL_COUNT));
        return cnt >= 1;
    }

}
