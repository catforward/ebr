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
package tsm.one.ebr.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 任务定义类，包装单独一个需要执行的外部命令行 一个任务集由以下三层结构构成
 * NET：保存若干个基于流程逻辑关系的NODE节点
 * NODE：保存与其他META之间的逻辑关系 
 * META：保存需要执行的任务（命令，程序...）的基本定义
 * 
 * @author catforward
 */
public class Task {

	/**
	 * 任务的状态枚举
	 */
	public enum TaskStatus {
		UNKNOWN("UNKNOWN", "STATUS UNKNOWN"),
		STANDBY("STANDBY", "TASK STANDBY"),
		SUSPENDED("SUSPEND", "TASK SUSPENDED"),
		RUNNING("RUNNING", "TASK RUNNING"),
		ERROR("ERROR", "TASK ERROR END"),
		SUCCESSED("SUCCESSED", "TASK NORMAL END");

		private String name;
		private String desc;

		/**
		 * 枚举构造函数
		 *
		 * @param name 枚举名称
		 * @param desc 枚举描述
		 */
		TaskStatus(String naeName, String newDesc) {
			name = naeName;
			desc = newDesc;
		}

		@Override
		public String toString() {
			return String.format("%s:%s", name, desc);
		}
	}

	public static class Meta {
		public final static String EMPTY = "";
		public final static String KEY_ARGS = "args";
		public final static String KEY_ID = "id";
		public final static String KEY_COMMAND = "command";
		public final static String KEY_DESC = "desc";
		public final static String KEY_PREDECESSORS = "predecessors";
		public final static String KEY_SUB_TASKS = "subtasks";
		public final static String KEY_TRIGGER = "trigger";
		public final static String KEY_TYPE = "type";
		public final static String VALUE_TYPE_NET = "net";
		public final static String VALUE_TYPE_TASK = "task";
		public final static String VALUE_TYPE_UNIT = "unit";

		private final String id;
		private String desc;
		private String type;
		private String command;
		private String args;

		public Meta(String newId) {
			id = newId;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the desc
		 */
		public String getDesc() {
			return desc;
		}

		/**
		 * @param newDesc the desc to set
		 */
		public void setDesc(String newDesc) {
			desc = newDesc;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param newType the type to set
		 */
		public void setType(String newType) {
			type = newType;
		}

		/**
		 * @return the command
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * @param newCommand the command to set
		 */
		public void setCommand(String newCommand) {
			command = newCommand;
		}

		/**
		 * @return the args
		 */
		public String getArgs() {
			return args;
		}

		/**
		 * @param newArgs the args to set
		 */
		public void setArgs(String newArgs) {
			args = newArgs;
		}

		@Override
		public String toString() {
			return String.format("[id:%s] [desc:%s] [type:%s] [command:%s] [args:%s]", id, desc, type, command, args);
		}
	}

	public static class Node {
		public final static String ROOT_NODE = "root";
		/** 逻辑前驱 */
		private final List<Node> predecessors = new ArrayList<>();
		/** 逻辑后继 */
		private final List<Node> successors = new ArrayList<>();
		/** 物理后继 */
		private final List<Node> children = new ArrayList<>();
		private final Meta meta;
		private Node parent;
		private String url;
		private volatile TaskStatus status;
		
		public Node(Meta newMeta) {
			meta = newMeta;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node newParent) {
			parent = newParent;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String newUrl) {
			url = newUrl;
		}

		public Meta getMeta() {
			return meta;
		}

		public TaskStatus getStatus() {
			return status;
		}

		public void setStatus(TaskStatus newStatus) {
			status = newStatus;
		}

		public List<Node> getSuccessors() {
			return successors;
		}

		public void addSuccessors(Node node) {
			successors.add(node);
		}

		public List<Node> getPredecessors() {
			return predecessors;
		}

		public void addPredecessors(Node node) {
			predecessors.add(node);
		}

		public List<Node> getChildren() {
			return children;
		}

		public void addChild(Node node) {
			children.add(node);
		}

		public String toString() {
			return url;
		}

		/**
		 * 检查当前NODE的所有前驱节点是否都已执行成功
		 * 
		 * @return boolean true: 所有前驱执行成功 false: 任意一前驱未执行完成或异常结束
		 */
		public boolean isConditionComplete() {
			boolean complete = true;
			for (Node prevNode : predecessors) {
				if (TaskStatus.SUCCESSED != prevNode.status) {
					complete = false;
					break;
				}
			}
			return complete;
		}
	}

	public static class Net {
		private final String url;
		private final Meta rootMeta;
		private final List<Node> headNodes;
		private final Map<String, Node> nodePool;
		private volatile TaskStatus status;

		public Net(Node rootNode) {
			url = rootNode.getUrl();
			rootMeta = rootNode.getMeta();
			headNodes = new ArrayList<>();
			nodePool = new HashMap<>();
			traverseTaskNodeTree(rootNode);
			status = TaskStatus.UNKNOWN;
		}

		public List<Node> getHeadNodes() {
			return headNodes;
		}

		public TaskStatus getStatus() {
			return status;
		}

		public String getUrl() {
			return url;
		}

		public void standby() {
			status = TaskStatus.STANDBY;
		}

		public void start() {
			// 所有node进入standby状态
			for (Node node : nodePool.values()) {
				resetNodeState(node, TaskStatus.STANDBY);
			}
			status = TaskStatus.RUNNING;
		}

		public void pause() {
			if (TaskStatus.RUNNING == status) {
				throw new RuntimeException(String.format("TaskNet[%s]已经进入RUNNING状态，无法暂停", url));
			}
			status = TaskStatus.SUSPENDED;
		}

		public void finish() {
			status = TaskStatus.SUCCESSED;
		}

		public void error() {
			status = TaskStatus.ERROR;
		}

		/**
		 * 更新指定URL的NODE的状态
		 * 
		 * @param url      更新目标节点URL
		 * @param newState 目标节点新状态
		 * @return Node 被更新的目标节点实例
		 */
		public Node updateNodeState(String url, TaskStatus newState) {
			Optional<Node> targetNodeOpt = Optional.ofNullable(nodePool.get(url));
			if (targetNodeOpt.isEmpty()) {
				throw new RuntimeException(String.format("不存在url为%s的TaskNode对象", url));
			}
			// 更新目标节点状态
			Node targetNode = targetNodeOpt.get();
			targetNode.setStatus(newState);
			// 父节点为UNIT时，更新UNIT父节点的状态
			Node parent = targetNode.getParent();
			if (Meta.VALUE_TYPE_UNIT.equalsIgnoreCase(parent.getMeta().getType())) {
				if (TaskStatus.RUNNING == newState || TaskStatus.ERROR == newState) {
					parent.setStatus(newState);
				} else if (TaskStatus.SUCCESSED == newState) {
					// 当UNIT父节点下所有子节点都成功时才更新父节点至成功状态
					long nonSuccessedCnt = parent.getChildren().stream()
							.filter(child -> TaskStatus.SUCCESSED != child.getStatus()).count();
					if (nonSuccessedCnt == 0) {
						parent.setStatus(newState);
					}
				}
			}
			return targetNode;
		}

		/**
		 * 检查当前NET中所有NODE是否都已执行成功
		 * 
		 * @return boolean true: 所有NODE执行成功 false: 任意一NODE未执行完成或异常结束
		 */
		public boolean isFinished() {
			boolean complete = true;
			for (Node node : nodePool.values()) {
				if (TaskStatus.SUCCESSED != node.getStatus()) {
					complete = false;
					break;
				}
			}
			return complete;
		}

		/**
		 * 遍历节点树，保存节点信息
		 * 
		 * @param Node 遍历目标节点实例
		 */
		private void traverseTaskNodeTree(Node node) {
			for (Node childNode : node.getChildren()) {
				nodePool.put(childNode.getUrl(), childNode);
				// 物理父节点为net节点并且逻辑前驱为空时，添加此节点为头节点
				if (this.rootMeta.getId().equalsIgnoreCase(childNode.getParent().getMeta().getId())
						&& childNode.getPredecessors().isEmpty()) {
					headNodes.add(childNode);
				}
				if (!childNode.getChildren().isEmpty()) {
					traverseTaskNodeTree(childNode);
				}
			}
		}

		private void resetNodeState(Node node, TaskStatus newState) {
			node.setStatus(newState);
			for (Node child : node.getChildren()) {
				resetNodeState(child, newState);
			}
		}
	}
}
