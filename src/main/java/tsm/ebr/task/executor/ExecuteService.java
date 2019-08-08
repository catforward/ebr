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
package tsm.ebr.task.executor;

import tsm.ebr.base.Const;
import tsm.ebr.base.Event;
import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Service.BaseService;
import tsm.ebr.base.Service.ServiceId;
import tsm.ebr.base.Task.PerformableTask;
import tsm.ebr.base.Task.State;
import tsm.ebr.util.ConfigUtils;
import tsm.ebr.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * <pre>
 * 外部程序执行监视处理类
 * </pre>
 *
 * @author catforward
 */
public class ExecuteService extends BaseService {
    private final Logger logger = Logger.getLogger(ExecuteService.class.getCanonicalName());
    /** 执行队列 */
    private final ThreadPoolExecutor taskExecutor;

    public ExecuteService() {
        int configNum = Integer.parseInt((String) ConfigUtils.getOrDefault(ConfigUtils.Item.KEY_EXCUTOR_NUM_MAX, "0"));
        int minNum = Runtime.getRuntime().availableProcessors();
        int maxNum = (configNum == 0) ? Runtime.getRuntime().availableProcessors() * 2 : configNum;
        taskExecutor = new ThreadPoolExecutor(minNum, maxNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public ServiceId id() {
        return ServiceId.EXECUTOR;
    }

    @Override
    protected void onInit() {
        register(Symbols.EVT_ACT_LAUNCH_TASK_UNITS);
        register(Symbols.EVT_ACT_LAUNCH_TASK_UNIT);
    }

    @Override
    protected void onFinish() {
        unregister(Symbols.EVT_ACT_LAUNCH_TASK_UNITS);
        unregister(Symbols.EVT_ACT_LAUNCH_TASK_UNIT);
        taskExecutor.shutdown();
    }

    @Override
    protected void onEvent(Event event) {
        switch (event.act) {
            case Symbols.EVT_ACT_LAUNCH_TASK_UNITS: {
                List<PerformableTask> uList = (List<PerformableTask>) event.param.get(Symbols.EVT_DATA_TASK_PERFORMABLE_UNITS_LIST);
                ArrayList<String> flowUrl = new ArrayList<>();
                for (var task : uList) {
                    if (task.command != null && !task.command.isBlank()) {
                        doLaunch(task.url, task.command);
                    } else {
                        flowUrl.add(task.url);
                    }
                }
                prepareLaunchFlows(flowUrl);
                break;
            }
            case Symbols.EVT_ACT_LAUNCH_TASK_UNIT: {
                String url = (String) event.param.get(Symbols.EVT_DATA_TASK_UNIT_URL);
                String command = (String) event.param.get(Symbols.EVT_DATA_TASK_UNIT_COMMAND);
                if (command != null && !command.isBlank()) {
                    doLaunch(url, command);
                } else {
                    prepareLaunchFlow(url);
                }
                break;
            }
            default: {
                throw new RuntimeException(String.format("[%s]:送错地方了老兄...", event.act));
            }
        }
    }

    /**
     * 通知其他服务一个Task的新状态
     * @param url task的识别url
     * @param newState task的新状态
     */
    void noticeNewState(String url, State newState) {
        HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
        param.put(Symbols.EVT_DATA_TASK_UNIT_URL, url);
        param.put(Symbols.EVT_DATA_TASK_UNIT_NEW_STATE, newState);
        post(Symbols.EVT_ACT_TASK_UNIT_STATE_CHANGED, param);
    }

    /**
     * 通知其他服务启动一个TaskFlow对象
     * @param url taskflow的识别url
     */
    void prepareLaunchFlow(String url) {
        HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
        param.put(Symbols.EVT_DATA_TASK_FLOW_URL, url);
        post(Symbols.EVT_ACT_LAUNCH_TASK_FLOW, param);
    }

    /**
     * 通知其他服务启动若干个TaskFlow对象
     * @param urls taskflow的识别url列表
     */
    void prepareLaunchFlows(ArrayList<String> urls) {
        if (!urls.isEmpty()) {
            HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
            param.put(Symbols.EVT_DATA_TASK_FLOW_URL_LIST, urls);
            post(Symbols.EVT_ACT_LAUNCH_TASK_FLOWS, param);
        }
    }

    /**
     * 启动一个Task
     * @param url task的识别url
     * @param command task的可执行外部命令
     */
    private void doLaunch(String url, String command) {
        noticeNewState(url, State.RUNNING);
		logger.info(String.format("Task启动%s", url));
        taskExecutor.submit(() -> {
			TaskWatcher watcher = new TaskWatcher(ExecuteService.this, url, command);
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
	private final Logger logger = Logger.getLogger(TaskWatcher.class.getCanonicalName());
	private final ExecuteService taskExecutor;
	private final String taskUrl;
	private final String taskCommand;

	TaskWatcher(ExecuteService executor, String tUrl, String command) {
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
			State exitState = (exitCode == 0) ? State.SUCCEEDED : State.ERROR;

            taskExecutor.noticeNewState(taskUrl, exitState);
		} catch (IOException | InterruptedException e) {
			LogUtils.dumpError(e);
			taskExecutor.noticeNewState(taskUrl, State.ERROR);
		}
	}
}
