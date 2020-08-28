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
import pers.ebr.server.common.Paths;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.domain.*;
import pers.ebr.server.domain.ExternalCommandTaskView;
import pers.ebr.server.domain.ExternalCommandTaskflowView;

import java.io.File;
import java.sql.*;
import java.util.*;

import static pers.ebr.server.common.TaskState.COMPLETE;
import static pers.ebr.server.common.TaskState.FAILED;

/**
 * <p>
 * 数据库服务的SQLITE实现
 * </p>
 *
 * @author l.gong
 */
final class SqliteDatabaseImpl implements IDatabase {
    private final static Logger logger = LoggerFactory.getLogger(SqliteDatabaseImpl.class);

    final static String VAL_NONE = "none";

    final static String COL_COUNT = "cnt";
    final static String COL_TASKFLOW_ID = "taskflow_id";
    final static String COL_TASKFLOW_DEFINE = "taskflow_define";

    final static String SQL_VIEW_EXISTS = "VIEW_EXISTS";
    final static String SQL_CREATE_VIEW = "CREATE_VIEW";
    final static String SQL_DROP_VIEW = "DROP_VIEW";
    final static String SQL_TABLE_EXISTS = "TABLE_EXISTS";
    final static String SQL_DROP_TABLE = "DROP_TABLE";
    final static String SQL_RENAME_TABLE = "RENAME_TABLE";
    final static String SQL_GET_TABLE_NAME = "GET_TABLE_NAME";

    final static String SQL_TASKFLOW_EXISTS = "TASKFLOW_EXISTS";
    final static String SQL_SAVE_TASKFLOW = "SAVE_TASKFLOW";
    final static String SQL_LOAD_TASKFLOW = "LOAD_TASKFLOW";
    final static String SQL_LOAD_ALL_TASKFLOW_DEFINE = "LOAD_ALL_TASKFLOW_DEFINE";
    final static String SQL_DEL_TASKFLOW = "DEL_TASKFLOW";

    final static String SQL_SAVE_TASK_DETAIL = "SAVE_TASK_DETAIL";
    final static String SQL_LOAD_ALL_TASK_DETAIL = "LOAD_ALL_TASK_DETAIL";
    final static String SQL_DEL_TASK_DETAIL = "DEL_TASK_DETAIL";

    final static String SQL_EXEC_HIST_HIST_EXISTS = "TASK_EXEC_HIST_EXISTS";
    final static String SQL_SAVE_EXEC_HIST_HIST = "SAVE_TASK_EXEC_HIST";
    final static String SQL_UPDATE_EXEC_HIST_HIST_STATE = "UPDATE_TASK_EXEC_HIST_STATE";
    final static String SQL_UPDATE_EXEC_HIST_HIST_RESULT = "UPDATE_TASK_EXEC_HIST_RESULT";
    final static String SQL_DEL_EXEC_HIST_HIST = "DEL_TASK_EXEC_HIST";

    private final Properties sqlTpl;
    private Connection connection = null;

    SqliteDatabaseImpl(Properties sqlTpl) {
        this.sqlTpl = sqlTpl;
    }

    /**
     * 保存任务流对象
     * @param taskflow [in] 待保存的任务流
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public void saveTaskflow(ITaskflow taskflow) throws RepositoryException {
        String saveSql = sqlTpl.getProperty(SQL_SAVE_TASKFLOW);
        try (PreparedStatement statement = connection.prepareStatement(saveSql)) {
            statement.setString(1, taskflow.getRootTask().getId());
            statement.setString(2, taskflow.toJsonObject().encodePrettily());
            statement.executeUpdate();
            setTaskDetail(taskflow);
            commit();
        } catch (SQLException ex) {
            rollback();
            throw new RepositoryException(ex);
        }
    }

    /**
     * 使用id查找并读取一个任务流
     * @param flowId [in] 任务流ID
     * @return ITaskflow
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public ITaskflow loadTaskflow(String flowId) throws RepositoryException {
        String loadSql = sqlTpl.getProperty(SQL_LOAD_TASKFLOW);
        try {
            List<Map<String, String>> result = query(loadSql, flowId);
            if (result.isEmpty()) {
                return null;
            }
            String defineBody = result.get(0).getOrDefault(COL_TASKFLOW_DEFINE, "");
            return ModelItemMaker.makeExternalTaskflow(new JsonObject(defineBody));
        } catch (SQLException ex) {
            throw new RepositoryException(ex);
        }
    }

    /**
     * 使用id删除一个任务流
     * @param flowId [in] 任务流ID
     * @return int
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public int removeTaskflow(String flowId) throws RepositoryException {
        int workflowCnt;
        int taskDetailCnt;
        int execHistCnt;
        String sql = sqlTpl.getProperty(SQL_DEL_TASKFLOW);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flowId);
            workflowCnt = statement.executeUpdate();
            String url = String.format("/%s", flowId);
            String prefix = String.format("/%s/", flowId);
            taskDetailCnt = this.removeTaskDetail(url, prefix);
            execHistCnt = this.removeTaskExecHist(url, prefix);
            commit();
            logger.info("delete workflow record:[{}], task detail record:[{}], execute hist record:[{}]",
                    workflowCnt, taskDetailCnt, execHistCnt);
        } catch (SQLException ex) {
            rollback();
            throw new RepositoryException(ex);
        }
        return (workflowCnt + taskDetailCnt + execHistCnt);
    }

    /**
     * 判断给定ID的任务流定义是否存在
     * @param flowId [in] 任务流ID
     * @return boolean
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public boolean isTaskflowExists(String flowId) throws RepositoryException {
        String sql = sqlTpl.getProperty(SQL_TASKFLOW_EXISTS);
        try {
            var result = query(sql, flowId);
            return !result.isEmpty();
        } catch (SQLException ex) {
            throw new RepositoryException(ex);
        }
    }

    /**
     * 保存任务流里所有的任务定义
     * @param flow [in] 任务流
     * @throws SQLException 发生SQL异常时转换并抛出此异常
     */
    private void setTaskDetail(ITaskflow flow) throws SQLException {
        if (flow.isEmpty()) {
            return;
        }
        Set<IExternalCommandTask> tasks = flow.getAllExternalTask();
        String saveSql = sqlTpl.getProperty(SQL_SAVE_TASK_DETAIL);
        try (PreparedStatement statement = connection.prepareStatement(saveSql)){
            for (IExternalCommandTask task : tasks) {
                statement.setString(1, task.getPath());
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
                if (task.getDepends().isEmpty()) {
                    statement.setString(6, VAL_NONE);
                } else {
                    statement.setString(6, task.getDepends().toString());
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * 删除任务定义
     * @param path       [in] 任务的逻辑路径
     * @param pathPrefix [in] 模糊查询用逻辑路径
     * @return int
     * @throws SQLException 发生SQL异常时转换并抛出此异常
     */
    private int removeTaskDetail(String path, String pathPrefix) throws SQLException {
        String sql = sqlTpl.getProperty(SQL_DEL_TASK_DETAIL);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, path);
            statement.setString(2, pathPrefix + "%");
            return statement.executeUpdate();
        }
    }

    /**
     * 保存一次任务执行记录
     * @param instanceId [in] 任务实例ID
     * @param path       [in] 任务逻辑路径
     * @param newState   [in] 任务最终状态
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public void saveTaskExecHist(String instanceId, String path, TaskState newState) throws RepositoryException {
        String sql = sqlTpl.getProperty(SQL_EXEC_HIST_HIST_EXISTS);
        boolean histRecExist = false;
        try {
            var result = query(sql, instanceId, path);
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
                    sql = sqlTpl.getProperty(SQL_UPDATE_EXEC_HIST_HIST_RESULT);
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setLong(1, System.currentTimeMillis());
                        statement.setInt(2, newState.ordinal());
                        statement.setString(3, instanceId);
                        statement.setString(4, path);
                        statement.executeUpdate();
                        commit();
                    }
                } else {
                    sql = sqlTpl.getProperty(SQL_UPDATE_EXEC_HIST_HIST_STATE);
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setInt(1, newState.ordinal());
                        statement.setString(2, instanceId);
                        statement.setString(3, path);
                        statement.executeUpdate();
                        commit();
                    }
                }
            } else {
                sql = sqlTpl.getProperty(SQL_SAVE_EXEC_HIST_HIST);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, instanceId);
                    statement.setString(2, path);
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

    /**
     * 执行删除任务执行历史的操作
     * @param path       [in] 任务的逻辑路径
     * @param pathPrefix [in] 模糊查询用逻辑路径
     * @return int
     * @throws SQLException 发生SQL异常时转换并抛出此异常
     */
    private int removeTaskExecHist(String path, String pathPrefix) throws SQLException {
        String sql = sqlTpl.getProperty(SQL_DEL_EXEC_HIST_HIST);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, path);
            statement.setString(2, pathPrefix + "%");
            return statement.executeUpdate();
        }
    }

    /**
     * 获取所有已保存的任务详细数据
     * @return Collection
     * @throws RepositoryException 发生SQL异常时转换并抛出此异常
     */
    @Override
    public Collection<ExternalCommandTaskflowView> getAllTaskflowDetail() throws RepositoryException {
        HashMap<String, ExternalCommandTaskflowView> flows = new HashMap<>(16);
        String sql = sqlTpl.getProperty(SQL_LOAD_ALL_TASK_DETAIL);
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)
        ) {
            String currentUrl = VAL_NONE;
            ExternalCommandTaskflowView workflow = null;
            while (rs.next()) {
                ExternalCommandTaskView task = ModelItemMaker.makeTaskView();
                task.setPath(rs.getString(1));
                task.setGroup(rs.getString(2));
                task.setId(rs.getString(3));
                task.setDesc(rs.getString(4));
                task.setCmd(rs.getString(5));
                task.setDepends(rs.getString(6));

                if (!task.getPath().startsWith(currentUrl)) {
                    if (workflow != null && !flows.containsKey(workflow.getRootView().getId())) {
                        flows.put(workflow.getRootView().getId(), workflow);
                    }
                    currentUrl = task.getPath();
                    workflow = ModelItemMaker.makeTaskflowView(task);
                }

                if (workflow != null) {
                    workflow.addTaskView(task);
                }
            }

            if (workflow != null && !flows.containsKey(workflow.getRootView().getId())) {
                flows.put(workflow.getRootView().getId(), workflow);
            }

        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RepositoryException(ex);
        }
        return flows.values();
    }

    //===========================================================================================================
    // Basic Proc
    //===========================================================================================================

    /**
     * 连接数据库
     * @throws RepositoryException 连接失败时
     */
    public void connect() throws RepositoryException {
        String connStr = String.format("jdbc:sqlite:%s%s%s",
                Paths.getDataPath(), File.separator, LocalRepositoryManager.SQLITE_SCHEMA);
        try {
            connection = DriverManager.getConnection(connStr);
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RepositoryException(ex);
        }
    }

    /**
     * 释放数据库连接
     * @throws RepositoryException 释放失败时
     */
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

    /**
     * 提交事务
     */
    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 执行SQL语句
     * @param sql 待执行SQL语句
     * @throws SQLException 执行失败时
     */
    public void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // set timeout to 30 sec.
            statement.setQueryTimeout(30);
            statement.executeUpdate(sql);
        } finally {
            commit();
        }
    }

    /**
     * 执行带有需要参数替换的SQL
     * @param tplSql 带参数的SQL
     * @param params 待替换的参数值
     * @return List
     * @throws SQLException 执行失败时
     */
    public List<Map<String, String>> query(String tplSql, String... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(tplSql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            return innerQuery(statement, tplSql);
        }
    }

    /**
     * 执行SQL查询
     * @param sql 待执行SQL
     * @return List
     * @throws SQLException 执行失败时
     */
    public List<Map<String, String>> query(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return innerQuery(statement, sql);
        }
    }

    /**
     * 执行SQL文
     * @param statement 连接
     * @param sql       SQL
     * @return List
     * @throws SQLException 执行失败时
     */
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

    /**
     * 判断表是否已存在
     * @param tableName 表名
     * @return boolean
     * @throws SQLException 执行失败时
     */
    boolean isTableExist(String tableName) throws SQLException {
        String existSql = sqlTpl.getProperty(SQL_TABLE_EXISTS);
        var result = query(String.format(existSql, tableName));
        if (result.isEmpty()) {
            return false;
        }
        int cnt = Integer.parseInt(result.get(0).get(COL_COUNT));
        return cnt >= 1;
    }

    /**
     * 判断视图是否存在
     * @param viewName 视图名
     * @return boolean
     * @throws SQLException 执行失败时
     */
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
