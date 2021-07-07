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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.base.BaseService;
import pers.tsm.ebr.base.IResult;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.types.ResultEnum;

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
    private final Map<String, String> actionMapping = new HashMap<>();

    @Override
    public void start() throws Exception {
        super.start();
        actionMapping.put(Symbols.START, ServiceSymbols.MSG_ACTION_TASK_START);
        actionMapping.put(Symbols.STOP, ServiceSymbols.MSG_ACTION_TASK_STOP);
        actionMapping.put(Symbols.PAUSE, ServiceSymbols.MSG_ACTION_TASK_PAUSE);
        actionMapping.put(Symbols.SKIP, ServiceSymbols.MSG_ACTION_TASK_SKIP);
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
            handleAction(action, target)
            .onSuccess(ar -> promise.complete(ResultEnum.SUCCESS))
            .onFailure(promise::fail);
        });
    }

    private Future<Void> handleAction(String action, JsonObject target) {
        String actionId = actionMapping.get(action);
        if (isNull(actionId) || actionId.isBlank()) {
            throw new AppException(ResultEnum.ERR_11007);
        }
        return Future.future(promise -> {
            vertx.eventBus().request(actionId, target, (AsyncResult<Message<JsonObject>> res) -> {
                if (res.failed()) {
                    promise.fail(res.cause());
                } else {
                    //JsonObject retData = new JsonObject().mergeIn(res.result().body());
                    promise.complete();
                }
            });
        });
    }

}
