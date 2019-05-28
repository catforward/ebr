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
package tsm.one.ebr.handlers;

import static tsm.one.ebr.base.Handler.HandlerEvent.Const.ACT_EXEC_NODE;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.ACT_NET_STATE_CHANGED;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_META;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_NET;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_NET_INSTANCE_ID;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_NODE_NEW_STATE;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_URL;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.FLG;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.FLG_AUTO_START;
import static tsm.one.ebr.base.HandlerId.TASK_APP;
import static tsm.one.ebr.base.HandlerId.TASK_EXECUTOR;
import static tsm.one.ebr.base.HandlerId.TASK_MANAGER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import tsm.one.ebr.base.Application;
import tsm.one.ebr.base.Handler;
import tsm.one.ebr.base.Task.Meta;
import tsm.one.ebr.base.Task.Net;
import tsm.one.ebr.base.Task.Node;
import tsm.one.ebr.base.Task.TaskStatus;

/**
 * <pre>
 * 任务执行管理处理类
 * </pre>
 * 
 * @author catforward
 */
public final class TaskManager extends Handler {
	Logger logger = Logger.getLogger(TaskManager.class.getName());
	private Map<String, Net> standbyTaskNetPool;
	private Map<String, Net> runningTaskNetPool;

	public TaskManager(Application app) {
		super(app);
	}

	@Override
	public void onInit() {
		application.getEventBus().register(this);
		standbyTaskNetPool = new HashMap<>();
		runningTaskNetPool = new HashMap<>();
		logger.fine("init OK");
	}

	@Override
	public void onStart() {
		logger.fine("start OK");
	}

	@Override
	public void onFinish() {
		application.getEventBus().unregister(this);
		logger.fine("finish OK");
	}

	@Subscribe
	public void onServiceEvent(HandlerEvent event) {
		if (TASK_APP != event.getDst() && TASK_MANAGER != event.getDst()) {
			return;
		}
		try {
			switch (event.getAct()) {
			case ACT_MANAGEMENT_APPEND: {
				onAddNewTaskNet(event);
				break;
			}
			case ACT_EXEC_NET: {
				onExecTaskNet(event);
				break;
			}
			case ACT_NODE_STATE_CHANGED: {
				onTaskNodeStateChanged(event);
				break;
			}
			case ACT_NET_STATE_CHANGED: {
				onTaskNetStateChanged(event);
				break;
			}
			case ACT_SERV_SHUTDOWN: {
				finish();
				break;
			}
			default:
				break;
			}
		} catch (Exception ex) {
			logger.severe(ex.getLocalizedMessage());
			finishNoticeFrom(TASK_MANAGER);
		}
	}

	/**
	 * <pre>
	 * 添加一个新的tasknet实例进入管理对象池
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onAddNewTaskNet(HandlerEvent event) {
		Net taskNet = (Net) event.getParam(DATA_TASK_NET);
		HandlerEvent.Const flg = (HandlerEvent.Const) event.getParam(FLG);
		if (!event.isEmptyValue(taskNet)) {
			addNewTaskNet(taskNet);
		}
		if (FLG_AUTO_START == flg) {
			onExecTaskNet(event);
		}
	}

	/**
	 * <pre>
	 * 启动一个tasknet实例
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onExecTaskNet(HandlerEvent event) {
		Net taskNet = (Net) event.getParam(DATA_TASK_NET);
		String url = taskNet.getUrl();
		String instanceId = execTaskNet(url);
		listOfHeadNodes(instanceId).forEach(node -> {
			postMessage(new HandlerEvent()
					.setSrc(TASK_MANAGER)
					.setDst(TASK_EXECUTOR)
					.setAct(ACT_EXEC_NODE)
					.addParam(DATA_TASK_NET_INSTANCE_ID, instanceId)
					.addParam(DATA_TASK_URL, node.getUrl())
					.addParam(DATA_TASK_META, node.getMeta()));
		});
	}

	/**
	 * <pre>
	 * 接受并处理任务状态变更事件
	 * 当任务集所有任务都正常结束时发送任务集完成事件
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onTaskNodeStateChanged(HandlerEvent event) {
		// 数据准备
		String instanceId = (String) event.getParam(DATA_TASK_NET_INSTANCE_ID);
		String url = (String) event.getParam(DATA_TASK_URL);
		TaskStatus newState = (TaskStatus) event.getParam(DATA_TASK_NODE_NEW_STATE);
		logger.info(String.format("Task新状态[%s]%s::%s", newState.toString(), instanceId, url));
		// 处理对象
		Optional<Net> targetNetOpt = Optional.ofNullable(runningTaskNetPool.get(instanceId));
		if (targetNetOpt.isEmpty()) {
			throw new RuntimeException(String.format("不存在instanceId为%s的运行中的TaskNet对象", instanceId));
		}
		Net targetNet = targetNetOpt.get();
		Node currentNode = targetNet.updateNodeState(url, newState);
		// 后继节点状态检查
		preformSuccessorNode(instanceId, currentNode);
		// TaskNet状态检查
		if (targetNet.isFinished()) {
			postMessage(new HandlerEvent()
					.setSrc(TASK_MANAGER)
					.setDst(TASK_MANAGER)
					.setAct(ACT_NET_STATE_CHANGED)
					.addParam(DATA_TASK_NET_INSTANCE_ID, instanceId));
		}
	}

	/**
	 * <pre>
	 * 启动一个指定instanceId的任务集
	 * 如果指定的任务集不存在则通知其他部件处理结束
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onTaskNetStateChanged(HandlerEvent event) {
		// 数据准备
		String instanceId = (String) event.getParam(DATA_TASK_NET_INSTANCE_ID);
		// 处理对象
		Optional<Net> targetNetOpt = Optional.ofNullable(runningTaskNetPool.get(instanceId));
		if (targetNetOpt.isEmpty()) {
			throw new RuntimeException(String.format("不存在instanceId为%s的运行中的TaskNet对象", instanceId));
		}
		Net net = targetNetOpt.get();
		standbyTaskNetPool.put(net.getUrl(), net);
		runningTaskNetPool.remove(instanceId);
		if (runningTaskNetPool.isEmpty()) {
			finishNoticeFrom(TASK_MANAGER);
		}
	}

	/**
	 * <pre>
	 * 启动一个任务节点的后续节点的任务
	 * </pre>
	 * 
	 * @param instanceId  任务集运行期识别ID
	 * @param currentNode 当前任务节点
	 */
	private void preformSuccessorNode(String instanceId, Node currentNode) {
		// 后继节点
		for (Node node : currentNode.getSuccessors()) {
			preformSuccessorByType(instanceId, node);
		}
		// 父节点为UNIT时,表明当前节点处于一个UNIT中，需要启动的实际是UNIT的后继
		// 如果UNIT没有全部完成则尝试启动UNIT的后继也不会有效果，效率堪忧所以-〉TODO
		Node parentNode = currentNode.getParent();
		if (Meta.VALUE_TYPE_UNIT.equalsIgnoreCase(parentNode.getMeta().getType())) {
			for (Node node : parentNode.getSuccessors()) {
				preformSuccessorByType(instanceId, node);
			}
		}
	}

	/**
	 * <pre>
	 * 启动一个任务节点的后续节点的任务
	 * </pre>
	 * 
	 * @param instanceId  任务集运行期识别ID
	 * @param currentNode 当前任务节点
	 */
	private void preformSuccessorByType(String instanceId, Node node) {
		if (TaskStatus.STANDBY == node.getStatus() && node.isConditionComplete()) {
			String nodeType = node.getMeta().getType();
			// TASK节点
			if (Meta.VALUE_TYPE_TASK.equalsIgnoreCase(nodeType)) {
				postMessage(new HandlerEvent()
						.setSrc(TASK_MANAGER)
						.setDst(TASK_EXECUTOR)
						.setAct(ACT_EXEC_NODE)
						.addParam(DATA_TASK_NET_INSTANCE_ID, instanceId)
						.addParam(DATA_TASK_URL, node.getUrl())
						.addParam(DATA_TASK_META, node.getMeta()));
			}
			// UNIT节点
			else if (Meta.VALUE_TYPE_UNIT.equalsIgnoreCase(nodeType)) {
				List<Node> children = node.getChildren().stream().filter(child -> child.getPredecessors().isEmpty())
						.collect(Collectors.toList());
				children.forEach(child -> {
					postMessage(new HandlerEvent()
							.setSrc(TASK_MANAGER)
							.setDst(TASK_EXECUTOR)
							.setAct(ACT_EXEC_NODE)
							.addParam(DATA_TASK_NET_INSTANCE_ID, instanceId)
							.addParam(DATA_TASK_URL, child.getUrl())
							.addParam(DATA_TASK_META, child.getMeta()));
				});
			}
		}
	}

	/**
	 * <pre>
	 * </pre>
	 * 
	 */
	private void addNewTaskNet(Net newNet) {
		newNet.standby();
		standbyTaskNetPool.put(newNet.getUrl(), newNet);
	}

	/**
	 * <pre>
	 * 启动一个任务集
	 * </pre>
	 * 
	 * @param url 目标任务集识别URL
	 */
	private String execTaskNet(String url) {
		Optional<Net> targetNet = Optional.ofNullable(standbyTaskNetPool.get(url));
		if (targetNet.isEmpty()) {
			throw new RuntimeException(String.format("不存在url为%s的TaskNet对象", url));
		}
		String instanceId = UUID.randomUUID().toString();
		Net targetNetValue = targetNet.get();
		targetNetValue.start();
		runningTaskNetPool.put(instanceId, targetNetValue);
		standbyTaskNetPool.remove(url);
		return instanceId;
	}

	/**
	 * <pre>
	 * 取得一个任务集的根任务集合
	 * </pre>
	 * 
	 * @param instanceId 任务集运行期识别ID
	 */
	private List<Node> listOfHeadNodes(String instanceId) {
		Optional<Net> targetNet = Optional.ofNullable(runningTaskNetPool.get(instanceId));
		if (targetNet.isEmpty()) {
			throw new RuntimeException(String.format("不存在instanceId为%s的运行中的TaskNet对象", instanceId));
		}
		return targetNet.get().getHeadNodes();
	}
}
