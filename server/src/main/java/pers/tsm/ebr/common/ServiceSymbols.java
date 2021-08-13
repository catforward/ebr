/*
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
package pers.tsm.ebr.common;

import static pers.tsm.ebr.common.AppConsts.BASE_URL;

/**
 * <pre>service's const</pre>
 *
 * @author l.gong
 */
public final class ServiceSymbols {

    private ServiceSymbols() {}

    /* API */
    public static final String URL_INFO_FLOW_LIST = BASE_URL + "/api/info/flows";
    public static final String SERVICE_INFO_FLOW_LIST = "service.info.flows";

    public static final String URL_INFO_FLOW_DETAIL = BASE_URL + "/api/info/flow";
    public static final String SERVICE_INFO_FLOW_DETAIL = "service.info.flow";

    public static final String URL_SCHD_ACTION = BASE_URL + "/api/schd/action";
    public static final String SERVICE_SCHD_ACTION = "service.schd.action";

    /* MSG */
    public static final String MSG_ACTION_REFRESH_FS_DEFINE = "msg.action.refresh.fs.define";
    public static final String MSG_ACTION_TASK_START = "msg.action.task.start";
    public static final String MSG_ACTION_TASK_STOP = "msg.action.task.stop";
    public static final String MSG_ACTION_TASK_PAUSE = "msg.action.task.pause";
    public static final String MSG_ACTION_TASK_SKIP = "msg.action.task.skip";

    public static final String MSG_STATE_TASK_COMPLETE = "msg.state.task.complete";
    public static final String MSG_STATE_TASK_RUNNING = "msg.state.task.running";
    public static final String MSG_STATE_TASK_FAILED = "msg.state.task.failed";

}
