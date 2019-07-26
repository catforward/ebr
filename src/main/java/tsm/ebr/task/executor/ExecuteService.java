package tsm.ebr.task.executor;

import tsm.ebr.base.Event;
import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Service;
import tsm.ebr.base.Task.State;
import tsm.ebr.base.Task.PerformableTask;
import tsm.ebr.utils.ConfigUtils;
import tsm.ebr.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ExecuteService extends Service {
    private Logger logger = Logger.getLogger(ExecuteService.class.getCanonicalName());
    private ExecutorService taskExecutor;

    public ExecuteService() {
        int maxNum = Integer.parseInt((String) ConfigUtils.getOrDefault(ConfigUtils.Item.KEY_EXCUTOR_NUM_MAX, "0"));
        int workerNum = (maxNum == 0) ? Runtime.getRuntime().availableProcessors() * 2 : maxNum;
        taskExecutor = Executors.newFixedThreadPool(workerNum);
    }
    /**
     * (子类实现)处理类ID
     */
    @Override
    public ServiceId id() {
        return ServiceId.TASK_EXECUTOR;
    }

    /**
     * (子类实现)服务初始化
     */
    @Override
    protected void onInit() {
        //
        register(Symbols.EVT_ACT_LAUNCH_TASK_UNITS);
        //
        register(Symbols.EVT_ACT_LAUNCH_TASK_UNIT);
    }

    /**
     * (子类实现)服务结束
     */
    @Override
    protected void onFinish() {
        unregister(Symbols.EVT_ACT_LAUNCH_TASK_UNITS);
        unregister(Symbols.EVT_ACT_LAUNCH_TASK_UNIT);
        taskExecutor.shutdown();
    }

    /**
     * (子类实现)处理事件
     *
     * @param event
     */
    @Override
    protected void onEvent(Event event) {
        switch (event.act) {
            case Symbols.EVT_ACT_LAUNCH_TASK_UNITS: {
                List<PerformableTask> uList = (List<PerformableTask>) event.getParam(Symbols.EVT_DATA_TASK_PERFORMABLE_UNITS_LIST).get();
                for (var task : uList) {
                    doLaunch(task.url, task.command);
                }
                break;
            }
            case Symbols.EVT_ACT_LAUNCH_TASK_UNIT: {
                String url = (String) event.getParam(Symbols.EVT_DATA_TASK_UNIT_URL).get();
                String command = (String) event.getParam(Symbols.EVT_DATA_TASK_UNIT_COMMAND).get();
                doLaunch(url, command);
                break;
            }
            default: {
                throw new RuntimeException(String.format("[%s]:送错地方了老兄...", event.act));
            }
        }
    }

    void noticeNewState(String url, State newState) {
        HashMap<String, Object> param = new HashMap<>();
        param.put(Symbols.EVT_DATA_TASK_UNIT_URL, url);
        param.put(Symbols.EVT_DATA_TASK_UNIT_NEW_STATE, newState);
        post(Symbols.EVT_ACT_TASK_UNIT_STATE_CHANGED, param);
    }

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
