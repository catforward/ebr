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
package pers.ebr.base;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.types.ResultEnum;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>Service's worker</pre>
 *
 * @author l.gong
 */
public abstract class BaseService extends BaseVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BaseService.class);
    protected JsonObject inData;
    protected JsonObject outData;

    @Override
    public void start() throws Exception {
        super.start();
        String deploymentId = deploymentID();
        logger.info("Service: {} started. [{}]", getServiceName(), deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        String deploymentId = deploymentID();
        logger.info("Service: {} stopped. [{}]", getServiceName(), deploymentId);
    }

    protected void registerService(String serviceId) {
        requireNonNull(serviceId);
        vertx.eventBus().consumer(serviceId, this::handleServiceEvent);
    }

    protected void registerRequest(String requestId) {
        requireNonNull(requestId);
        vertx.eventBus().consumer(requestId, this::handleRequestEvent);
    }
    
    protected void registerMsg(String msgId) {
        requireNonNull(msgId);
        vertx.eventBus().consumer(msgId, this::handleMsgEvent);
    }

    protected JsonObject getPostBody() {
        return isNull(inData) ? AppConsts.EMPTY_JSON_OBJ : inData.getJsonObject(AppConsts.BODY);
    }

    protected String getParameter(String name, String def) {
        if (isNull(inData) || isNull(name)) {
            return def;
        }
        String value;
        switch (name) {
            case AppConsts.USER_AGENT: { value = inData.getString(AppConsts.USER_AGENT); break; }
            case AppConsts.METHOD: { value = inData.getString(AppConsts.METHOD); break; }
            case AppConsts.PATH:{ value = inData.getString(AppConsts.PATH); break; }
            default: { value = def; break; }
        }
        return isNull(value) ? def : value;
    }

    protected String getParameter(String name) {
        return getParameter(name, AppConsts.BLANK_STR);
    }

    protected Future<IResult> doPrepare() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    protected abstract String getServiceName();

    protected Future<IResult> doRequest() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    protected void handleRequestEvent(Message<JsonObject> msg) {
        try {
            inData = msg.body();
            outData = new JsonObject();
            doPrepare()
            .compose(ret -> doRequest())
            .onSuccess(ret -> makeReplyMsg(msg, ret))
            .onFailure(ex -> {
               if (ex instanceof AppException) {
                   logger.debug(AppConsts.BLANK_STR, ex.getCause());
                   AppException se = (AppException) ex;
                   msg.reply(new ServiceResultMsg(se.getReason()).toJsonObject());
               } else {
                   logger.error(AppConsts.BLANK_STR, ex);
                   msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
               }
            });
        } catch (Exception ex) {
            logger.error(AppConsts.BLANK_STR, ex);
            msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
        }
    }

    protected Future<IResult> doService() {
        return Future.future(promise -> promise.complete(ResultEnum.SUCCESS));
    }

    protected void handleServiceEvent(Message<JsonObject> msg) {
        try {
            inData = msg.body();
            outData = new JsonObject();
            doPrepare()
            .compose(ret -> doService())
            .onSuccess(ret -> makeReplyMsg(msg, ret))
            .onFailure(ex -> {
                if (ex instanceof AppException) {
                    logger.debug(AppConsts.BLANK_STR, ex.getCause());
                    AppException se = (AppException) ex;
                    msg.reply(new ServiceResultMsg(se.getReason()).toJsonObject());
                } else {
                    logger.error(AppConsts.BLANK_STR, ex);
                    msg.reply(new ServiceResultMsg(ResultEnum.ERR_500).toJsonObject());
                }
            });
        } catch (Exception ex) {
            logger.error(AppConsts.BLANK_STR, ex);
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

    private void makeReplyMsg(Message<JsonObject> msg, IResult ret) {
        logger.trace("makeReplyMsg: {}", outData);
        msg.reply(new ServiceResultMsg(ret).setData(outData).toJsonObject());
    }

}
