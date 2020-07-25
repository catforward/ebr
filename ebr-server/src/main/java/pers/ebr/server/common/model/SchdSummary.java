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

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

import static pers.ebr.server.common.model.ITask.*;

/**
 * <pre>
 * TaskPoolDetail Object
 * </pre>
 *
 * @author l.gong
 */
public final class SchdSummary implements IJsonObjectConverter {

    private final String schdType;
    /**
     * 执行中任务数
     */
    private final AtomicInteger activeCnt;
    /**
     * 正常结束累计任务数
     */
    private final AtomicInteger completeSumCnt;
    /**
     * 异常结束累计任务数
     */
    private final AtomicInteger failedSumCnt;

    SchdSummary(String schdType) {
        this.schdType = schdType;
        this.activeCnt = new AtomicInteger();
        this.completeSumCnt = new AtomicInteger();
        this.failedSumCnt = new AtomicInteger();
    }

    public void reset() {
        this.activeCnt.set(0);
        this.completeSumCnt.set(0);
        this.failedSumCnt.set(0);
    }

    public void setActiveCnt(int newValue) {
        this.activeCnt.set(newValue);
    }

    public void incCompleteSumCnt() {
        this.completeSumCnt.incrementAndGet();
    }

    public void incFailedSumCnt() {
        this.failedSumCnt.incrementAndGet();
    }

    public int getActiveCnt() {
        return this.activeCnt.get();
    }

    public int getCompleteSumCnt() {
        return this.completeSumCnt.get();
    }

    public int getFailedSumCnt() {
        return this.failedSumCnt.get();
    }

    /**
     * 返回此数据的JSON对象
     *
     * @return String
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("active", activeCnt.get());
        jsonObject.put("complete", completeSumCnt.get());
        jsonObject.put("failed", failedSumCnt.get());
        return jsonObject;
    }

}
