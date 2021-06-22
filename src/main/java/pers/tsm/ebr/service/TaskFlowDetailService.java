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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.StringUtils;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.data.TaskDefineFileProp;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.types.ServiceResultEnum;

import static java.util.Objects.isNull;


/**
 * <pre>
 * request:
 * {
 * 	"flow":"xxx"
 * } * 
 * response: {
 * 	"flow": {
 * 		"url": "xxx",
 * 		"size": xxx,
 * 		"lastModifiedTime": "xxxx",
 * 		"content" : {...}
 * 	}
 * }
 * 
 * </pre>
 *
 * @author l.gong
 */
public class TaskFlowDetailService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(TaskFlowDetailService.class);
    
    @Override
    public void start() throws Exception {
        super.start();
        registerService(ServiceSymbols.SERVICE_INFO_FLOW);
    }

	@Override
	protected String getServiceName() {
		return TaskFlowDetailService.class.getName();
	}
	
	@Override
    public Future<IResult> doPrepare() {
        logger.trace("doPrepare -> {}", inData);
        return Future.future(promise -> {
            JsonObject postBody = getPostBody();
            String taskUrl = postBody.getString(Symbols.FLOW);
            if (isNull(taskUrl) || taskUrl.isBlank()) {
                logger.debug("flow's url is empty.");
                promise.fail(new ServiceException(ServiceResultEnum.RC_11001));
            } else {
                promise.complete(ServiceResultEnum.NORMAL);
            }
        });
    }
	
	@Override
    protected Future<IResult> doService() {
        logger.trace("doService -> {}", inData);
        return Future.future(promise -> {
        	JsonObject postBody = getPostBody();
            String flowUrl = postBody.getString(Symbols.FLOW);
        	JsonObject flowData = new JsonObject();
            outData.put(Symbols.FLOW, flowData);
            try {
            	TaskDefineFileProp prop = TaskDefineRepo.getDefineFileInfo(flowUrl);
            	flowData.put(Symbols.URL, prop.getFlowUrl());
            	flowData.put(Symbols.SIZE, prop.getFileSize());
            	flowData.put(Symbols.LAST_MODIFIED_TIME, StringUtils.toDatetimeStr(prop.getLastModifiedTime(), zone));
            	flowData.put(Symbols.CONTENT, TaskDefineRepo.getDefineFileContent(flowUrl));
            } catch (Exception ex) {
            	promise.fail(ex);
            	return;
            }
        	promise.complete(ServiceResultEnum.NORMAL);
        });
	}

}
