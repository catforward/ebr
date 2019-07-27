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
import tsm.ebr.task.manager.Item.Flow;
import tsm.ebr.task.manager.Item.Unit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class StateHolder {
    private final static StateHolder _INSTANCE = new StateHolder();
    /* KEY-> url of flow VALUE-> ref of flow */
    private final Map<String, Flow> urlFlowMap;
    /* KEY-> url of unit VALUE-> ref of unit */
    private final Map<String, Unit> urlUnitMap;
    private Flow rootFlow;

    private StateHolder() {
        urlFlowMap = new ConcurrentHashMap<>();
        urlUnitMap = new ConcurrentHashMap<>();
        rootFlow = null;
    }


    static StateHolder getInstance() {
        return _INSTANCE;
    }

    static StateHolder addFlow(Flow newTaskFlow) {
        if (!_INSTANCE.urlFlowMap.containsKey(newTaskFlow.rootUnit.url)) {
            _INSTANCE.urlFlowMap.put(newTaskFlow.rootUnit.url, newTaskFlow);
        }
        if (_INSTANCE.rootFlow == null && Type.ROOT == newTaskFlow.rootUnit.type) {
            _INSTANCE.rootFlow = newTaskFlow;
        }
        return _INSTANCE;
    }

    static StateHolder addUnit(Unit newUnit) {
        if (!_INSTANCE.urlUnitMap.containsKey(newUnit.url)) {
            _INSTANCE.urlUnitMap.put(newUnit.url, newUnit);
        }
        return _INSTANCE;
    }

    static Flow getRootFlow() {
        return _INSTANCE.rootFlow;
    }

    static Flow getFlow(String url) {
        return _INSTANCE.urlFlowMap.get(url);
    }

    static Unit getUnit(String url) {
        return _INSTANCE.urlUnitMap.get(url);
    }

    static void init() {
        // reset first
        clear();
    }

    static void clear() {
        _INSTANCE.rootFlow = null;
        _INSTANCE.urlFlowMap.clear();
        _INSTANCE.urlUnitMap.clear();
    }
}
