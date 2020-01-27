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

import pers.ebr.cli.core.Broker;
import pers.ebr.cli.core.Message;

/**
 * <pre>
 * 任务运行时状态管理模块
 * </pre>
 * @author l.gong
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
        JobItemStateHolder.clear();
    }
}
