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
package tsm.ebr.handlers;

import static tsm.ebr.base.Handler.HandlerEvent.Const.ACT_LAUNCH_TASK_UNIT;
import static tsm.ebr.base.Handler.HandlerEvent.Const.ACT_TASK_GRAPH_STATE_CHANGED;
import static tsm.ebr.base.Handler.HandlerEvent.Const.ACT_TASK_UNIT_STATE_CHANGED;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_GRAPH;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_GRAPH_NEW_STATE;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_COMMAND;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_NEW_STATE;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_URL;
import static tsm.ebr.base.Handler.HandlerEvent.Const.FLG;
import static tsm.ebr.base.Handler.HandlerEvent.Const.FLG_AUTO_START;
import static tsm.ebr.base.HandlerId.TASK_APP;
import static tsm.ebr.base.HandlerId.TASK_EXECUTOR;
import static tsm.ebr.base.HandlerId.TASK_MANAGER;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import tsm.ebr.base.Application;
import tsm.ebr.base.Handler;
import tsm.ebr.base.data.TaskFlow;
import tsm.ebr.base.data.TaskUnit;
import tsm.ebr.base.data.TaskUnit.State;
import tsm.ebr.base.data.TaskUnit.Type;
import tsm.ebr.base.utils.LogUtils;

/**
 * <pre>
 * 任务执行管理处理类
 * </pre>
 * 
 * @author catforward
 */
public final class TaskManager extends Handler {
	private Logger logger = Logger.getLogger(TaskManager.class.getName());
	private TaskFlow taskGraphObj;

	public TaskManager(Application app) {
		super(app);
	}

	@Override
	public void onInit() {
		application.getEventBus().register(this);
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
				onAddNewManagementItem(event);
				break;
			}
			case ACT_LAUNCH_TASK_GRAPH: {
				onLaunchTaskGraph(event);
				break;
			}
			case ACT_TASK_UNIT_STATE_CHANGED: {
				onTaskUnitStateChanged(event);
				break;
			}
			case ACT_TASK_GRAPH_STATE_CHANGED: {
				onTaskGraphStateChanged(event);
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
			LogUtils.dumpException(ex);
			finishNoticeFrom(TASK_MANAGER);
		}
	}

	/**
	 * <pre>
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onAddNewManagementItem(HandlerEvent event) {
		taskGraphObj = (TaskFlow) event.getParam(DATA_TASK_GRAPH);
		HandlerEvent.Const flg = (HandlerEvent.Const) event.getParam(FLG);
		if (!event.isEmptyValue(taskGraphObj)) {
			taskGraphObj.standby();
		}
		if (FLG_AUTO_START == flg) {
			onLaunchTaskGraph(event);
		}
	}

	/**
	 * <pre>
	 * 启动一个TaskGraph实例
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onLaunchTaskGraph(HandlerEvent event) {
		if (taskGraphObj == null) {
			throw new RuntimeException("TaskGraph实例还没生成阿！老哥！");
		}
		taskGraphObj.start();
		headUnitsOf(taskGraphObj.rootUnit).forEach(unit -> {
			postMessage(new HandlerEvent()
					.setSrc(TASK_MANAGER)
					.setDst(TASK_EXECUTOR)
					.setAct(ACT_LAUNCH_TASK_UNIT)
					.addParam(DATA_TASK_UNIT_URL, unit.getUrl())
					.addParam(DATA_TASK_UNIT_COMMAND, unit.getCommand()));
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
	private void onTaskUnitStateChanged(HandlerEvent event) {
		// 数据准备
		String unitUrl = (String) event.getParam(DATA_TASK_UNIT_URL);
		State newState = (State) event.getParam(DATA_TASK_UNIT_NEW_STATE);
		logger.info(String.format("Unit新状态[%s]::%s", newState.name(), unitUrl));
		TaskUnit currentUnit = taskGraphObj.updateUnitState(unitUrl, newState);
		if (currentUnit.parent != null
				&& Type.MODULE == currentUnit.parent.getType()
				&& currentUnit.parent.isSuccessed()) {
			postMessage(new HandlerEvent()
					.setSrc(TASK_EXECUTOR)
					.setDst(TASK_MANAGER)
					.setAct(ACT_TASK_UNIT_STATE_CHANGED)
					.addParam(DATA_TASK_UNIT_URL, currentUnit.parent.getUrl())
					.addParam(DATA_TASK_UNIT_NEW_STATE, currentUnit.parent.getState()));
		}
		// 后继节点状态检查
		tryToLaunchSuccessor(currentUnit);
		// TaskNet状态检查
		if (taskGraphObj.isComplete()) {
			postMessage(new HandlerEvent()
					.setSrc(TASK_MANAGER)
					.setDst(TASK_MANAGER)
					.setAct(ACT_TASK_GRAPH_STATE_CHANGED)
					.addParam(DATA_TASK_GRAPH_NEW_STATE, State.SUCCESSED));
		}
	}

	/**
	 * <pre>
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void onTaskGraphStateChanged(HandlerEvent event) {
		// 数据准备
		State newState = (State) event.getParam(DATA_TASK_GRAPH_NEW_STATE);
		logger.info(String.format("TaskGraph新状态[%s]::%s", newState.name(), taskGraphObj.rootUnit.getUrl()));
		taskGraphObj.rootUnit.updateState(newState);
		if (State.SUCCESSED == taskGraphObj.rootUnit.getState()
				|| State.ERROR == taskGraphObj.rootUnit.getState()) {
			finishNoticeFrom(TASK_MANAGER);
		}
	}

	/**
	 * <pre>
	 * 启动一个任务单元的后续单元的任务
	 * </pre>
	 * 
	 * @param currentTaskUnit 当前任务单元
	 */
	private void tryToLaunchSuccessor(TaskUnit currentTaskUnit) {
		// 后继节点
		for (TaskUnit unit : taskGraphObj.getSuccessorsOf(currentTaskUnit)) {
			if (State.STANDBY == unit.getState()
					&& taskGraphObj.isConditionCompleteOf(unit)) {
				preformSuccessor(unit);
			}
		}
	}

	/**
	 * <pre>
	 * 启动一个任务单元的后续单元的任务
	 * </pre>
	 * 
	 * @param targetTaskUnit 当前任务单元
	 */
	private void preformSuccessor(TaskUnit targetTaskUnit) {
		// TASK节点
		if (Type.TASK == targetTaskUnit.getType()) {
			postMessage(new HandlerEvent()
					.setSrc(TASK_MANAGER)
					.setDst(TASK_EXECUTOR)
					.setAct(ACT_LAUNCH_TASK_UNIT)
					.addParam(DATA_TASK_UNIT_URL, targetTaskUnit.getUrl())
					.addParam(DATA_TASK_UNIT_COMMAND, targetTaskUnit.getCommand()));
		}
		// MODULE节点
		else if (Type.MODULE == targetTaskUnit.getType()) {
			headUnitsOf(targetTaskUnit).forEach(childUnit -> {
				postMessage(new HandlerEvent()
						.setSrc(TASK_MANAGER)
						.setDst(TASK_EXECUTOR)
						.setAct(ACT_LAUNCH_TASK_UNIT)
						.addParam(DATA_TASK_UNIT_URL, childUnit.getUrl())
						.addParam(DATA_TASK_UNIT_COMMAND, childUnit.getCommand()));
			});;
		}
	}

	/**
	 * <pre>
	 * 取得一个任务集的根任务集合
	 * </pre>
	 * 
	 * @param targetUnit 当前任务单元
	 */
	private List<TaskUnit> headUnitsOf(TaskUnit targetUnit) {
		if (Type.ROOT == targetUnit.getType() || Type.MODULE == targetUnit.getType()) {
			return targetUnit.children.stream().filter(child -> child.getPredecessorsId().isEmpty())
					.collect(Collectors.toList());
		} else {
			return List.of();
		}
	}
}
