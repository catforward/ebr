/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.cli.core.jobs;

import pers.ebr.cli.core.base.Handler;
import pers.ebr.cli.core.base.Message;
import pers.ebr.cli.core.data.JobState;
import pers.ebr.cli.core.util.AppLogger;

/**
 * <pre>
 * 当发生Message.Symbols.MSG_ACT_JOB_STATE_CHANGED事件时
 * 更新指定的任务状态
 * </pre>
 * @author l.gong
 */
public class JobStateUpdateHandler implements Handler {

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
