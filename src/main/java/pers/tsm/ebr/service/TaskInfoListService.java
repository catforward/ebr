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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.base.BaseService;
import pers.tsm.ebr.base.IResult;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.common.StringUtils;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.data.TaskDefineFileProp;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.types.ResultEnum;


/**
 * <pre>
 * response:
 * {
 * flows: [
 *     {
 *         "url": "xxx"，
 *         "lastModifiedTime": "xxxx"
 *     }, {}, ...
 * ]
 * }
 * </pre>
 *
 * @author l.gong
 */
public class TaskInfoListService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(TaskInfoListService.class);

    @Override
    public void start() throws Exception {
        super.start();
        registerService(ServiceSymbols.SERVICE_INFO_FLOWS);
    }

    @Override
    protected String getServiceName() {
        return TaskInfoListService.class.getName();
    }

    @Override
    protected Future<IResult> doService() {
        logger.trace("doService -> {}", inData);
        return Future.future(promise -> {
            JsonArray flows = new JsonArray();
            outData.put(Symbols.FLOWS, flows);
            Map<String, TaskDefineFileProp> info =  TaskDefineRepo.copyDefineFileInfo();
            info.forEach((url, prop) -> {
                JsonObject flow = new JsonObject();
                flow.put(Symbols.URL, prop.getFlowUrl());
                flow.put(Symbols.LAST_MODIFIED_TIME, StringUtils.toDatetimeStr(prop.getLastModifiedTime(), zone));
                flows.add(flow);
            });
            promise.complete(ResultEnum.SUCCESS);
        });
    }

}
