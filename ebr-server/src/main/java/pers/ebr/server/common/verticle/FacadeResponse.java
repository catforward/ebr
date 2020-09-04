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

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.JsonObject;
import pers.ebr.server.common.IObjectConverter;
import pers.ebr.server.common.ResultEnum;

import java.util.HashMap;
import java.util.Map;

import static pers.ebr.server.common.verticle.VerticleConst.*;

/**
 * <p>
 * 定义Verticle的返回结果
 * 正常时：
 * {
 *    msg: "api.proc",
 *    ret: true,
 *    code: 20000,
 *    data: {
 *        "key": "value",
 *        ...
 *    }
 * }
 *
 * 异常时：
 * {
 *     msg: "api.proc",
 *     ret: false,
 *     code: 20001,
 *     data: {
 *         error: "error_message",
 *         ...
 *     }
 * }
 * </p>
 *
 * @author l.gong
 */
public final class FacadeResponse implements IObjectConverter {
    /** 响应是否成功 */
    private final Boolean ret;
    /** 响应状态码 */
    private final Integer code;
    /** 响应信息(请求ID) */
    private final String msg;
    /** 响应结果数据 */
    private JsonObject data = new JsonObject();

    public FacadeResponse(String message, Boolean success, Integer code) {
        this.msg = message;
        this.ret = success;
        this.code = code;
    }

    public FacadeResponse(ResultEnum result) {
        this.msg = result.getMessage();
        this.ret = result.getSuccess();
        this.code = result.getCode();
    }

    public static FacadeResponse ok(String message) {
        return new FacadeResponse(message,
                ResultEnum.SUCCESS.getSuccess(),
                ResultEnum.SUCCESS.getCode());
    }

    public static FacadeResponse ng(String message) {
        return new FacadeResponse(message,
                ResultEnum.ERROR.getSuccess(),
                ResultEnum.ERROR.getCode());
    }

    public static FacadeResponse of(ResultEnum result) {
        return new FacadeResponse(result);
    }

    public FacadeResponse setData(Map<String,Object> map) {
        map.forEach(this.data::put);
        return this;
    }

    public FacadeResponse setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public FacadeResponse setData(JsonObject data) {
        this.data.mergeIn(data);
        return this;
    }

    /**
     * 返回此数据的JSON对象
     *
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(FACADE_MSG, msg);
        jsonObject.put(FACADE_RESULT, ret);
        jsonObject.put(FACADE_CODE, code);
        jsonObject.put(FACADE_DATA, data);
        return jsonObject;
    }
}
