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

import ebr.core.base.Broker;
import ebr.core.base.Message;

/**
 * <pre>
 * 任务运行时状态管理模块
 * </pre>
 * @author catforward
 */
public class JobStateManagementBroker extends Broker.BaseBroker {

    public JobStateManagementBroker() {
        super();
    }

    @Override
    public Broker.Id id() {
        return Broker.Id.MANAGEMENT;
    }

    @Override
    protected void onInit() {
        // 由客户端触发
        registerActionHandler(Message.Symbols.MSG_ACT_LAUNCH_JOB_FLOW,
                PerformableJobItemCollectHandler.class);
        // 由executor触发
        registerActionHandler(Message.Symbols.MSG_ACT_JOB_STATE_CHANGED,
                JobStateUpdateHandler.class,
                PerformableJobItemCollectHandler.class);
    }

    @Override
    protected void onFinish() {
        unregister(Message.Symbols.MSG_ACT_LAUNCH_JOB_FLOW);
        unregister(Message.Symbols.MSG_ACT_JOB_STATE_CHANGED);
    }
}

