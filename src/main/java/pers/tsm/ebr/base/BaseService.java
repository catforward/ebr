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
package pers.tsm.ebr.base;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static pers.tsm.ebr.common.Symbols.BLANK_STR;
import static pers.tsm.ebr.common.Symbols.BODY;
import static pers.tsm.ebr.common.Symbols.METHOD;
import static pers.tsm.ebr.common.Symbols.PATH;
import static pers.tsm.ebr.common.Symbols.USER_AGENT;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppConfigs;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;

/**
 *
 *
 * @author l.gong
 */
public abstract class BaseService extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BaseService.class);
    protected JsonObject inData;
    protected JsonObject outData;
    protected final JsonObject emptyJsonObject = new JsonObject();
    protected final JsonArray emptyJsonArray = new JsonArray();
    protected ZoneId zone;

    @Override
    public void start() throws Exception {
        super.start();
        zone = ZoneId.of(config().getString(AppConfigs.SERVICE_TIMEZONE, "Asia/Tokyo"));
        logger.info("Service: {} started. [{}]", getServiceName(), deploymentID());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.info("Service: {} stopped. [{}]", getServiceName(), deploymentID());
    }

    /**
     * <p>注册服务</p>
     * @param serviceId 服务ID
     */
    protected void registerService(String serviceId) {
        requireNonNull(serviceId);
        vertx.eventBus().consumer(serviceId, this::handleServiceEvent);
    }

    /**
     * <p>注册内部请求服务</p>
     * @param requestId 服务ID
     */
    protected void registerRequest(String requestId) {
        requireNonNull(requestId);
        vertx.eventBus().consumer(requestId, this::handleRequestEvent);
    }
    
    protected void registerMsg(String msgId) {
        requireNonNull(msgId);
        vertx.eventBus().consumer(msgId, this::handleMsgEvent);
    }

    /**
     * <p>获取请求体</p>
     * @return 请求体
     */
    protected JsonObject getPostBody() {
        return isNull(inData) ? emptyJsonObject : inData.getJsonObject(BODY);
    }

    /**
     * <p>获取字符串型的请求参数值</p>
     * @param name 参数名
     * @param def 默认值
     * @return 参数值
     */
    protected String getParameter(String name, String def) {
        if (isNull(inData) || isNull(name)) {
            return def;
        }
        String value;
        switch (name) {
            case USER_AGENT: { value = inData.getString(USER_AGENT); break; }
            case METHOD: { value = inData.getString(METHOD); break; }
            case PATH:{ value = inData.getString(PATH); break; }
            default: { value = def; break; }
        }
        return isNull(value) ? def : value;
    }

    /**
     * <p>获取字符串型的请求参数值</p>
     * @param name 参数名
     * @return 参数值
     */
    protected String getParameter(String name) {
        return getParameter(name, BLANK_STR);
    }

    /**
     * <p>前置准备处理</p>
     * @return 结果返回码
     */
    protected Future<IResult> doPrepare() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    /**
     * <p>获取服务名称</p>
     * @return 服务名称
     */
    protected abstract String getServiceName();

    /**
     * <p>服务处理</p>
     * @return 结果返回码
     */
    protected Future<IResult> doRequest() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    /**
     * <p>内部请求处理</p>
     * @param msg 请求体
     */
    protected void handleRequestEvent(Message<JsonObject> msg) {
        try {
            inData = msg.body();
            outData = new JsonObject();
            doPrepare()
            .compose(ret -> doRequest())
            .onSuccess(ret -> makeReplyMsg(msg, ret))
            .onFailure(ex -> {
               if (ex instanceof AppException) {
                   logger.debug("Service Fail...", ex.getCause());
                   AppException se = (AppException) ex;
                   msg.reply(new ServiceResultMsg(se.getReason()).toJsonObject());
               } else {
                   logger.error("Unknown Error...", ex);
                   msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
               }
            });
        } catch (Exception ex) {
            logger.error("Unknown Error...", ex);
            msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
        }
    }

    /**
     * <p>服务处理</p>
     * @return 结果返回码
     */
    protected Future<IResult> doService() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    /**
     * <p>处理注册的事件</p>
     *
     * @param msg EventBus消息
     */
    protected void handleServiceEvent(Message<JsonObject> msg) {
        try {
            inData = msg.body();
            outData = new JsonObject();
            doPrepare()
            .compose(ret -> doService())
            .onSuccess(ret -> makeReplyMsg(msg, ret))
            .onFailure(ex -> {
                if (ex instanceof AppException) {
                    logger.debug("Service Fail...", ex.getCause());
                    AppException se = (AppException) ex;
                    msg.reply(new ServiceResultMsg(se.getReason()).toJsonObject());
                } else {
                    logger.error("Unknown Error...", ex);
                    msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
                }
            });
        } catch (Exception ex) {
            logger.error("Unknown Error...", ex);
            msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
        }
    }

    protected Future<IResult> doMsg() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    protected void handleMsgEvent(Message<JsonObject> msg) {
        try {
            inData = msg.body();
            outData = new JsonObject();

            doMsg().onFailure(ex -> {
                if (ex instanceof AppException) {
                    logger.debug("Service Fail...", ex.getCause());
                    AppException se = (AppException) ex;
                    msg.reply(new ServiceResultMsg(se.getReason()).toJsonObject());
                } else {
                    logger.error("Unknown Error...", ex);
                    msg.reply(new ServiceResultMsg(ResultEnum.ERROR).toJsonObject());
                }
            });
        } catch (Exception ex) {
            logger.error("Unknown Error...", ex);
            msg.reply(new ServiceResultMsg(ResultEnum.ERROR).toJsonObject());
        }
    }

    private Future<Void> makeReplyMsg(Message<JsonObject> msg, IResult ret) {
        return Future.future(promise -> {
            logger.trace("makeReplyMsg: {}", outData);
            msg.reply(new ServiceResultMsg(ret).toJsonObject());
            promise.complete();
        });
    }

    protected void emitMsg(String msg, JsonObject param) {
        vertx.eventBus().publish(msg, param);
    }

}
