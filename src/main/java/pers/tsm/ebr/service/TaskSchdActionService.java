/**
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
package pers.tsm.ebr.service;

import static java.util.Objects.isNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.base.BaseService;
import pers.tsm.ebr.base.IResult;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.data.Flow;
import pers.tsm.ebr.data.Task;
import pers.tsm.ebr.data.TaskRepo;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskStateEnum;

/**
 *<pre>
 * request:
 * {
 *  "action": "xxx",
 *  "target": {
 *      "flow": "xxx",
 *      "task":"xxx"(optional)
 *  }
 * } * 
 * response: { }
 * 
 * </pre>
 *
 * @author l.gong
 */
public class TaskSchdActionService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(TaskSchdActionService.class);

    @Override
    public void start() throws Exception {
        super.start();
        registerService(ServiceSymbols.SERVICE_SCHD_ACTION);
    }

    @Override
    protected String getServiceName() {
        return TaskSchdActionService.class.getName();
    }

    @Override
    public Future<IResult> doPrepare() {
        logger.trace("doPrepare -> {}", inData);
        return Future.future(promise -> {
            JsonObject postBody = getPostBody();
            String action = postBody.getString(Symbols.ACTION);
            if (isNull(action) || action.isBlank()) {
                logger.debug("parameter[action] is empty.");
                promise.fail(new AppException(ResultEnum.ERR_11001));
                return;
            }
            JsonObject target = postBody.getJsonObject(Symbols.TARGET);
            if (isNull(target) || target.isEmpty()) {
                logger.debug("parameter[target] is empty.");
                promise.fail(new AppException(ResultEnum.ERR_11001));
                return;
            }
            String flowUrl = target.getString(Symbols.FLOW);
            if (isNull(flowUrl) || flowUrl.isBlank()) {
                logger.debug("parameter[flowUrl] is empty.");
                promise.fail(new AppException(ResultEnum.ERR_11001));
                return;
            }
            promise.complete(ResultEnum.SUCCESS);
        });
    }

    @Override
    protected Future<IResult> doService() {
        logger.trace("doService -> {}", inData);
        return Future.future(promise -> {
            JsonObject postBody = getPostBody();
            String action = postBody.getString(Symbols.ACTION);
            JsonObject target = postBody.getJsonObject(Symbols.TARGET);
            getActionHandler(action, target)
            .onSuccess(ar -> promise.complete(ResultEnum.SUCCESS))
            .onFailure(promise::fail);
        });
    }

    private Future<Void> getActionHandler(String action, JsonObject target) {
        switch (action) {
        case Symbols.START: return handlePerformAction(target);
        case Symbols.STOP:
        case Symbols.PAUSE:
        case Symbols.SKIP:
        default: throw new AppException(ResultEnum.ERR_11007);
        }
    }

    private Future<Void> handlePerformAction(JsonObject target) {
        return Future.future(promise -> {
            String flowUrl = target.getString(Symbols.FLOW);
            String taskUrl = target.getString(Symbols.TASK);
            if (isNull(taskUrl) || taskUrl.isBlank()) {
                doPerformFlow(TaskRepo.getFlow(flowUrl), target);
            } else {
                doPerformTask(TaskRepo.getTaskFrom(flowUrl, taskUrl), target);
            }
            promise.complete();
        });
    }

    private void doPerformFlow(Flow flow, JsonObject target) {
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }
        System.out.println(flow.toString());
        TaskStateEnum state = flow.getState();
        if (TaskStateEnum.RUNNING == state || TaskStateEnum.SKIPPED == state) {
            throw new AppException(ResultEnum.ERR_11005);
        }
        TaskRepo.pushRunningPool(flow);
        emitMsg(ServiceSymbols.MSG_ACTION_TASK_PERFORM, target);
    }

    private void doPerformTask(Task task, JsonObject target) {
        if (isNull(task)) {
            throw new AppException(ResultEnum.ERR_11004);
        }
        TaskStateEnum state = task.getState();
        if (TaskStateEnum.RUNNING == state || TaskStateEnum.SKIPPED == state) {
            throw new AppException(ResultEnum.ERR_11006);
        }
        emitMsg(ServiceSymbols.MSG_ACTION_TASK_PERFORM, target);
    }

}
