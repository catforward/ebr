/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package tsm.ebr.task.manager;

import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Task.Flow;
import tsm.ebr.base.Task.Type;
import tsm.ebr.base.Task.Unit;

import static tsm.ebr.base.Message.Symbols.MSG_DATA_TASK_ROOT_UNIT;

/**
 * <pre>
 * 构建模块内数据结构
 * </pre>
 * @author catforward
 */
public class ItemBuildHandler implements IHandler {

    /**
     * <pre>
     * 构建Unit树
     * 构建TaskFlow对象
     * 成功时发送启动TaskFlow事件
     * </pre>
     * @param context
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        Unit rootUnit = (Unit) context.getParam(MSG_DATA_TASK_ROOT_UNIT);
        addTaskUnit(rootUnit);
        addTaskFlow(rootUnit);
        context.setNextAction(Symbols.MSG_ACT_LAUNCH_TASK_FLOW);
        context.addHandlerResult(Symbols.MSG_DATA_TASK_FLOW_URL,
                StateHolder.getRootFlow().rootUnit.url);
        return true;
    }

    private void addTaskUnit(Unit unit) {
        StateHolder.addUnit(unit);
        for (Unit child : unit.children) {
            StateHolder.addUnit(child);
            if (!child.children.isEmpty()) {
                addTaskUnit(child);
            }
        }
    }

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
