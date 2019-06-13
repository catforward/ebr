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
package tsm.ebr.base.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import tsm.ebr.base.data.TaskUnit.State;
import tsm.ebr.base.data.TaskUnit.Type;

public class TaskFlow {
	// KEY:TaskUnit的UID
	private final Map<String, TaskUnit> rawTaskUnitPool;
	// KEY：ROOT/MODULE的URL
	public final Map<String, MutableGraph<TaskUnit>> taskGraphPool;
	public final TaskUnit rootUnit;

	public TaskFlow(TaskUnit unit) {
		rawTaskUnitPool = new HashMap<>();
		taskGraphPool = new HashMap<>();
		rootUnit = unit;
	}
	
	public void build(Map<String, TaskUnit> unitPool) {
		if (rootUnit == null) {
			throw new RuntimeException("没有ROOT任务单元阿！老哥");
		}
		rawTaskUnitPool.putAll(unitPool);
		MutableGraph<TaskUnit> rootGraph = createEmptyGraph();
		taskGraphPool.put(rootUnit.getUrl(), rootGraph);
		for (TaskUnit unit : rootUnit.children) {
			createTaskGraph(unit, rootGraph);
		}
	}
	
	public void standby() {
		rootUnit.updateState(State.STANDBY);
	}
	
	public void start() {
		// 所有unit进入standby状态
		rawTaskUnitPool.forEach((key, value) -> {
			value.updateState(State.STANDBY);
		});
		rootUnit.updateState(State.RUNNING);
	}
	
	public Set<TaskUnit> getSuccessorsOf(TaskUnit unit) {
		String graphUrl = getGraphUrlOf(unit);
		MutableGraph<TaskUnit> graph = taskGraphPool.get(graphUrl);
		return graph.successors(unit);
	}
	
	public Set<TaskUnit> getPredecessorsOf(TaskUnit unit) {
		String graphUrl = getGraphUrlOf(unit);
		MutableGraph<TaskUnit> graph = taskGraphPool.get(graphUrl);
		return graph.predecessors(unit);
	}
	
	public boolean isConditionCompleteOf(TaskUnit unit) {
		for (TaskUnit prev : getPredecessorsOf(unit)) {
			if (State.SUCCESSED != prev.getState()) {
				return false;
			}
		}
		return true;
	}

	public TaskUnit updateUnitState(String url, State newState) {
		String uid = getTaskUidFrom(url);
		Optional<TaskUnit> optTaskUnit = Optional.ofNullable(rawTaskUnitPool.get(uid));
		if (optTaskUnit.isEmpty()) {
			throw new RuntimeException(String.format("不存在url为%s的TaskUnit对象", url));
		}
		// 更新目标节点状态
		TaskUnit unit = optTaskUnit.get();
		unit.updateState(newState);
		
		return unit;
	}

	public boolean isComplete() {
		return State.SUCCESSED == rootUnit.getState();
	}

	private void createTaskGraph(TaskUnit unit, MutableGraph<TaskUnit> graph) {
		graph.addNode(unit);
		List<String> predecessors = unit.getPredecessorsId();
		if (!predecessors.isEmpty()) {
			for (String predecessorId : predecessors) {
				TaskUnit predecessor = rawTaskUnitPool.get(predecessorId);
				graph.putEdge(predecessor, unit);
			}
		}
		if (Type.MODULE == unit.getType()) {
			MutableGraph<TaskUnit> subGraph = createEmptyGraph();
			taskGraphPool.put(unit.getUrl(), subGraph);
			for (TaskUnit childUnit : unit.children) {
				createTaskGraph(childUnit, subGraph);
			}
		}
	}

	private MutableGraph<TaskUnit> createEmptyGraph() {
		return GraphBuilder.directed() // 指定为有向图
			.nodeOrder(ElementOrder.<TaskUnit>insertion()) // 节点按插入顺序输出
			// (还可以取值无序unordered()、节点类型的自然顺序natural())
			// .expectedNodeCount(NODE_COUNT) //预期节点数
			.allowsSelfLoops(false) // 不允许自环
			.build();
	}
	
	private String getGraphUrlOf(TaskUnit unit) {
		String url = unit.getUrl();
		int lastSlash = url.lastIndexOf("/");
		if (lastSlash == 0) {
			return url;
		} else {
			return url.substring(0, lastSlash);
		}
	}
	
	private String getTaskUidFrom(String fullUrl) {
		int lastSlash = fullUrl.lastIndexOf("/");
		if (lastSlash == 0) {
			return fullUrl;
		} else {
			return fullUrl.substring(lastSlash + 1);
		}
	}

}
