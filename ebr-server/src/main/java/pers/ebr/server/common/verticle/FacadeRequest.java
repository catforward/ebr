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

import java.util.HashMap;
import java.util.Map;

import static pers.ebr.server.common.verticle.VerticleConst.FACADE_DATA;
import static pers.ebr.server.common.verticle.VerticleConst.FACADE_MSG;

/**
 * <p>
 * 定义Verticle的请求数据
 * {
 *    msg: "api.proc",
 *    data: {
 *        "key": "value",
 *        ...
 *    }
 * }
 * </p>
 *
 * @author l.gong
 */
public final class FacadeRequest {
    /** 请求信息(请求ID) */
    private String msg;
    /** 请求数据 */
    private final Map<String, Object> data = new HashMap<>();

    FacadeRequest(Message<JsonObject> msg) {
        reset(msg);
    }

    void reset(Message<JsonObject> msg) {
        this.msg = msg.body().getString(FACADE_MSG);
        data.clear();
        JsonObject jsonData = msg.body().getJsonObject(FACADE_DATA);
        for (Map.Entry<String, Object> entry : jsonData) {
            data.put(entry.getKey(), entry.getValue());
        }
    }

    public String getMsg() {
        return this.msg;
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public Map<String, Object> getData() {
        return Map.copyOf(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
