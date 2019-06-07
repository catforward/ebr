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
package tsm.one.ebr.base.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * 任务定义类
 * - 保存定义文件中的基本信息
 * - 保存单元之间的父子关系
 * - 单元类型有以下三种
 *  -- ROOT：最顶层单元
 *  -- MODULE：有子元素的单元
 *  -- UNIT：最底层单元
 * - 单元有以下状态
 *  -- 成功：外部命令成功
 *  -- 错误：外部命令失败
 *  -- 待机：初始状态
 *  -- 运行：外部命令运行中
 * </pre>
 * 
 * @author catforward
 */
public class TaskUnit {

	public enum State {
		SUCCESSED, ERROR, STANDBY, RUNNING;
	}

	public enum Type {
		ROOT, TASK, MODULE;
	}

	public static class Symbols {
		public final static String EMPTY = "";
		public final static String KEY_ROOT_UNIT = "root_unit";
		public final static String KEY_UID = "uid";
		public final static String KEY_DESC = "desc";
		public final static String KEY_COMMAND = "command";
		public final static String KEY_UNITS = "units";
		public final static String KEY_TRIGGER = "trigger";
		public final static String KEY_PREDECESSORS_LIST = "predecessors";
	}

	public final HashMap<String, Object> meta;
	public final ArrayList<TaskUnit> children;
	public final TaskUnit parent;
	private Type type;
	private String url;
	private volatile State state;
	/* 当单元类型为非TASK时，记录子任务的完成数 */
	private volatile int unfinishedCount;

	public TaskUnit(String unitId, TaskUnit parentUnit) {
		meta = new HashMap<>();
		meta.put(Symbols.KEY_UID, unitId);
		children = new ArrayList<>();
		parent = parentUnit;
		type = Type.TASK;
		unfinishedCount = 0;
	}

	@Override
	public String toString() {
		return String.format("uid:%s=type:%s", getUid(), getType());
	}

	public String getUid() {
		return (String) meta.getOrDefault(Symbols.KEY_UID, "");
	}

	public String getCommand() {
		return (String) meta.getOrDefault(Symbols.KEY_COMMAND, "");
	}

	@SuppressWarnings("unchecked")
	public List<String> getPredecessorsId() {
		return (List<String>) meta.getOrDefault(Symbols.KEY_PREDECESSORS_LIST, List.of());
	}

	/**
	 * <pre>
	 * MODULE单元
	 * 当子单元成功执行后，子调用此函数，将未完成子单元数减1
	 * 当数量为零时，视为此MODULE执行成功
	 * </pre>
	 */
	public void childUnitCompleted() {
		--unfinishedCount;
		if (unfinishedCount <= 0) {
			state = State.SUCCESSED;
		}
	}

	public TaskUnit setUrl(String newUrl) {
		url = newUrl;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public TaskUnit updateState(State newState) {
		switch(newState) {
		case STANDBY: {
			unfinishedCount = children.size();
			break;
		}
		case SUCCESSED: {
			if (this.parent != null) {
				parent.childUnitCompleted();
			}
			break;
		}
		default: break;
		}
		state = newState;
		return this;
	}

	public State getState() {
		return state;
	}

	public TaskUnit setType(Type newType) {
		type = newType;
		return this;
	}

	public Type getType() {
		return type;
	}
	
	public boolean isSuccessed() {
		boolean successed = false;
		switch (type) {
		case ROOT: 
		case MODULE: {
			successed = (unfinishedCount == 0);
			break;
		}
		case TASK: {
			successed = (State.SUCCESSED == state);
			break;
		}
		default: break;
		}
		return successed;
	}
}
