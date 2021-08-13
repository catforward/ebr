/*
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
package pers.tsm.ebr.base;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static pers.tsm.ebr.common.AppConsts.*;

/**
 * <pre>service's result message</pre>
 *
 * @author l.gong
 */
public class ServiceResultMsg {
    private final String code;
    private final String message;
    private final JsonObject data = new JsonObject();

    public ServiceResultMsg(IResult result) {
        this.code = result.getCode();
        this.message = result.getMessage();
    }

    public ServiceResultMsg setData(JsonObject jsonObject) {
        this.data.mergeIn(jsonObject);
        return this;
    }

    public ServiceResultMsg addArrayData(String key, JsonArray jsonArray) {
        this.data.put(key, jsonArray);
        return this;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(CODE, this.code);
        jsonObject.put(MSG, this.message);
        if (!this.data.isEmpty()) {
            jsonObject.put(DATA, this.data);
        }
        return jsonObject;
    }

    public JsonObject rawData() {
        return data;
    }

    @Override
    public String toString() {
        return this.toJsonObject().toString();
    }
}
