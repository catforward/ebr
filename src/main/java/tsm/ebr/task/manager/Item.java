package tsm.ebr.task.manager;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.State;
import tsm.ebr.base.Task.Symbols;
import tsm.ebr.base.Task.Type;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * <pre>
 * Unit:任务定义类
 * - 保存定义文件中的基本信息
 * - 保存单元之间的父子关系
 * - 单元类型有以下三种
 *  -- ROOT：最顶层单元，代表包括其子元素在内，构成一个完整的Flow
 *  -- MODULE：有子元素的单元
 *  -- UNIT：最底层单元
 * - 单元有以下状态
 *  -- 成功：外部命令成功
 *  -- 错误：外部命令失败
 *  -- 待机：初始状态
 *  -- 运行：外部命令运行中
 * Flow:任务流定义类
 * - 保存单元间的关系(前驱或后置)
 * </pre>
 *
 * @author catforward
 */
class Item {

    static class Flow {

        final Unit rootUnit;
        private final MutableGraph<Unit> flowGraph;

        private Flow(Unit unit) {
            rootUnit = unit;
            flowGraph = createEmptyGraph();
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
     *     封装任务定义
     *     用于管理任务间的关系
     * </pre>
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

        private Unit(Meta meta) {
            // copy
            type = meta.type;
            url = meta.url;
            uid = (String) meta.raw.get(Symbols.KEY_UID);
            desc = (String) meta.raw.get(Symbols.KEY_DESC);
            command = (String) meta.raw.get(Symbols.KEY_COMMAND);
            // cascade init
            parent = meta.parent != null ? new Unit(meta.parent) : null;
            children = new ArrayList<>(meta.children.size());
            predecessors = new ArrayList<>(meta.predecessors.size());
            for (Meta child : meta.children) {
                children.add(new Unit(child));
            }
            for (Meta pred : meta.predecessors) {
                predecessors.add(new Unit(pred));
            }
        }

        static Unit copyOf(Meta meta) {
            return new Unit(meta);
        }

        @Override
        public String toString() {
            return uid;
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
