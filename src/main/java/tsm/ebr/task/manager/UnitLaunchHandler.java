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

import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.PerformableTask;
import tsm.ebr.task.manager.Item.Flow;
import tsm.ebr.task.manager.Item.Unit;

import java.util.ArrayList;
import java.util.Set;

import static tsm.ebr.base.Task.Type.MODULE;

public class UnitLaunchHandler implements IHandler {

    @Override
    public boolean doHandle(HandlerContext context) {
        String act = context.getCurrentAction();
        switch (act) {
            case Symbols.EVT_ACT_TASK_UNIT_STATE_CHANGED: {
                launchUnit(context);
                break;
            }
            default: {
                throw new RuntimeException(String.format("虽然不太可能，但是，送错地方了老兄...[%s]:", act));
            }
        }
        return true;
    }

    private void launchUnit(HandlerContext context) {
        String url = (String) context.getParam(Symbols.EVT_DATA_TASK_UNIT_URL);
        ArrayList<PerformableTask> units = searchPerformableUnit(url);
        context.setNextAction(Symbols.EVT_ACT_LAUNCH_TASK_UNITS);
        context.addHandlerResult(Symbols.EVT_DATA_TASK_PERFORMABLE_UNITS_LIST, units);
    }

    /**
     * 查找顺序
     * 1.查找出此url的直接后集结点
     * 2.针对1的结果，检查每个后继节点的前提节点是否完成， 都完成了就加入待启动列表
     * 3.如果1没有找到任何直接的后继节点，则检查此url所在父节点是否为非TASK节点
     * 4.针对3的结果，如果父节点完成了则检索出父节点的直接后继节点，重复1的处理
     */
    private ArrayList<PerformableTask> searchPerformableUnit(String url) {
        ArrayList<PerformableTask> pList = new ArrayList<>();
        Unit changedUnit = StateHolder.getUnit(url);
        Flow flow = StateHolder.getFlow(changedUnit.parent.url);
        Set<Unit> sucSet = flow.getSuccessorsOf(changedUnit);

        if (!sucSet.isEmpty()) {
            collectPerformableUnit(flow, sucSet, pList);
        } else {
            if (MODULE == changedUnit.parent.type && changedUnit.parent.isComplete()) {
                flow = StateHolder.getFlow(changedUnit.parent.parent.url);
                sucSet = flow.getSuccessorsOf(changedUnit.parent);
                collectPerformableUnit(flow, sucSet, pList);
            }
        }
        return pList;
    }

    private void collectPerformableUnit(Flow flow, Set<Unit> units, ArrayList<PerformableTask> pList) {
        for (Unit suc : units) {
            long uCount = flow.getPredecessorsOf(suc).stream().filter(u -> !u.isComplete()).count();
            if (uCount == 0) {
                // 连同MODULE一同发送
                // 接受方判断是MODULE会发送LAUNCH_FLOW事件
                pList.add(new PerformableTask(suc.url, suc.command));
            }
        }
    }
}
