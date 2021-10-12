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
package pers.ebr.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.*;
import pers.ebr.data.TaskDefineFileProp;
import pers.ebr.data.TaskRepo;
import pers.ebr.types.ResultEnum;

import java.util.Map;

/**
 * <pre>
 * response:
 * {
 * flows: [
 *     {
 *         "url": string,
 *         "state": string,
 *         "lastModifiedTime": string,
 *         "size": number
 *     }, {}, ...
 * ]
 * }
 * </pre>
 *
 * @author l.gong
 */
public class FlowListService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(FlowListService.class);

    @Override
    public void start() throws Exception {
        super.start();
        registerService(ServiceSymbols.SERVICE_INFO_FLOW_LIST);
    }

    @Override
    protected String getServiceName() {
        return FlowListService.class.getName();
    }

    @Override
    protected Future<IResult> doService() {
        logger.trace("doService -> {}", inData);
        return Future.future(promise -> {
            JsonArray flows = new JsonArray();
            outData.put(AppSymbols.FLOWS, flows);
            Map<String, TaskDefineFileProp> info =  TaskRepo.getAllFlowInfo();
            info.forEach((url, prop) -> {
                JsonObject flow = new JsonObject();
                flow.put(AppSymbols.URL, prop.getFlowUrl());
                flow.put(AppSymbols.STATE, prop.getState());
                flow.put(AppSymbols.LAST_MODIFIED_TIME,
                        StringUtils.toDatetimeStr(prop.getLastModifiedTime(),
                        AppConfigs.getZoneId()));
                flow.put(AppSymbols.SIZE, prop.getFileSize());
                flows.add(flow);
            });
            promise.complete(ResultEnum.SUCCESS);
        });
    }

}
