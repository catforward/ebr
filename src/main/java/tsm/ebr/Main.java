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
package tsm.ebr;

import tsm.ebr.base.Application;
import tsm.ebr.base.Const;
import tsm.ebr.task.executor.ExecuteService;
import tsm.ebr.util.ConfigUtils;
import tsm.ebr.util.LogUtils;
import tsm.ebr.task.manager.StateManagementService;
import tsm.ebr.task.storage.StorageService;
import tsm.ebr.thin.GetOpts;

import java.util.HashMap;
import java.util.Map;

import static tsm.ebr.base.Service.ServiceId.*;

/**
 * <pre>
 * External Batch Runner
 * 解决以下2个问题为目的
 * - 自动化管理并执行若干个，可并行执行但有依赖关系的外部程序
 * - 用来学习新API，适应新语法，跟进JDK的版本的更新
 * </pre>
 *
 * @author catforward
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //showArgs(args);
        new Main().initAndStart(args);
    }

    public void initAndStart(String[] args) throws Exception {
        // 又不是服务器程序，不处理异常，如果有，那就任其终止程序
        ConfigUtils.init();
        ConfigUtils.merge(makeOptArgMap(args));
        LogUtils.init();
        Application app = Application.init();
        deployServices(app);
        app.run();
    }

    private Map<String, String> makeOptArgMap(String[] args) {
        HashMap<String, String> optArg = new HashMap<>(Const.INIT_CAP);
        GetOpts opts = new GetOpts(args, "t:");
        int c = -1;
        try {
            while ((c = opts.getNextOption()) != -1) {
                char cc = (char) c;
                switch (cc) {
                    case 't': {
                        optArg.put(ConfigUtils.Item.KEY_INSTANT_TASK, opts.getOptionArg());
                        break;
                    }
                    default: {
                        showUsage();
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            showUsage();
            ex.printStackTrace();
        }
        return optArg;
    }

    static void showUsage() {
        System.out.println("Usage: <path>/jar_file [-t] <name of task define file>");
        System.exit(1);
    }

    static void deployServices(Application app) {
        // 在此按需要初始化的顺序来添加处理程序定义
        app.deployService(STORAGE, new StorageService());
        app.deployService(MANAGEMENT, new StateManagementService());
        app.deployService(EXECUTOR, new ExecuteService());
    }

//	static void showArgs(String[] args) {
//		for (int i = 0; i < args.length; ++i) {
//			System.out.println(String.format("arg[%s]:%s", String.valueOf(i), args[i]));
//		}
//	}
}
