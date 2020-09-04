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
package pers.ebr.server.common.verticle;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * <pre>
 * 外部请求上下文
 * 使用：
 *  获取请求数据
 *  FacadeRequest req = getRequest();
 *  ...
 *  设置响应数据
 *  setResponseData("name", "value");
 *  or
 *  JsonObject data = new JsonObject();
 *  ...
 *  setResponseData(data);
 *
 *  发生错误时
 *  ...
 *  setResponseData("error", "msg");
 *  ...
 *
 * </pre>
 * @author l.gong
 */
public final class FacadeContext implements AutoCloseable {
    private Message<JsonObject> rawMsg;
    private final FacadeRequest request;
    private final JsonObject responseData;

    FacadeContext(Message<JsonObject> msg) {
        Objects.requireNonNull(msg);
        rawMsg = msg;
        responseData = new JsonObject();
        request = new FacadeRequest(msg);
    }

    /**
     * 释放资源
     */
    void release() {
        rawMsg = null;
    }

    /**
     * 获取原始请求信息
     *
     * @return Message 原始请求信息
     */
    public Message<JsonObject> getRawMessage() {
        return rawMsg;
    }

    /**
     * 获取响应数据
     *
     * @return JsonObject 响应数据
     */
    public JsonObject getResponseData() {
        return responseData;
    }

    /**
     * 获取外部请求
     * @return FacadeRequest 外部请求
     */
    public FacadeRequest getRequest() {
        return request;
    }

    /**
     * 添加响应数据
     *
     * @param data 待添加的响应数据
     */
    public void setResponseData(JsonObject data) {
        Objects.requireNonNull(data);
        responseData.mergeIn(data);
    }

    /**
     * 添加响应数据
     *
     * @param name  待添加响应数据名称
     * @param value 待添加响应数据值
     */
    public void setResponseData(String name, Object value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        responseData.put(name, value);
    }

    @Override
    public void close() throws Exception {
        release();
    }
}
