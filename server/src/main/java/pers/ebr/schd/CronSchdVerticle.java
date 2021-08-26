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
import pers.ebr.base.*;
import pers.ebr.data.CronFlowRepo;
import pers.ebr.data.Flow;
import pers.ebr.data.TaskRepo;
import pers.ebr.types.ResultEnum;
import pers.ebr.types.TaskStateEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Objects.isNull;
import static pers.ebr.base.AppConsts.EMPTY_JSON_OBJ;
import static pers.ebr.types.TaskStateEnum.STORED;

/**
 * <pre>checking flow's cron expr every n seconds</pre>
 *
 * @author l.gong
 */
public class CronSchdVerticle extends BaseSchdVerticle {
    private static final Logger logger = LoggerFactory.getLogger(CronSchdVerticle.class);

    private long timerId = 0L;
    private long checkInterval = 0L;

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer(ServiceSymbols.MSG_STATE_FLOW_LAUNCH, this::onFlowLaunchMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_CRON_REJECT, this::onCronRejectMsg);
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_CRON_CHECK, this::handlePeriodic);
        checkInterval = config().getLong(AppConfigs.SERVICE_CRON_SCHD_CHECK_INTERVAL_SECONDS, 5L) * 1000;
        // the first time
        vertx.setTimer(checkInterval, id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_CRON_CHECK, EMPTY_JSON_OBJ));
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
        ZoneId zoneId = AppConfigs.getZoneId();
        CronFlowRepo.getCronSchdFlowPoolRef().forEach((flowUrl, flowObj) -> {
            TaskStateEnum state = flowObj.getState();
            Cron cron = flowObj.getCron();
            if (STORED == state && !isNull(cron)) {
                // cal next execute time
                ExecutionTime executionTime = ExecutionTime.forCron(cron);
                LocalDateTime lastFinTime = flowObj.getLatestResetDateTime();
                LocalDateTime nowTime = LocalDateTime.now(zoneId);
                Optional<ZonedDateTime> nextExecTime = executionTime.nextExecution(
                        ZonedDateTime.of(isNull(lastFinTime) ? nowTime: lastFinTime, zoneId));
                if (nextExecTime.isPresent() && nextExecTime.get().toLocalDateTime().isBefore(nowTime)) {
                    logger.debug("Cron: lastFinTime:{}, nextExecTime:{}, nowTime:{}", lastFinTime, nextExecTime, nowTime);
                    notice(ServiceSymbols.MSG_ACTION_TASK_START, flowObj);
                    logger.debug("Cron: Launch Msg Sending by Cron Schd. flow:{}", flowUrl);
                }
            }
        });
        // for next time
        timerId = vertx.setTimer(checkInterval,
                id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_CRON_CHECK, EMPTY_JSON_OBJ));
    }

    private void onFlowLaunchMsg(Message<JsonObject> msg) {
        JsonObject target = msg.body();
        String flowUrl = target.getString(AppConsts.FLOW);
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }
        if (!StringUtils.isNullOrBlank(flow.getRootTask().getCronStr())) {
            CronFlowRepo.addFlow(flow);
        }
    }

    private void onCronRejectMsg(Message<JsonObject> msg) {
        JsonObject target = msg.body();
        String flowUrl = target.getString(AppConsts.FLOW);
        Flow flow = TaskRepo.getFlow(flowUrl);
        if (isNull(flow)) {
            throw new AppException(ResultEnum.ERR_11003);
        }
        CronFlowRepo.removeFlow(flow);
    }
}
