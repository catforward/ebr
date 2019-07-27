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
import tsm.ebr.base.Service.ServiceId;
import tsm.ebr.utils.LogUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static tsm.ebr.base.Event.Symbols.*;
import static tsm.ebr.base.Service.ServiceId.APP;

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
public class Application {

    private final Logger logger = Logger.getLogger(Application.class.getCanonicalName());
    /** 主线程循环停止标志 */
    private boolean terminated;
    /** 提供具体功能的处理类实例 */
    private final Map<ServiceId, Service> servPool;
    /** 事件执行线程池 （本应用同一时刻只有1个线程来执行具体处理，不考虑耗时操作的情况，因为没有打算写耗时处理） */
    private final ExecutorService singleEventDispatcher;
    /** 事件总线 */
    private final EventBus eventBus;

    private final static Application _INSTANCE = new Application();

    private Application() {
        terminated = false;
        servPool = new LinkedHashMap<>();
        singleEventDispatcher = Executors.newSingleThreadExecutor();
        eventBus = new AsyncEventBus("EBR_EVENT_BUS", singleEventDispatcher);
    }

    /**
     * <pre>
     * 应用程序初始化
     * 子类onInit函数中添加所需具体功能的处理类实例
     * </pre>
     */
    public static Application init() {
        _INSTANCE.eventBus.register(_INSTANCE);
        return _INSTANCE;
    }

    private void finish() {
        eventBus.unregister(_INSTANCE);
        singleEventDispatcher.shutdown();
    }

    /**
     * <pre>
     * 取得事件总线实例
     * </pre>
     *
     * @return 事件总线实例
     */
    static EventBus getEventBus() {
        return _INSTANCE.eventBus;
    }

    /**
     * <pre>
     * 添加提供具体功能的处理类实例
     * </pre>
     *
     * @param id    处理类标识ID
     * @param service  处理类实例
     */
    public void deployService(ServiceId id, Service service) {
        if (id == null || service == null) {
            throw new IllegalArgumentException("argument can not be null...");
        }
        if (!servPool.containsKey(id)) {
            servPool.put(id, service);
        }
    }

    public void run() {
        // 通知所有service进行初始化
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
            boolean shouldTerminated = true;
            try {
                var entries = servPool.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<ServiceId, Service> entry = entries.next();
                    Service service = entry.getValue();
                    if (Service.ServiceStatus.FINISHED != service.status() && shouldTerminated) {
                        shouldTerminated = false;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LogUtils.dumpError(e);
            } finally {
                terminated = shouldTerminated;
            }
        }
        logger.info("EBR MAIN LOOP FINISHED!");
    }

    /**
     * <pre>
     * 向日志文件中记录事件总线中传递的所有事件信息
     * 当ebr.properties的ebr.output.event.log为false时，
     * 关闭记录功能
     * </pre>
     *
     * @param event 传递的事件
     */
    @Subscribe
    public final void dispatchEvent(Event event) {
        if(LogUtils.isEventLogEnabled()) {
            logger.info(event.toString());
        }
        // 检查是否是服务开始or结束事件
        if (EVT_ACT_SERVICE_INIT.equals(event.act)) {
            // 所有Service初始化
            servPool.forEach((id, service) -> service.init());
            // 通知读取定义文件
            eventBus.post(new Event(EVT_ACT_LOAD_DEF_FILE, APP, APP));
            return;
        } else if (EVT_ACT_SERVICE_SHUTDOWN.equals(event.act)
            || EVT_ACT_ALL_TASK_FINISHED.equals(event.act)) {
            // 所有Service停止
            servPool.forEach((id, service) -> service.finish());
            return;
        }
        // 群发
        if (APP == event.dst) {
            servPool.forEach((id, service) -> service.receive(event));
        }
        // 派送
        else {
            Service service = servPool.get(event.dst);
            if (service != null) {
                service.receive(event);
            }
        }
    }
}
