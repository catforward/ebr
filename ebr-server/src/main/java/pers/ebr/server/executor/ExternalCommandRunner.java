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
package pers.ebr.server.executor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static pers.ebr.server.common.Configs.KEY_EXECUTOR_NUM_MAX;
import static pers.ebr.server.common.Configs.KEY_EXECUTOR_NUM_MIN;

/**
 * The SchedulerVerticle
 *
 * @author l.gong
 */
public class ExternalCommandRunner extends AbstractVerticle {
    private final static Logger logger = LoggerFactory.getLogger(ExternalCommandRunner.class);
    /** 执行队列 */
    private ExecutorService executorPool;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        Integer minNum = config.getInteger(KEY_EXECUTOR_NUM_MIN);
        Integer maxNum = config.getInteger(KEY_EXECUTOR_NUM_MAX);
        executorPool = new ThreadPoolExecutor(minNum, maxNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new RunnerThreadFactory("ebr-runner-"));

        EventBus bus = vertx.eventBus();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executorPool.shutdown();
    }
}
