/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.task.manager;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.State;
import tsm.ebr.base.Task.Symbols;
import tsm.ebr.base.Task.Type;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class Item {
    /**
     * <pre>
     * 关系紧密地若干任务组成一组任务流
     *  主要包含以下元素
     *  - 代表任务流自身的任务单元
     *  - 保存任务流中任务单元间的关系图(前驱或后置)
     * </pre>
     *
     * @author catforward
     */
    static class Flow {

        final Unit rootUnit;
        private final MutableGraph<Unit> flowGraph;

        private Flow(Unit unit) {
            rootUnit = unit;
            flowGraph = createEmptyGraph();
        }

        @Override
        public String toString() {
            return rootUnit.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(rootUnit.hashCode(), flowGraph.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Flow)) {
                return false;
            }
            Flow flow = (Flow) o;
            return Objects.equals(rootUnit, flow.rootUnit) &&
                    Objects.equals(flowGraph, flow.flowGraph);
        }

        static Flow makeFrom(Unit unit) {
            return new Flow(unit).build();
        }

        public Flow build() {
            for (Unit child : rootUnit.children) {
                createTaskGraph(child);
            }
            return this;
        }

        public void standby() {
            rootUnit.updateState(State.STANDBY);
        }

        public void start() {
            // 所有unit进入standby状态
            flowGraph.nodes().forEach(unit -> unit.updateState(State.STANDBY));
            rootUnit.updateState(State.RUNNING);
        }

        public Set<Unit> getTopLevelUnits() {
            return flowGraph.nodes().stream()
                    .filter(unit -> rootUnit.url.equals(unit.parent.url))
                    .collect(Collectors.toSet());
        }

        public Set<Unit> getSuccessorsOf(Unit unit) {
            return flowGraph.successors(unit);
        }

        public Set<Unit> getPredecessorsOf(Unit unit) {
            return flowGraph.predecessors(unit);
        }

        public boolean isConditionCompletedOf(Unit unit) {
            for (Unit prev : getPredecessorsOf(unit)) {
                if (State.SUCCEEDED != prev.getState()) {
                    return false;
                }
            }
            return true;
        }

        public boolean isComplete() {
            return rootUnit.isComplete();
        }

        private void createTaskGraph(Unit unit) {
            flowGraph.addNode(unit);
            if (!unit.predecessors.isEmpty()) {
                for (Unit predecessor : unit.predecessors) {
                    flowGraph.putEdge(predecessor, unit);
                }
            }
        }

        private MutableGraph<Unit> createEmptyGraph() {
            return GraphBuilder.directed() // 指定为有向图
                    .nodeOrder(ElementOrder.<Unit>insertion()) // 节点按插入顺序输出
                    // (还可以取值无序unordered()、节点类型的自然顺序natural())
                    // .expectedNodeCount(NODE_COUNT) //预期节点数
                    .allowsSelfLoops(false) // 不允许自环
                    .build();
        }
    }

    /**
     * <pre>
     * Unit:任务定义类
     * - 保存定义文件中的基本信息
     * - 保存单元之间的父子关系
     * - 单元类型有以下三种
     *  -- ROOT：最顶层单元，子元素包括若干MODULE或TASK
     *  -- MODULE：若干基本运行单元构成的模块单元
     *  -- TASK：最基本运行单元
     * - 单元有以下状态
     *  -- 成功：外部命令成功
     *  -- 错误：外部命令失败
     *  -- 待机：初始状态
     *  -- 运行：外部命令运行中
     * </pre>
     *
     * @author catforward
     */
    static class Unit {
        // 任务基本属性定义
        final String uid;
        final String desc;
        final String command;
        final String url;
        final Type type;
        // 任务间的关系定义
        final Unit parent;
        final ArrayList<Unit> children;
        final ArrayList<Unit> predecessors;
        // 任务状态
        private State state;
        // 当单元类型为非TASK(MODULE or ROOT)时，记录子任务的完成数
        private AtomicInteger unfinishedCount;

        Unit(Meta meta, Unit uParent) {
            // copy
            type = Type.valueOf(meta.symbols.get(Symbols.KEY_UNIT_TYPE));
            url = meta.symbols.get(Symbols.KEY_UNIT_URL);
            uid = meta.symbols.get(Symbols.KEY_UID);
            desc = meta.symbols.get(Symbols.KEY_DESC);
            command = meta.symbols.get(Symbols.KEY_COMMAND);
            parent = uParent;
            children = new ArrayList<>(meta.children.size());
            predecessors = new ArrayList<>(meta.predecessorUrl.size());
            unfinishedCount = new AtomicInteger(meta.children.size());
        }

        @Override
        public String toString() {
            return uid;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uid, desc, command, url, type);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Unit)) {
                return false;
            }
            Unit unit = (Unit) o;
            return Objects.equals(uid, unit.uid) &&
                    Objects.equals(desc, unit.desc) &&
                    Objects.equals(command, unit.command) &&
                    Objects.equals(url, unit.url) &&
                    Objects.equals(type, unit.type) &&
                    Objects.equals(parent, unit.parent) &&
                    Objects.equals(children, unit.children) &&
                    Objects.equals(predecessors, unit.predecessors) &&
                    Objects.equals(state, unit.state) &&
                    Objects.equals(unfinishedCount, unit.unfinishedCount);
        }

        /**
         * <pre>
         * MODULE单元
         * 当子单元成功执行后，子调用此函数，将未完成子单元数减1
         * 当数量为零时，视为此MODULE执行成功
         * </pre>
         */
        void childUnitCompleted() {
            if (Type.TASK != type && unfinishedCount.decrementAndGet() <= 0) {
                updateState(State.SUCCEEDED);
            }
        }

        Unit updateState(State newState) {
            switch (newState) {
                case STANDBY: {
                    state = State.STANDBY;
                    if (parent != null && Type.TASK != parent.type) {
                        unfinishedCount = new AtomicInteger(children.size());
                    }
                    break;
                }
                case SUCCEEDED: {
                    state = State.SUCCEEDED;
                    if (parent != null && Type.TASK != parent.type) {
                        parent.childUnitCompleted();
                    }
                    break;
                }
                case RUNNING: {
                    state = State.RUNNING;
                    break;
                }
                case ERROR: {
                    state = State.ERROR;
                    if (parent != null) {
                        parent.updateState(newState); // 传递到root unit
                    }
                }
                default: {
                    state = State.ERROR;
                    break;
                }
            }
            state = newState;
            return this;
        }

        State getState() {
            return state;
        }

        boolean isComplete() {
            return state == State.SUCCEEDED;
        }
    }
}
