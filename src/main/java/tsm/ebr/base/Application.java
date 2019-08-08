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
package tsm.ebr.base;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tsm.ebr.base.Service.BaseService;
import tsm.ebr.base.Service.ServiceId;
import tsm.ebr.util.LogUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static tsm.ebr.base.Event.Symbols.*;
import static tsm.ebr.base.Service.ServiceId.APP;

/**
 * <pre>
 * 管理所有Service实例
 * 基本处理内容：
 *  主线程
 *  - 创建并启动全局事件总线
 *  - 以一定时间间隔检查所有Service实例的状态
 *   -- 全部达到Finished时结束主循环
 *  - 主循环退出后结束全局事件总线
 *  事件分配线程
 *  - 将事件派发至注册的Service中
 * </pre>
 *
 * @author catforward
 */
public final class Application {

    private final static Logger logger = Logger.getLogger(Application.class.getCanonicalName());
    /** 主线程循环停止标志 */
    private boolean terminated;
    /** 提供具体功能的处理类实例 */
    private final Map<ServiceId, BaseService> servPool;
    /** 事件执行线程池 （本应用同一时刻只有1个线程来执行具体处理，不考虑耗时操作的情况，因为没有打算写耗时处理） */
    private final ExecutorService singleEventDispatcher;
    /** 事件总线 */
    private final EventBus eventBus;
    /** 单例 */
    private final static Application INSTANCE = new Application();

    /**
     * 初始化所有资源
     */
    private Application() {
        terminated = false;
        servPool = new LinkedHashMap<>(Const.INIT_CAP);
        singleEventDispatcher = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>(1024), new ThreadPoolExecutor.AbortPolicy());
        eventBus = new AsyncEventBus("EBR_EVENT_BUS", singleEventDispatcher);
    }

    /**
     * <pre>
     * 应用程序初始化
     * </pre>
     */
    public static Application init() {
        INSTANCE.eventBus.register(INSTANCE);
        return INSTANCE;
    }

    /**
     * <pre>
     * 应用程序结束
     * </pre>
     */
    private void finish() {
        eventBus.unregister(INSTANCE);
        singleEventDispatcher.shutdown();
    }

    /**
     * <pre>
     * 取得事件总线实例
     * </pre>
     *
     * @return EventBus 事件总线实例
     */
    static EventBus getEventBus() {
        return INSTANCE.eventBus;
    }

    /**
     * <pre>
     * 添加提供具体功能的处理类实例
     * </pre>
     *
     * @param id    服务类标识ID
     * @param service  处理类实例
     */
    public void deployService(ServiceId id, BaseService service) {
        if (id == null || service == null) {
            throw new IllegalArgumentException("argument can not be null...");
        }
        if (!servPool.containsKey(id)) {
            servPool.put(id, service);
        }
    }

    /**
     * <pre>
     * 开始并进入主循环
     * </pre>
     */
    public void run() {
        eventBus.post(new Event(EVT_ACT_SERVICE_INIT, APP, APP));
        mainLoop();
        finish();
    }

    /**
     * <pre>
     * 主循环
     * 启动服务实例后，主线程以1秒间隔监视各个服务实例状态
     * 当所有服务实例的状态都处于finished状态时，退出主循环即程序结束
     * </pre>
     */
    private void mainLoop() {
        logger.info("EBR MAIN LOOP START!");
        while (!terminated) {
            boolean shouldStop = true;
            try {
                var entries = servPool.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<ServiceId, BaseService> entry = entries.next();
                    BaseService service = entry.getValue();
                    if (Service.ServiceStatus.FINISHED != service.status() && shouldStop) {
                        shouldStop = false;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LogUtils.dumpError(e);
            } finally {
                terminated = shouldStop;
            }
        }
        logger.info("EBR MAIN LOOP FINISHED!");
    }

    /**
     * <pre>
     * 分发事件总线中传递的所有事件信息
     * </pre>
     *
     * @param event 传递的事件
     */
    @Subscribe
    public void dispatchEvent(Event event) {
        if(LogUtils.isEventLogEnabled()) {
            logger.info(event.toString());
        }

        if (EVT_ACT_SERVICE_INIT.equals(event.act)) {
            servPool.forEach((id, service) -> service.init());
            eventBus.post(new Event(EVT_ACT_LOAD_DEF_FILE, APP, APP));
            return;
        } else if (EVT_ACT_SERVICE_SHUTDOWN.equals(event.act)
            || EVT_ACT_ALL_TASK_FINISHED.equals(event.act)) {
            servPool.forEach((id, service) -> service.finish());
            return;
        }

        if (APP == event.dst) {
            servPool.forEach((id, service) -> service.receive(event));
        }

        else {
            BaseService service = servPool.get(event.dst);
            if (service != null) {
                service.receive(event);
            }
        }
    }
}
