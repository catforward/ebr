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

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.common.repository.Repository;
import pers.ebr.server.common.repository.RepositoryException;
import pers.ebr.server.common.verticle.MulitFunctionalFacadeVerticle;
import pers.ebr.server.common.verticle.FacadeContext;
import pers.ebr.server.domain.ExternalCommandTaskflowView;

import java.util.Collection;

import static pers.ebr.server.application.AppTopic.MSG_LAUNCH_FLOW;
import static pers.ebr.server.common.Const.*;
import static pers.ebr.server.facade.FacadeTopic.API_QUERY_ALL_FLOW;
import static pers.ebr.server.facade.FacadeTopic.API_LAUNCH_FLOW;

/**
 * <p>taskflow的管理请求处理类</p>
 *  <ul>
 *      <li>获取所有taskflow</li>
 *      <li>启动指定的taskflow</li>
 *      <li>停止指定的taskflow</li>
 *  </ul>
 * @author l.gong
 */
public class TaskflowManagerVerticle extends MulitFunctionalFacadeVerticle {
    private final static Logger logger = LoggerFactory.getLogger(TaskflowManagerVerticle.class);

    /**
     * Verticle初始化
     *
     * @throws Exception 任意异常发生时
     */
    @Override
    protected void onStart() throws Exception {
        subscribe(API_QUERY_ALL_FLOW, this::handleQueryAllTaskflow);
        subscribe(API_LAUNCH_FLOW, this::handleLaunchTaskflow);
    }

    /**
     * Verticle结束
     *
     * @throws Exception 任意异常发生时
     */
    @Override
    protected void onStop() throws Exception {

    }

    /**
     * 获取所有taskflow定义
     * @param ctx 请求上下文
     * @return boolean true: 处理成功 false: 处理失败
     * @throws RepositoryException 存储异常发生时
     */
    private boolean handleQueryAllTaskflow(FacadeContext ctx) throws RepositoryException {
        Collection<ExternalCommandTaskflowView> flows = Repository.getDb().getAllTaskflowDetail();
        if (!flows.isEmpty()) {
            JsonArray array = new JsonArray();
            flows.forEach(workflowView -> array.add(workflowView.toJsonObject()));
            ctx.setResponseData(new JsonObject().put(REQUEST_FLOW_ARRAY, array));
        } else {
            ctx.setResponseData(new JsonObject());
        }
        return true;
    }

    /**
     * 启动指定的taskflow
     * @param ctx 请求上下文
     * @return boolean true: 处理成功 false: 处理失败
     * @throws RepositoryException 存储异常发生时
     */
    private boolean handleLaunchTaskflow(FacadeContext ctx) throws RepositoryException  {
        String flowId = (String) ctx.getRequest().getData(MSG_PARAM_TASKFLOW_ID);
            // check
        if (flowId == null || flowId.isBlank()) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("invalid workflow id: [%s]", flowId));
            ctx.setResponseData(errInfo);
            return false;
        }
        String flowPath = String.format("/%s", flowId);
        if (Repository.getPool().getRunningTaskflowByPath(flowPath) != null) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("workflow id: [%s] is already running", flowId));
            ctx.setResponseData(errInfo);
            return false;
        }
        if (!Repository.getDb().isTaskflowExists(flowId)) {
            JsonObject errInfo = new JsonObject();
            errInfo.put(RESPONSE_INFO, String.format("define is not exists (id: [%s])", flowId));
            ctx.setResponseData(errInfo);
            return false;
        }

        EventBus bus = vertx.eventBus();
        JsonObject noticeParam = new JsonObject();
        noticeParam.put(MSG_PARAM_TASKFLOW_ID, flowId);
        bus.publish(MSG_LAUNCH_FLOW, noticeParam);

        // response
        JsonObject retInfo = new JsonObject();
        retInfo.put(RESPONSE_INFO, String.format("task flow start. (id: [%s])", flowId));
        ctx.setResponseData(retInfo);
        return true;
    }

}
