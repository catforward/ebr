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
package pers.ebr.server.common.model;

import io.vertx.core.json.JsonObject;

/**
 * <pre>
 * DTO数据，发送至客户端
 * </pre>
 *
 * @author l.gong
 */
public interface IDetail {

    /**
     * WORKFLOW型的详细数据
     */

    int WORKFLOW = 1;
    /**
     * TASK型的详细数据
     */
    int TASK = 2;

    /**
     * 详细数据的类型
     * @return int
     */
    int type();

    /**
     * 返回此数据的JSON对象
     * @return String
     */
    JsonObject toJsonObject();
}
