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

import static tsm.ebr.base.Handler.HandlerEvent.Const.ACT_TASK_UNIT_STATE_CHANGED;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_COMMAND;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_NEW_STATE;
import static tsm.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_UNIT_URL;
import static tsm.ebr.base.HandlerId.TASK_APP;
import static tsm.ebr.base.HandlerId.TASK_EXECUTOR;
import static tsm.ebr.base.HandlerId.TASK_MANAGER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import tsm.ebr.base.Application;
import tsm.ebr.base.Handler;
import tsm.ebr.base.data.TaskUnit.State;
import tsm.ebr.base.utils.ConfigUtils;

/**
 * <pre>
 * 外部程序执行处理类
 * </pre>
 * 
 * @author catforward
 */
public final class TaskExecutor extends Handler {
	private Logger logger = Logger.getLogger(TaskExecutor.class.getName());
	private ExecutorService taskExecutor;

	public TaskExecutor(Application app) {
		super(app);
	}

	@Override
	public void onInit() {
		application.getEventBus().register(this);
		int tmpNum = Integer.parseInt(ConfigUtils.get(ConfigUtils.Item.KEY_EXCUTOR_NUM));
		int workerNum = (tmpNum == 0) ? Runtime.getRuntime().availableProcessors() : tmpNum;
		taskExecutor = Executors.newFixedThreadPool(workerNum);
		logger.fine("init OK");
	}

	@Override
	public void onStart() {
		logger.fine("start OK");
	}

	@Override
	public void onFinish() {
		taskExecutor.shutdown();
		application.getEventBus().unregister(this);
		logger.fine("finish OK");
	}

	@Subscribe
	public void onServiceEvent(HandlerEvent event) {
		if (TASK_APP != event.getDst() && TASK_EXECUTOR != event.getDst()) {
			return;
		}

		try {
			switch (event.getAct()) {
			case ACT_LAUNCH_TASK_UNIT: {
				execTask(event);
				break;
			}
			case ACT_SERV_SHUTDOWN: {
				this.finish();
				break;
			}
			default:
				break;
			}
		} catch (Exception ex) {
			logger.severe(ex.getLocalizedMessage());
			finishNoticeFrom(TASK_EXECUTOR);
		}
	}

	/**
	 * <pre>
	 * 通知接受方程序的最新状态
	 * </pre>
	 * 
	 * @param taskUrl  状态变更的任务URL
	 * @param newState 任务的最新状态
	 */
	void noticeNewState(String taskUrl, State newState) {
		postMessage(new HandlerEvent()
				.setSrc(TASK_EXECUTOR)
				.setDst(TASK_MANAGER)
				.setAct(ACT_TASK_UNIT_STATE_CHANGED)
				.addParam(DATA_TASK_UNIT_URL, taskUrl)
				.addParam(DATA_TASK_UNIT_NEW_STATE, newState));
	}

	/**
	 * <pre>
	 * 运行一个指定的程序
	 * </pre>
	 * 
	 * @param event 事件类实例 
	 */
	private void execTask(HandlerEvent event) {
		String taskUrl = (String) event.getParam(DATA_TASK_UNIT_URL);
		String command = (String) event.getParam(DATA_TASK_UNIT_COMMAND);
		noticeNewState(taskUrl, State.RUNNING);
		logger.info(String.format("Task启动%s", taskUrl));
		execExternalCommand(taskUrl, command);
	}

	/**
	 * <pre>
	 * 启动一个外部程序
	 * </pre>
	 * 
	 * @param taskUrl     状态变更的任务URL
	 * @param command     启动对象的元数据
	 */
	private void execExternalCommand(String taskUrl, String command) {
		taskExecutor.submit(() -> {
			TaskWatcher watcher = new TaskWatcher(TaskExecutor.this, taskUrl, command);
			watcher.watch();
		});
	}
}

/**
 * <pre>
 * 外部程序执行监视处理类
 * </pre>
 * 
 * @author catforward
 */
class TaskWatcher {
	private final Logger logger = Logger.getLogger(TaskWatcher.class.getName());
	private final TaskExecutor taskExecutor;
	private final String taskUrl;
	private final String taskCommand;

	TaskWatcher(TaskExecutor executor, String tUrl, String command) {
		taskExecutor = executor;
		taskUrl = tUrl;
		taskCommand = command;
	}

	/**
	 * <pre>
	 * 启动并等待任务执行完成
	 * </pre>
	 * 
	 */
	void watch() {
		try {
			Process process = Runtime.getRuntime().exec(taskCommand);
			process.getOutputStream().close();

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
				String line;
				while ((line = br.readLine()) != null) {
					logger.info(line);
				}
			}

			int exitCode = process.waitFor();
			logger.fine("exitCode = " + exitCode);
			State exitState = (exitCode == 0) ? State.SUCCESSED : State.ERROR;
			taskExecutor.noticeNewState(taskUrl, exitState);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			taskExecutor.noticeNewState(taskUrl, State.ERROR);
		}
	}
}
