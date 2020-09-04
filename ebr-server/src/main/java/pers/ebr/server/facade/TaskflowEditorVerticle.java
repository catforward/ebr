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
package pers.ebr.server.facade;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.repository.RepositoryException;
import pers.ebr.server.common.verticle.MulitFunctionalFacadeVerticle;
import pers.ebr.server.common.verticle.FacadeContext;
import pers.ebr.server.domain.ITaskflow;
import pers.ebr.server.domain.ModelItemMaker;
import pers.ebr.server.common.repository.Repository;

import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.facade.FacadeTopic.*;

/**
 * <p>taskflow的编辑请求处理类</p>
 * <ul>
 *     <li>验证</li>
 *     <li>保存</li>
 *     <li>删除</li>
 * </ul>
 *
 * @author l.gong
 */
public class TaskflowEditorVerticle extends MulitFunctionalFacadeVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskflowEditorVerticle.class);

    /**
     * Verticle初始化
     * @throws Exception 任意异常发生时
     */
    @Override
    protected void onStart() throws Exception {
        subscribe(API_VALIDATE_FLOW, this::handleValidateTaskflow);
        subscribe(API_SAVE_FLOW, this::handleSaveTaskflow);
        subscribe(API_DELETE_FLOW, this::handleDeleteTaskflow);
        subscribe(API_DUMP_FLOW_DEF, this::handleDumpTaskflowDefine);
    }

    /**
     * Verticle结束
     * @throws Exception 任意异常发生时
     */
    @Override
    protected void onStop() throws Exception {

    }

    /**
     * 验证taskflow的定义
     * @param ctx 请求上下文
     * @return boolean true: 处理成功 false: 处理失败
     */
    private boolean handleValidateTaskflow(FacadeContext ctx) {
        JsonObject flowBody = new JsonObject((String) ctx.getRequest().getData(REQUEST_FLOW_DEFINE));
        if (flowBody.isEmpty()) {
            return false;
        }
        ITaskflow workflow = ModelItemMaker.makeExternalTaskflow(flowBody);
        logger.debug("create a task flow -> {}", workflow.toString());
        return true;
    }

    /**
     * 持久化taskflow的定义
     * @param ctx 请求上下文
     * @return boolean true: 处理成功 false: 处理失败
     * @throws RepositoryException 存储异常发生时
     */
    private boolean handleSaveTaskflow(FacadeContext ctx) throws RepositoryException {
        JsonObject flowBody = new JsonObject((String) ctx.getRequest().getData(REQUEST_FLOW_DEFINE));
        if (flowBody.isEmpty()) {
            return false;
        }
        ITaskflow workflow = ModelItemMaker.makeExternalTaskflow(flowBody);
        Repository.getDb().saveTaskflow(workflow);
        return true;
    }

    /**
     * 删除指定的taskflow的定义
     * @param ctx 请求上下文
     * @return boolean true: 处理成功 false: 处理失败
     * @throws RepositoryException 存储异常发生时
     */
    private boolean handleDeleteTaskflow(FacadeContext ctx) throws RepositoryException {
        String flowId = (String) ctx.getRequest().getData(MSG_PARAM_TASKFLOW_ID);
        // check
        if (flowId == null || flowId.isBlank()) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("invalid taskflow id: [%s]", flowId));
            ctx.setResponseData(errInfo);
            return false;
        }
        String flowUrl = String.format("/%s", flowId);
        if (Repository.getPool().getRunningTaskflowByPath(flowUrl) != null) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("taskflow id: [%s] is already running", flowId));
            ctx.setResponseData(errInfo);
            return false;
        }
        // delete it
        int cnt = Repository.getDb().removeTaskflow(flowId);
        if (cnt == 0) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("taskflow id: [%s] is already running", flowId));
            ctx.setResponseData(errInfo);
            return false;
        } else {
            JsonObject retInfo = new JsonObject();
            retInfo.put(RESPONSE_INFO, String.format("delete records of taskflow : [%s]", cnt));
            ctx.setResponseData(retInfo);
            return true;
        }
    }

    private boolean handleDumpTaskflowDefine(FacadeContext ctx) {
        // TODO
        return false;
    }

}
