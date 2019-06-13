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
package tsm.one.ebr;

import static tsm.one.ebr.base.HandlerId.TASK_BUILDER;
import static tsm.one.ebr.base.HandlerId.TASK_EXECUTOR;
import static tsm.one.ebr.base.HandlerId.TASK_MANAGER;

import tsm.one.ebr.base.Application;
import tsm.one.ebr.base.utils.ConfigUtils;
import tsm.one.ebr.base.utils.LogUtils;
import tsm.one.ebr.handlers.TaskBuilder;
import tsm.one.ebr.handlers.TaskExecutor;
import tsm.one.ebr.handlers.TaskManager;
import tsm.one.ebr.thin.GetOpts;

/**
 * <pre>
 * External Batch Runner
 * 解决以下2个问题为目的
 * - 自动化管理并执行若干个，基于流程的处理程序集
 * - 用来学习新API，适应新语法，跟进JDK的版本的更新
 * </pre>
 * 
 * @author catforward
 */
public class Main extends Application {

	public static void main(String[] args) throws Exception {
		//showArgs(args);
		new Main().initAndStart(args);
	}

	public void initAndStart(String[] args) throws Exception {
		// 又不是服务器程序，不处理异常，如果有，那就任其终止程序
		ConfigUtils.init();
		mergeCmdOptionsIntoConfig(args);
		LogUtils.init();
		init();
		start();
	}
	
	private void mergeCmdOptionsIntoConfig(String[] args) {
		GetOpts opts = new GetOpts(args, "t:");
		int c = -1;
		try {
			while ((c = opts.getNextOption()) != -1) {
				char cc = (char) c;
				switch (cc) {
				case 't': {
					ConfigUtils.update(ConfigUtils.Item.KEY_INSTANT_TASK, opts.getOptionArg());
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
	}

	private void showUsage() {
		System.out.println("Usage: <path>/jar_file [-t] <name of task define file>");
		System.exit(1);
	}

	@Override
	protected void onInit() {
		// 在此按需要初始化的顺序来添加处理程序定义
		appendHandler(TASK_EXECUTOR, new TaskExecutor(this));
		appendHandler(TASK_MANAGER, new TaskManager(this));
		appendHandler(TASK_BUILDER, new TaskBuilder(this));
	}
	
//	private static void showArgs(String[] args) {
//		for (int i = 0; i < args.length; ++i) {
//			System.out.println(String.format("arg[%s]:%s", String.valueOf(i), args[i]));
//		}
//	}
}
