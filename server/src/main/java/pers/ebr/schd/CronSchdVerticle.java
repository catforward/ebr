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
package pers.ebr.schd;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.AppConfigs;
import pers.ebr.base.BaseScheduler;
import pers.ebr.base.ServiceSymbols;
import pers.ebr.data.CronFlowRepo;
import pers.ebr.data.Flow;
import pers.ebr.types.TaskStateEnum;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.util.Objects.isNull;
import static pers.ebr.base.AppConsts.EMPTY_JSON_OBJ;
import static pers.ebr.types.TaskStateEnum.STORED;

/**
 * <pre>checking flow's cron expr every n seconds</pre>
 *
 * @author l.gong
 */
public class CronSchdVerticle extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CronSchdVerticle.class);

    private long timerId = 0L;
    private long checkInterval = 0L;

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_CRON_CHECK, this::handlePeriodic);
        checkInterval = config().getLong(AppConfigs.SERVICE_CRON_SCHD_CHECK_INTERVAL_SECONDS, 5L);
        // the first time
        vertx.setTimer(checkInterval * 1000, id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_CRON_CHECK, EMPTY_JSON_OBJ));
        String deploymentId = deploymentID();
        logger.info("CronSchdVerticle started. [{}]", deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.cancelTimer(timerId);
        String deploymentId = deploymentID();
        logger.info("CronSchdVerticle stopped. [{}]", deploymentId);
    }

    private void handlePeriodic(Message<JsonObject> msg) {
        CronFlowRepo.getCronSchdFlowPoolRef().forEach((flowUrl, flowObj) -> {
            TaskStateEnum state = flowObj.getState();
            Cron cron = flowObj.getCron();
            if (STORED == state && !isNull(cron)) {
                LocalDateTime lastFinTime = flowObj.getLatestResetDateTime();
                ZonedDateTime now = ZonedDateTime.now(AppConfigs.getZoneId());
                ZonedDateTime from = isNull(lastFinTime) ? now : ZonedDateTime.of(lastFinTime, AppConfigs.getZoneId());

                // cal next execute time
                ExecutionTime executionTime = ExecutionTime.forCron(cron);
                Optional<Duration> timeToNextExecution = executionTime.timeToNextExecution(now);
                if (timeToNextExecution.isEmpty()) {
                    logger.debug("timeToNextExecution is null. skip...");
                    return;
                }

                // improve me
                long secToNextExec = timeToNextExecution.get().get(ChronoUnit.SECONDS);

                if (AppConfigs.isDevMode()) {
                    logCronInfo(flowObj, executionTime, now, from, secToNextExec);
                }

                if (secToNextExec <= checkInterval) {
                    logger.info("Cron: will launch by Cron Schd. flow:{}", flowUrl);
                    launchFlow(flowObj);
                }
            }
        });
        // for next time
        timerId = vertx.setTimer(checkInterval * 1000,
                id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_CRON_CHECK, EMPTY_JSON_OBJ));
    }

    private void logCronInfo(Flow flow, ExecutionTime executionTime, ZonedDateTime now, ZonedDateTime from, long secToNextExec) {
        Optional<ZonedDateTime> nextExecTime = executionTime.nextExecution(from);
        String nextExecTimeStr = "unknown";
        if (nextExecTime.isPresent()) {
            nextExecTimeStr = nextExecTime.get().toString();
        }

        logger.debug("Cron Info: flow:{}, lastFinTime:{}, nowTime:{}, fromTime:{}, nextExecTime:{}, secToNextExec:{}, checkInterval:{}",
                flow.getUrl(), flow.getLatestResetDateTime(), now, from, nextExecTimeStr, secToNextExec, checkInterval);
    }

}
