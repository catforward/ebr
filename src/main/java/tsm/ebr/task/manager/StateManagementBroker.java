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

import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Broker.BaseBroker;
import tsm.ebr.base.Broker.Id;

/**
 * 任务运行时状态管理模块
 * @author catforward
 */
public class StateManagementBroker extends BaseBroker {

    public StateManagementBroker() {
        super();
        StateHolder.init();
    }

    /**
     *
     */
    @Override
    public Id id() {
        return Id.MANAGEMENT;
    }

    /**
     *
     */
    @Override
    protected void onInit() {
        // 当Meta数据生成后，包装其成为Unit单元树
        registerActionHandler(Symbols.MSG_ACT_TASK_META_CREATED,
                ItemBuildHandler.class);
        // 当生成Flow图之后，启动该Flow
        registerActionHandler(Symbols.MSG_ACT_LAUNCH_TASK_FLOW,
                FlowLaunchHandler.class);
        registerActionHandler(Symbols.MSG_ACT_LAUNCH_TASK_FLOWS,
                FlowLaunchHandler.class);
        // 任务状态变更时，检查其前驱后置以及父节点状态
        registerActionHandler(Symbols.MSG_ACT_TASK_UNIT_STATE_CHANGED,
                StateUpdateHandler.class,
                UnitLaunchHandler.class);
    }

    /**
     *
     */
    @Override
    protected void onFinish() {
        unregister(Symbols.MSG_ACT_TASK_META_CREATED);
        unregister(Symbols.MSG_ACT_LAUNCH_TASK_FLOW);
        unregister(Symbols.MSG_ACT_LAUNCH_TASK_FLOWS);
        unregister(Symbols.MSG_ACT_TASK_UNIT_STATE_CHANGED);
    }
}
