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
package pers.ebr.server.model;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 执行器的执行状态统计数据
 * </p>
 *
 * @author l.gong
 */
public final class ExecutorStatisticsView implements IObjectConverter {

    private final String type;
    private final AtomicInteger activeCnt;
    private final AtomicInteger completeSumCnt;
    private final AtomicInteger failedSumCnt;

    ExecutorStatisticsView(String schdType) {
        this.type = schdType;
        this.activeCnt = new AtomicInteger();
        this.completeSumCnt = new AtomicInteger();
        this.failedSumCnt = new AtomicInteger();
    }

    /**
     * 重置统计数据
     */
    public void reset() {
        this.activeCnt.set(0);
        this.completeSumCnt.set(0);
        this.failedSumCnt.set(0);
    }

    /**
     * 运行中任务计数器自增
     */
    public void incActiveCnt() {
        this.activeCnt.incrementAndGet();
    }

    /**
     * 运行中任务计数器自减
     */
    public void decActiveCnt() {
        if (this.activeCnt.get() > 0) {
            this.activeCnt.decrementAndGet();
        }
    }

    /**
     * 执行完成任务计数器自增
     */
    public void incCompleteSumCnt() {
        this.completeSumCnt.incrementAndGet();
    }

    /**
     * 执行失败任务计数器自增
     */
    public void incFailedSumCnt() {
        this.failedSumCnt.incrementAndGet();
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
