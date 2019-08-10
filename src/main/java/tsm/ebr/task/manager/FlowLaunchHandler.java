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

import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Task.Flow;
import tsm.ebr.base.Task.PerformableTask;
import tsm.ebr.base.Task.Type;
import tsm.ebr.base.Task.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 接受并处理以下事件
 * - 启动taskflow事件
 *  -- 找到此taskflow中的顶层task并通知启动服务
 * - 启动若干个taskflow事件
 *  -- 循环taskflow的url列表找出所有的顶层task并通知启动服务
 * - task单元的状态改变
 *  -- 根据状态变更的task的url找出接下来需要被启动的task并通知启动服务
 * @author catforward
 */
public class FlowLaunchHandler implements IHandler {

    @Override
    public boolean doHandle(HandlerContext context) {
        String act = context.getCurrentAction();
        switch (act) {
            case Symbols.MSG_ACT_LAUNCH_TASK_FLOW: {
                launchFlow(context);
                break;
            }
            case Symbols.MSG_ACT_LAUNCH_TASK_FLOWS: {
                launchFlowList(context);
                break;
            }
            case Symbols.MSG_ACT_TASK_UNIT_STATE_CHANGED: {
                searchPerformableFlow(context);
                break;
            }
            default: {
                throw new RuntimeException(String.format("虽然不太可能，但是，送错地方了老兄...[%s]:", act));
            }
        }
        return true;
    }

    /**
     * 找到指定的taskflow的顶层task单元
     * 通过发送事件通知启动服务
     *
     * @param context
     */
    private void launchFlow(HandlerContext context) {
        String url = (String) context.getParam(Symbols.MSG_DATA_TASK_FLOW_URL);
        Flow flow = StateHolder.getFlow(url);
        Set<Unit> units = flow.getTopLevelUnits();
        ArrayList<PerformableTask> pList = new ArrayList<>(units.size());
        for (Unit unit : units) {
            if (flow.getPredecessorsOf(unit).stream().filter(u -> !u.isComplete()).count() == 0) {
                pList.add(new PerformableTask(unit.url, unit.command));
            }
        }
        flow.start();
        context.setNextAction(Symbols.MSG_ACT_LAUNCH_TASK_UNITS);
        context.addHandlerResult(Symbols.MSG_DATA_TASK_PERFORMABLE_UNITS_LIST, pList);
    }

    /**
     * 找到指定的若干个taskflow的所有顶层task单元
     * 通过发送事件通知启动服务
     *
     * @param context
     */
    private void launchFlowList(HandlerContext context) {
        List<String> pUrlList = (List<String>) context.getParam(Symbols.MSG_DATA_TASK_FLOW_URL_LIST);
        ArrayList<PerformableTask> pList = new ArrayList<>();
        for (String url : pUrlList) {
            Flow flow = StateHolder.getFlow(url);
            Set<Unit> units = flow.getTopLevelUnits();
            for (Unit unit : units) {
                if (flow.getPredecessorsOf(unit).stream().filter(u -> !u.isComplete()).count() == 0) {
                    pList.add(new PerformableTask(unit.url, unit.command));
                }
            }
            flow.start();
        }
        context.setNextAction(Symbols.MSG_ACT_LAUNCH_TASK_UNITS);
        context.addHandlerResult(Symbols.MSG_DATA_TASK_PERFORMABLE_UNITS_LIST, pList);
    }

    /**
     * 查找顺序
     * 1.查找出此url的直接后集结点
     * 2.针对1的结果，检查每个后继节点的前提节点是否完成， 都完成了就加入待启动列表
     * 3.如果1没有找到任何直接的后继节点，则检查此url所在父节点是否为非TASK节点
     * 4.针对3的结果，如果父节点完成了则检索出父节点的直接后继节点，重复1的处理
     * @param context
     */
    private void searchPerformableFlow(HandlerContext context) {
        String url = (String) context.getParam(Symbols.MSG_DATA_TASK_UNIT_URL);
        ArrayList<String> pUrlList = new ArrayList<>();
        Unit changedUnit = StateHolder.getUnit(url);
        Flow flow = StateHolder.getFlow(changedUnit.parent.url);
        Set<Unit> sucSet = flow.getSuccessorsOf(changedUnit);

        if (!sucSet.isEmpty()) {
            collectPerformableFlow(flow, sucSet, pUrlList);
        } else {
            if (Type.MODULE == changedUnit.parent.type && changedUnit.parent.isComplete()) {
                flow = StateHolder.getFlow(changedUnit.parent.parent.url);
                sucSet = flow.getSuccessorsOf(changedUnit.parent);
                collectPerformableFlow(flow, sucSet, pUrlList);
            }
        }
        context.setNextAction(Symbols.MSG_ACT_LAUNCH_TASK_FLOWS);
        context.addHandlerResult(Symbols.MSG_DATA_TASK_FLOW_URL_LIST, pUrlList);
    }

    /**
     * 循环针对每一个单元查找符合以下条件的才可以被作为待启动的taskflow
     * 1. 处理对象的类型必须是ROOT或MODULE（类型不等于TYPE）
     * 2. 处理对象的前驱节点必须全部正常结束
     * @param flow
     * @param units
     * @param pList
     */
    private void collectPerformableFlow(Flow flow, Set<Unit> units, ArrayList<String> pList) {
        for (Unit suc : units) {
            if (Type.TASK == suc.type) {
                continue;
            }
            long uCount = flow.getPredecessorsOf(suc).stream().filter(u -> !u.isComplete()).count();
            if (uCount == 0) {
                pList.add(suc.url);
            }
        }
    }
}
