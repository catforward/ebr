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

import static tsm.one.ebr.base.Handler.HandlerEvent.Const.ACT;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DST;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.SRC;
import static tsm.one.ebr.base.utils.ConfigUtils.Item.KEY_OUTPUT_EVENTLOG;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import tsm.one.ebr.base.Handler.HandlerEvent;
import tsm.one.ebr.base.Handler.HandlerStatus;
import tsm.one.ebr.base.utils.ConfigUtils;

/**
 * <pre>
 * 管理所有Service实例
 * 基本处理内容：
 *  - （子类）注册Service实例
 *  - 创建并启动全局事件总线
 *  - 以一定时间间隔检查所有Service实例的状态
 *   -- 全部达到Finished时结束主循环
 *  - 主循环退出后结束全局事件总线
 * </pre>
 * 
 * @author catforward
 */
public abstract class Application {

	private final Logger logger = Logger.getLogger(Application.class.getName());
	/** 主循环停止标志 */
	private volatile boolean terminated;
	/** 提供具体功能的处理类实例 */
	private final Map<HandlerId, Handler> serviceMap;
	/** 事件执行线程池 （本应用同一时刻只有1个线程来执行具体处理，不考虑耗时操作的情况，因为没有打算写耗时处理） */
	private final ExecutorService eventExecutorThreadPool;
	/** 事件总线 */
	private final EventBus eventBus;

	protected Application() {
		terminated = false;
		serviceMap = new LinkedHashMap<>();
		eventExecutorThreadPool = Executors.newFixedThreadPool(1);
		eventBus = new AsyncEventBus("EBR_EVENT_BUS", eventExecutorThreadPool);
	}

	/**
	 * <pre>
	 * 应用程序初始化
	 * 子类onInit函数中添加所需具体功能的处理类实例
	 * </pre>
	 */
	protected void init() {
		eventBus.register(this);
		onInit();
		serviceMap.forEach((k, v) -> v.init());
	}

	/**
	 * <pre>
	 * 主循环
	 * 启动服务实例 以1秒间隔监视各个服务实例状态
	 * 当所有服务实例的状态都处于finished状态时，退出主循环即程序结束
	 * </pre>
	 */
	protected void start() {
		logger.info("EBR MAIN LOOP START!");
		while (!terminated) {
			boolean willTerminate = true;
			try {
				var entries = serviceMap.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<HandlerId, Handler> entry = entries.next();
					Handler service = entry.getValue();
					// start service
					if (HandlerStatus.INITIALIZED == service.status()) {
						service.start();
					}
					// status checker
					if (willTerminate && HandlerStatus.FINISHED != service.status()) {
						willTerminate = false;
					}
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO
				e.printStackTrace();
			} finally {
				terminated = willTerminate;
			}
		}
		eventExecutorThreadPool.shutdown();
		logger.info("EBR MAIN LOOP FINISHED!");
	}

	/**
	 * <pre>
	 * 添加提供具体功能的处理类实例
	 * </pre>
	 * 
	 * @param id      处理类标识ID
	 * @param service 处理类实例
	 */
	protected void appendHandler(HandlerId id, Handler service) {
		serviceMap.put(id, service);
	}

	/**
	 * <pre>
	 * 由子类实现并添加功能类实例
	 * </pre>
	 */
	protected abstract void onInit();

	/**
	 * <pre>
	 * 向日志文件中记录事件总线中传递的所有事件信息
	 * 当ebr.properties的ebr.output.event.log为false时，
	 * 关闭记录功能
	 * </pre>
	 * 
	 * @param event 事件总线中传递的事件
	 */
	@Subscribe
	public final void eventLog(HandlerEvent event) {
		if (Boolean.getBoolean((String) ConfigUtils.getOrDefault(KEY_OUTPUT_EVENTLOG, "false"))) {
			logger.fine(String.format("[act]: %s :: <src>: %s -> <dst>: %s", event.getParam(ACT), event.getParam(SRC),
					event.getParam(DST)));
		}
	}

	/**
	 * <pre>
	 * 取得事件总线实例
	 * </pre>
	 * 
	 * @return 事件总线实例
	 */
	public final EventBus getEventBus() {
		return eventBus;
	}

}
