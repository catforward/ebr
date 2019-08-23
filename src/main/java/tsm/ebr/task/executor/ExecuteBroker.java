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

import tsm.ebr.base.Broker.BaseBroker;
import tsm.ebr.base.Broker.Id;
import tsm.ebr.base.Const;
import tsm.ebr.base.Message;
import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Task.PerformableTask;
import tsm.ebr.base.Task.State;
import tsm.ebr.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * <pre>
 * 外部程序执行监视处理类
 * </pre>
 *
 * @author catforward
 */
public class ExecuteBroker extends BaseBroker {
    private final Logger logger = Logger.getLogger(ExecuteBroker.class.getCanonicalName());

    public ExecuteBroker() {
    }

    @Override
    public Id id() {
        return Id.EXECUTOR;
    }

    @Override
    protected void onInit() {
        register(Symbols.MSG_ACT_LAUNCH_TASK_UNITS);
        register(Symbols.MSG_ACT_LAUNCH_TASK_UNIT);
    }

    @Override
    protected void onFinish() {
        unregister(Symbols.MSG_ACT_LAUNCH_TASK_UNITS);
        unregister(Symbols.MSG_ACT_LAUNCH_TASK_UNIT);
    }

    @Override
    protected void onMessage(Message message) {
        switch (message.act) {
            case Symbols.MSG_ACT_LAUNCH_TASK_UNITS: {
                List<PerformableTask> uList = (List<PerformableTask>) message.param.get(Symbols.MSG_DATA_TASK_PERFORMABLE_UNITS_LIST);
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
            case Symbols.MSG_ACT_LAUNCH_TASK_UNIT: {
                String url = (String) message.param.get(Symbols.MSG_DATA_TASK_UNIT_URL);
                String command = (String) message.param.get(Symbols.MSG_DATA_TASK_UNIT_COMMAND);
                if (command != null && !command.isBlank()) {
                    doLaunch(url, command);
                } else {
                    prepareLaunchFlow(url);
                }
                break;
            }
            default: {
                throw new RuntimeException(String.format("[%s]:送错地方了老兄...", message.act));
            }
        }
    }

    /**
     * <pre>
     * 通知其他服务一个Task的新状态
     * </pre>
     * @param url task的识别url
     * @param newState task的新状态
     */
    void noticeNewState(String url, State newState) {
        HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
        param.put(Symbols.MSG_DATA_TASK_UNIT_URL, url);
        param.put(Symbols.MSG_DATA_TASK_UNIT_NEW_STATE, newState);
        post(Symbols.MSG_ACT_TASK_UNIT_STATE_CHANGED, param);
    }

    /**
     * <pre>
     * 通知其他服务启动一个TaskFlow对象
     * </pre>
     * @param url taskflow的识别url
     */
    void prepareLaunchFlow(String url) {
        HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
        param.put(Symbols.MSG_DATA_TASK_FLOW_URL, url);
        post(Symbols.MSG_ACT_LAUNCH_TASK_FLOW, param);
    }

    /**
     * <pre>
     * 通知其他服务启动若干个TaskFlow对象
     * </pre>
     * @param urls taskflow的识别url列表
     */
    void prepareLaunchFlows(ArrayList<String> urls) {
        if (!urls.isEmpty()) {
            HashMap<String, Object> param = new HashMap<>(Const.INIT_CAP);
            param.put(Symbols.MSG_DATA_TASK_FLOW_URL_LIST, urls);
            post(Symbols.MSG_ACT_LAUNCH_TASK_FLOWS, param);
        }
    }

    /**
     * <pre>
     * 启动一个Task
     * </pre>
     * @param url task的识别url
     * @param command task的可执行外部命令
     */
    private void doLaunch(String url, String command) {
        noticeNewState(url, State.RUNNING);
        logger.info(String.format("Task启动%s", url));
        deployTask(() -> {
            try {
                Process process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.fine(line);
                    }
                }

                int exitCode = process.waitFor();
                logger.fine("exitCode = " + exitCode);
                State exitState = (exitCode == 0) ? State.SUCCEEDED : State.ERROR;
                noticeNewState(url, exitState);
            } catch (IOException | InterruptedException e) {
                LogUtils.dumpError(e);
                noticeNewState(url, State.ERROR);
            }
        });
    }
}
