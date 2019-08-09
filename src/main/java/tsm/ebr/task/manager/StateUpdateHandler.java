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
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Task.State;

import java.util.logging.Logger;

/**
 * 任务状态更新
 * @author catforward
 */
public class StateUpdateHandler implements IHandler {
    private final static Logger logger = Logger.getLogger(StateUpdateHandler.class.getCanonicalName());

    /**
     *
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        String url = (String) context.getParam(Symbols.MSG_DATA_TASK_UNIT_URL);
        State state = (State) context.getParam(Symbols.MSG_DATA_TASK_UNIT_NEW_STATE);
        StateHolder.getUnit(url).updateState(state);
        // 如果任意一个unit执行错误
        // 或者顶层flow都完成了
        // 则通知应用程序退出
        if (State.ERROR == state) {
            logger.warning(String.format("[%s]: error end...", url));
            context.setNoticeAction(Symbols.MSG_ACT_SERVICE_SHUTDOWN);
            return false;
        } else if (StateHolder.getRootFlow().isComplete()) {
            context.setNextAction(Symbols.MSG_ACT_ALL_TASK_FINISHED);
            return false;
        }
        return true;
    }
}
