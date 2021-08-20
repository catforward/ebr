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
package pers.tsm.ebr.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.tsm.ebr.base.BaseService;
import pers.tsm.ebr.base.IResult;
import pers.tsm.ebr.common.AppConsts;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.data.Flow;
import pers.tsm.ebr.data.Task;
import pers.tsm.ebr.data.TaskRepo;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskAttrEnum;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static pers.tsm.ebr.common.AppConsts.*;


/**
 * <pre>
 * request:
 * {
 *     "flow":string
 * } * 
 * response: {
 *     "flow": {
 *         "url": string,
 *         "content" : [
 *          {
 *              url: string,
 *              type: string,
 *              state: string,
 *              script: string,
 *              depends: [
 *                  string, ...
 *              ]
 *          }, ...
 *         ]
 *     }
 * }
 * 
 * </pre>
 *
 * @author l.gong
 */
public class FlowDetailService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(FlowDetailService.class);

    @Override
    public void start() throws Exception {
        super.start();
        registerService(ServiceSymbols.SERVICE_INFO_FLOW_DETAIL);
    }

    @Override
    protected String getServiceName() {
        return FlowDetailService.class.getName();
    }

    @Override
    public Future<IResult> doPrepare() {
        logger.trace("doPrepare -> {}", inData);
        return Future.future(promise -> {
            JsonObject postBody = getPostBody();
            String taskUrl = postBody.getString(AppConsts.FLOW);
            if (isNull(taskUrl) || taskUrl.isBlank()) {
                logger.debug("flow's url is empty.");
                promise.fail(new AppException(ResultEnum.ERR_11001));
            } else {
                promise.complete(ResultEnum.SUCCESS);
            }
        });
    }

    @Override
    protected Future<IResult> doService() {
        logger.trace("doService -> {}", inData);
        return Future.future(promise -> {
            JsonObject postBody = getPostBody();
            String flowUrl = postBody.getString(AppConsts.FLOW);
            JsonObject flowData = new JsonObject();
            outData.put(AppConsts.FLOW, flowData);
            try {
                Flow flow = TaskRepo.getFlow(flowUrl);
                if (isNull(flow)) {
                    throw new AppException(ResultEnum.ERR_11003);
                }
                flowData.put(AppConsts.URL, flow.getUrl());
                flowData.put(AppConsts.CONTENT, toContentArray(flow.getRootTask()));
            } catch (Exception ex) {
                promise.fail(ex);
                return;
            }
            promise.complete(ResultEnum.SUCCESS);
        });
    }

    private JsonArray toContentArray(Task root) {
        if (isNull(root)) {
            return EMPTY_JSON_ARR;
        }

        List<JsonObject> list = new ArrayList<>();
        root.getChildren().forEach(child -> makeDescObject(child, list));
        if (list.isEmpty()) {
            return EMPTY_JSON_ARR;
        }

        JsonArray desc = new JsonArray();
        list.forEach(desc::add);
        return desc;
    }

    private void makeDescObject(Task task, List<JsonObject> list) {
        JsonObject obj = new JsonObject();
        obj.put(URL,task.getUrl());
        obj.put(TYPE, task.getType().getName());
        obj.put(STATE, task.getState().getName());
        obj.put(TaskAttrEnum.SCRIPT.getName(), task.getScript());
        JsonArray depends = new JsonArray();
        obj.put(TaskAttrEnum.DEPENDS.getName(), depends);
        task.getPredecessor().forEach(pred -> depends.add(pred.getUrl()));
        list.add(obj);

        task.getChildren().forEach(child -> makeDescObject(child, list));
    }

}
