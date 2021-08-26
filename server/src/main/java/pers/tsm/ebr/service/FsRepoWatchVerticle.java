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
package pers.tsm.ebr.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.tsm.ebr.common.AppConfigs;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.data.TaskDefineRepo;

import java.io.IOException;

import static pers.tsm.ebr.common.AppConsts.EMPTY_JSON_OBJ;

/**
 * <pre>Refreshing define file's folder every n seconds</pre>
 *
 * @author l.gong
 */
public class FsRepoWatchVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(FsRepoWatchVerticle.class);

    private long timerID = 0L;
    private long scanInterval = 0L;

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE, this::onRefreshFsDefineMsg);
        scanInterval = config().getLong(AppConfigs.SERVICE_FS_DATA_CHECK_INTERVAL_SECONDS, 30L) * 1000;
        // the first time
        vertx.setTimer(2000, id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE, EMPTY_JSON_OBJ));
        String deploymentId = deploymentID();
        logger.info("FsRepoWatchVerticle started. [{}]", deploymentId);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.cancelTimer(timerID);
        String deploymentId = deploymentID();
        logger.info("FsRepoWatchVerticle stopped. [{}]", deploymentId);
    }

    private void onRefreshFsDefineMsg(Message<JsonObject> msg) {
        try {
            TaskDefineRepo.reload();
        } catch (IOException ex) {
            logger.error("reload data folder error...", ex);
        } finally {
            // for next time
            timerID = vertx.setTimer(scanInterval,
                    id -> vertx.eventBus().publish(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE, EMPTY_JSON_OBJ));
        }
    }

}
