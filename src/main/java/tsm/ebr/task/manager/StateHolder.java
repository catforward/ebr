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
package tsm.ebr.task.manager;

import tsm.ebr.base.Task.Type;
import tsm.ebr.base.Task.Unit;
import tsm.ebr.base.Task.Flow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 模块内运行时统一管理任务执行状态
 * @author catforward
 */
class StateHolder {
    private final static Logger logger = Logger.getLogger(StateHolder.class.getCanonicalName());
    private final static StateHolder INSTANCE = new StateHolder();
    /**  */
    private final Map<String, Flow> urlFlowMap;
    /**  */
    private final Map<String, Unit> urlUnitMap;
    /**  */
    private Flow rootFlow;

    private StateHolder() {
        urlFlowMap = new ConcurrentHashMap<>();
        urlUnitMap = new ConcurrentHashMap<>();
        rootFlow = null;
    }

    static StateHolder getInstance() {
        return INSTANCE;
    }

    /**
     *
     */
    static StateHolder addFlow(Flow newTaskFlow) {
        if (!INSTANCE.urlFlowMap.containsKey(newTaskFlow.rootUnit.url)) {
            INSTANCE.urlFlowMap.put(newTaskFlow.rootUnit.url, newTaskFlow);
        }
        if (INSTANCE.rootFlow == null && Type.ROOT == newTaskFlow.rootUnit.type) {
            INSTANCE.rootFlow = newTaskFlow;
        }
        logger.info(newTaskFlow.toString());
        return INSTANCE;
    }

    /**
     *
     */
    static StateHolder addUnit(Unit newUnit) {
        if (!INSTANCE.urlUnitMap.containsKey(newUnit.url)) {
            INSTANCE.urlUnitMap.put(newUnit.url, newUnit);
        }
        return INSTANCE;
    }

    /**
     *
     */
    static Flow getRootFlow() {
        return INSTANCE.rootFlow;
    }

    /**
     *
     */
    static Flow getFlow(String url) {
        return INSTANCE.urlFlowMap.get(url);
    }

    /**
     *
     */
    static Unit getUnit(String url) {
        return INSTANCE.urlUnitMap.get(url);
    }

    /**
     *
     */
    static void init() {
        // reset first
        clear();
    }

    /**
     *
     */
    static void clear() {
        INSTANCE.rootFlow = null;
        INSTANCE.urlFlowMap.clear();
        INSTANCE.urlUnitMap.clear();
    }
}
