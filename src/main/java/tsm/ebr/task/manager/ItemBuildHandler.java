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

import tsm.ebr.base.Const;
import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.Type;
import tsm.ebr.task.manager.Item.Flow;
import tsm.ebr.task.manager.Item.Unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static tsm.ebr.base.Event.Symbols.EVT_DATA_META_MAP;
import static tsm.ebr.base.Task.Symbols.KEY_ROOT_UNIT;
import static tsm.ebr.base.Task.Symbols.KEY_UNIT_URL;

/**
 * 构建模块内数据结构
 * @author catforward
 */
public class ItemBuildHandler implements IHandler {

    /**
     * 构建Unit树
     * 构建TaskFlow对象
     * 成功时发送启动TaskFlow事件
     * @param context
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        Unit rootUnit = createUnitTree(context);
        addTaskUnit(rootUnit);
        addTaskFlow(rootUnit);
        context.setNextAction(Symbols.EVT_ACT_LAUNCH_TASK_FLOW);
        context.addHandlerResult(Symbols.EVT_DATA_TASK_FLOW_URL,
                StateHolder.getRootFlow().rootUnit.url);
        return true;
    }

    /**
     *
     * @param context
     * @return root unit
     */
    private Unit createUnitTree(HandlerContext context) {
        Map<String, Meta> urlMetaMap = (Map<String, Meta>) context.getParam(EVT_DATA_META_MAP);
        Meta rootMeta = urlMetaMap.get(KEY_ROOT_UNIT);
        HashMap<String, Unit> urlUnitMap = new HashMap<>(Const.INIT_CAP);
        createUnit(null, rootMeta, urlUnitMap);
        Unit rootUnit = urlUnitMap.get(KEY_ROOT_UNIT);
        updatePredecessors(rootMeta, urlUnitMap);
        return rootUnit;
    }

    /**
     *
     */
    private void createUnit(Unit uParent, Meta meta, Map<String, Unit> urlUnitMap) {
        String url = meta.symbols.get(KEY_UNIT_URL);
        Unit currentUnit = Optional.ofNullable(urlUnitMap.get(url)).orElseGet(() -> {
            Unit newUnit = new Unit(meta, uParent);
            urlUnitMap.put(newUnit.url, newUnit);
            if (uParent == null) {
                urlUnitMap.put(KEY_ROOT_UNIT, newUnit);
            } else {
                uParent.children.add(newUnit);
            }
            return newUnit;
        });
        for (Meta child : meta.children) {
            createUnit(currentUnit, child, urlUnitMap);
        }
    }

    /**
     *
     */
    private void updatePredecessors(Meta meta, Map<String, Unit> urlUnitMap) {
        Unit currentUnit = urlUnitMap.get(meta.symbols.get(KEY_UNIT_URL));
        for (String pUrl : meta.predecessorUrl) {
            Unit pUnit = urlUnitMap.get(pUrl);
            currentUnit.predecessors.add(pUnit);
        }
        for (Meta child : meta.children) {
            updatePredecessors(child, urlUnitMap);
        }
    }

    /**
     *
     */
    private void addTaskUnit(Unit unit) {
        StateHolder.addUnit(unit);
        for (Unit child : unit.children) {
            StateHolder.addUnit(child);
            if (!child.children.isEmpty()) {
                addTaskUnit(child);
            }
        }
    }

    /**
     *
     */
    private void addTaskFlow(Unit unit) {
        Flow flow = Flow.makeFrom(unit);
        flow.standby();
        StateHolder.addFlow(flow);
        for (Unit child : unit.children) {
            if (Type.MODULE == child.type) {
                addTaskFlow(child);
            }
        }
    }
}
