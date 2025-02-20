/*
  Copyright 2021 liang gong

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.ebr.base;

/**
 * <pre>Service's const</pre>
 *
 * @author l.gong
 */
public final class ServiceSymbols {

    private ServiceSymbols() {}

    /* API & Service */
    /* naming rule: func_type.module.func_name */
    public static final String API_INFO_FLOW_LIST = "api.info.flow_list";
    public static final String SERVICE_INFO_FLOW_LIST = "service.info.flow_list";

    public static final String API_INFO_FLOW_DETAIL = "api.info.flow_detail";
    public static final String SERVICE_INFO_FLOW_DETAIL = "service.info.flow_detail";

    public static final String API_SCHD_ACTION = "api.schd.action";
    public static final String SERVICE_SCHD_ACTION = "service.schd.action";

    /* MSG */
    public static final String MSG_ACTION_REFRESH_FS_DEFINE = "msg.action.refresh.fs.define";
    public static final String MSG_ACTION_CRON_CHECK = "msg.action.cron.check";
    public static final String MSG_ACTION_FLOW_START = "msg.action.flow.start";
    public static final String MSG_ACTION_FLOW_ABORTED = "msg.action.flow.aborted";

    public static final String MSG_STATE_FLOW_LAUNCH = "msg.state.flow.launch";
    public static final String MSG_STATE_FLOW_FINISH = "msg.state.flow.finish";

    public static final String MSG_STATE_TASK_LAUNCH = "msg.state.task.launch";
    public static final String MSG_STATE_TASK_ABORTED = "msg.state.task.aborted";
    public static final String MSG_STATE_TASK_PAUSED = "msg.state.task.paused";
    public static final String MSG_STATE_TASK_SKIPPED = "msg.state.task.skipped";
    public static final String MSG_STATE_TASK_COMPLETE = "msg.state.task.complete";
    public static final String MSG_STATE_TASK_RUNNING = "msg.state.task.running";
    public static final String MSG_STATE_TASK_FAILED = "msg.state.task.failed";

}
