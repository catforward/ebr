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
package ebr.core.jobs;

import ebr.core.base.Handler;
import ebr.core.base.Message;
import ebr.core.data.JobState;
import ebr.core.util.AppLogger;

/**
 * <pre>
 * 当发生Message.Symbols.MSG_ACT_JOB_STATE_CHANGED事件时
 * 更新指定的任务状态
 * </pre>
 * @author catforward
 */
public class JobStateUpdateHandler implements Handler.IHandler {

    public JobStateUpdateHandler() {
        super();
    }

    @Override
    public boolean doHandle(Handler.HandlerContext context) {
        String url = (String) context.getParam(Message.Symbols.MSG_DATA_JOB_URL);
        JobState state = (JobState) context.getParam(Message.Symbols.MSG_DATA_NEW_JOB_STATE);
        AppLogger.info(String.format("url:[%s] state->[%s]", url, state.name()));
        JobItemStateHolder.getJob(url).updateState(state);
        // 如果任意一个unit执行错误
        // 或者顶层flow都完成了
        // 则通知应用程序退出
        if (JobState.FAILED == state) {
            context.setNoticeAction(Message.Symbols.MSG_ACT_SERVICE_SHUTDOWN);
            return false;
        } else if (JobItemStateHolder.getRootJobFlow().isComplete()) {
            context.setNextAction(Message.Symbols.MSG_ACT_ALL_JOB_FINISHED);
            return false;
        }
        return true;
    }
}
