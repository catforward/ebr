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
package pers.ebr.server.model;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.TaskState;
import pers.ebr.server.common.graph.DirectedGraph;
import pers.ebr.server.common.graph.GraphBuilder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static pers.ebr.server.common.TaskState.*;
import static pers.ebr.server.common.TaskType.GROUP;

/**
 * <p>
 * 封装任务的工作流对象
 * 一个工作流对象由以下部分组合而成
 * <ul>
 *     <li>根任务引用</li>
 *     <li>运行时的实例ID</li>
 *     <li>运行时任务逻辑路径与任务的映射集合</li>
 *     <li>运行时任务ID与任务的映射集合</li>
 *     <li>运行时封装Group类型任务内所有子任务间依赖关系的图数据集合</li>
 * </ul>
 * </p>
 *
 * @author l.gong
 */
public final class ExternalCommandWorkflow implements IWorkflow {
    private final static Logger logger = LoggerFactory.getLogger(ExternalCommandWorkflow.class);
    private ExternalCommandTask rootTask = null;
    private String instanceId = null;
    private IRunnableTaskAppender appender = null;
    private ITaskStateWatcher watcher = null;
    private final HashMap<String, ExternalCommandTask> pathTaskMap = new HashMap<>();
    private final HashMap<String, ExternalCommandTask> idTaskMap = new HashMap<>();
    private final HashMap<String, DirectedGraph<ExternalCommandTask>> idGraphMap = new HashMap<>();

    ExternalCommandWorkflow() {}

    /**
     * 创建工作流内部资源
     */
    ExternalCommandWorkflow build() {
        updatePropInfo();
        updateGraphInfo();
        updatePathInfo(rootTask);
        idTaskMap.forEach((id, task) -> pathTaskMap.put(task.getPath(), task));
        return this;
    }

    /**
     * 添加任务对象
     *
     * @param task [in] 待添加的任务对象
     */
    void addTask(ExternalCommandTask task) {
        idTaskMap.put(task.meta.getId(), task);
        if (task.isRootTask()) {
            if (rootTask == null) {
                rootTask = task;
            } else {
                throw new RuntimeException(String.format("only one root task can be defined in a signal define file. id:[%s]", task.meta.getId()));
            }
        }
    }

    /**
     * 工作流执行结束后释放资源
     */
    @Override
    public void release() {
        appender = null;
        watcher = null;
        idGraphMap.clear();
        pathTaskMap.clear();
        idTaskMap.forEach((id, task) -> task.release());
        idTaskMap.clear();
    }

    /**
     * 启动工作流
     */
    @Override
    public void standby() {
        idTaskMap.forEach((id, task) -> task.prop.setState(INACTIVE));
        TaskState oldState = TaskState.valueOf(rootTask.prop.state.name());
        rootTask.prop.setState(ACTIVE);
        notifyTaskStateChanged(rootTask.prop.getPath(), true, oldState, ACTIVE);
        collectRunnableTasks(rootTask);
    }

    /**
     * 判断此工作流中是否包含任务
     *
     * @return boolean
     */
    @Override
    public boolean isEmpty() {
        return idTaskMap.isEmpty();
    }

    /**
     * 获取根任务
     *
     * @return IExternalTask
     */
    @Override
    public IExternalCommandTask getRootTask() {
        return rootTask;
    }

    /**
     * 设置运行时实例ID
     *
     * @param newId [in] 待设置的实例ID
     */
    @Override
    public void setInstanceId(String newId) {
        instanceId = newId;
        idTaskMap.forEach((id, task) -> task.prop.setInstanceId(instanceId));
    }

    /**
     * 获取运行时实例ID
     *
     * @return String
     */
    @Override
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 获取工作流的状态
     *
     * @return TaskState
     */
    @Override
    public TaskState getState() {
        return rootTask.prop.getState();
    }

    /**
     * 获取所有任务的集合
     *
     * @return Set
     */
    @Override
    public Set<IExternalCommandTask> getAllExternalTask() {
        return Set.copyOf(idTaskMap.values());
    }

    /**
     * <pre>
     * 更新任务状态,
     * 之后检查并更新可执行任务的集合
     * </pre>
     *
     * @param path     [in] 任务逻辑路径
     * @param newState [in] 任务新状态
     */
    @Override
    public synchronized void setTaskState(String path, TaskState newState) {
        ExternalCommandTask task = Optional.ofNullable(pathTaskMap.get(path)).orElseThrow();
        TaskState oldState = TaskState.valueOf(task.prop.state.name());
        task.prop.setState(newState);
        notifyTaskStateChanged(path, task.isRootTask(), oldState, newState);

        if (task.isRootTask()) {
            return;
        }

        // 任务失败时，逐级向上传递失败状态
        if (FAILED == task.prop.getState()) {
            setTaskState(task.prop.getGroupTask().getPath(), FAILED);
            return;
        }
        // 任务开始，收集以此任务为中心的可执行任务集合
        if (ACTIVE == task.prop.getState()) {
            collectRunnableTasks(task);
            return;
        }
        // 任务正常结束
        if (COMPLETE == task.prop.getState()) {
            // 收集以此任务为中心的可执行任务集合
            collectRunnableTasks(task);
            // 如果此任务所在组全部正常结束，则将正常结束状态传递至上级
            ExternalCommandTask groupTask = idTaskMap.get(task.meta.getGroup());
            long unfinishedDependTaskCnt = groupTask.prop.getSubTaskSet().stream()
                    .filter(t -> COMPLETE != t.prop.getState()).count();
            if (unfinishedDependTaskCnt == 0) {
                setTaskState(groupTask.getPath(), COMPLETE);
            }
        }
    }

    /**
     * <p>
     * IRunnableTaskAppender接口的实现类，
     * 当有满足执行条件的任务对象存在时，向接口实现者推从可执行的引用
     * </p>
     *
     * @param  appender [in] IRunnableTaskAppender接口的实现者
     */
    @Override
    public void setRunnableTaskAppender(IRunnableTaskAppender appender) {
        this.appender = appender;
    }

    /**
     * <p>
     * 当有任务对象状态改变时，向接口实现者推送新状态
     * </p>
     *
     * @param watcher [in] ITaskStateWatcher接口的实现类
     */
    @Override
    public void setTaskStateWatcher(ITaskStateWatcher watcher) {
        this.watcher = watcher;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        idGraphMap.forEach((id, graph) -> sb.append(String.format("\n%s : (\n%s\n)", id, graph.toString())));
        return sb.toString();
    }

    /**
     * <p>
     *     更新加入工作流中所有任务的以下运行时状态
     *     <ul>
     *         <li>任务的组任务引用</li>
     *         <li>任务的依赖任务引用集合</li>
     *         <li>任务的子任务引用集合</li>
     *     </ul>
     * </p>
     */
    private void updatePropInfo() {
        idTaskMap.forEach((id, task) -> {
            if (task.isRootTask()) {
                task.prop.setGroupTask(task);
                createGraph(task.getId());
            } else {
                ExternalCommandTask groupTask = Optional.ofNullable(idTaskMap.get(task.meta.getGroup())).orElseThrow();
                if (GROUP != groupTask.prop.getType()) {
                    groupTask.prop.setType(GROUP);
                }
                groupTask.prop.addSubTask(task);
                task.meta.getDepends().forEach(depId -> {
                    ExternalCommandTask depTask = Optional.ofNullable(idTaskMap.get(depId)).orElseThrow();
                    task.prop.addDepTask(depTask);
                });
                task.prop.setGroupTask(groupTask);
            }
        });
    }

    /**
     * 更新加入工作流中所有任务的DAG图
     */
    private void updateGraphInfo() {
        idTaskMap.forEach((id, task) -> {
            if (task.isRootTask()) { return; }
            DirectedGraph<ExternalCommandTask> groupGraph = Optional.ofNullable(idGraphMap.get(task.meta.getGroup())).orElseGet(() -> {
                createGraph(task.meta.getGroup());
                return Optional.ofNullable(idGraphMap.get(task.meta.getGroup())).orElseThrow();
            });
            groupGraph.addVertex(task);

            task.meta.getDepends().forEach(dependId -> {
                ExternalCommandTask predecessor = Optional.ofNullable(idTaskMap.get(dependId)).orElseThrow();
                groupGraph.putEdge(predecessor, task);
            });
        });
    }

    /**
     * 更新加入工作流中所有任务的逻辑路径
     *
     * @param task [in] 待更新的任务对象
     */
    private void updatePathInfo(ExternalCommandTask task) {
        if (task.isRootTask()) {
            task.prop.setPath(String.format("/%s", task.meta.getId()));
        } else {
            task.prop.setPath(String.format("%s/%s", task.prop.getGroupTask().getPath(), task.meta.getId()));
        }
        if (GROUP == task.prop.getType()) {
            for (ExternalCommandTask sub : task.prop.getSubTaskSet()) {
                if (GROUP == sub.prop.getType()) {
                    updatePathInfo(sub);
                } else {
                    sub.prop.setPath(String.format("%s/%s", sub.prop.getGroupTask().getPath(), sub.meta.getId()));
                }
            }
        }
    }

    /**
     * 创建一个指定ID的图数据
     * @param graphId [in] 待设置的图数据ID
     */
    private void createGraph(String graphId) {
        idGraphMap.put(graphId, makeEmptyGraph());
    }

    /**
     * 创建一个空的DAG图
     * @return DirectedGraph
     */
    private DirectedGraph<ExternalCommandTask> makeEmptyGraph() {
        return GraphBuilder.directed().setAllowsSelfLoops(false).build();
    }

    /**
     * 查找满足执行条件的任务
     *
     * @param task [in] 状态已变更的任务
     */
    private void collectRunnableTasks(ExternalCommandTask task) {
        if (GROUP == task.prop.getType() && ACTIVE == task.prop.getState()) {
            task.prop.getSubTaskSet().forEach(sub -> {
                long unfinishedDependTaskCnt = sub.prop.getDepTaskSet().stream()
                        .filter(t -> COMPLETE != t.prop.getState()).count();
                if (unfinishedDependTaskCnt == 0 && INACTIVE == sub.prop.getState()) {
                    appendToRunnableQueue(sub);
                }
            });
        } else if (COMPLETE == task.prop.getState()) {
            DirectedGraph<ExternalCommandTask> groupGraph = idGraphMap.get(task.meta.getGroup());
            if (GROUP != task.prop.getGroupTask().getType() || groupGraph == null) {
                return;
            }
            groupGraph.successors(task).forEach(successor -> {
                long unfinishedDependTaskCnt = successor.getDependTaskSet().stream()
                        .filter(t -> COMPLETE != t.getState()).count();
                if (unfinishedDependTaskCnt == 0 && INACTIVE == successor.getState()) {
                    appendToRunnableQueue(successor);
                }
            });
        }
    }

    /**
     * 向IRunnableTaskAppender接口的实现类推送可执行的引用
     * @param task [in] 待执行任务
     */
    private void appendToRunnableQueue(ExternalCommandTask task) {
        if (appender != null) {
            appender.append(this, task);
        }
    }

    /**
     * 向ITaskStateWatcher接口的实现类推送任务的状态迁移信息
     * @param path       [in] 任务逻辑路径
     * @param isRootTask [in] 是否是根任务
     * @param oldState   [in] 变更前状态
     * @param newState   [in] 变更后状态
     */
    private void notifyTaskStateChanged(String path, boolean isRootTask, TaskState oldState, TaskState newState) {
        if (watcher != null) {
            watcher.onStateChanged(instanceId, path, isRootTask, oldState, newState);
        }
    }

    /**
     * 返回此数据的JSON对象
     *
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        idTaskMap.forEach((id, task) -> {
            jsonObject.put(task.meta.getId(), task.meta.toJsonObject());
        });
        return jsonObject;
    }
}
