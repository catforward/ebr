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
package pers.ebr;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.AppConfigs;
import pers.ebr.base.HttpApiServer;
import pers.ebr.data.CronFlowRepo;
import pers.ebr.data.TaskDefineRepo;
import pers.ebr.data.TaskRepo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

/**
 * <pre>External Batch Runner</pre>
 *
 * @author l.gong
 */
public final class AppMain {
    private static final Logger logger = LoggerFactory.getLogger(AppMain.class);
    
    private static final String MAJOR = "4";
    private static final String MINOR = "0";
    private static final String PHASE = "alpha";
    public static final String VERSION = MAJOR + "." + MINOR + "-" + PHASE;
    
    private Vertx vertx;

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new AppMain().launch();
    }

    void launch() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    onShutdown();
                } catch (InterruptedException ex) {
                    logger.error("error occurred!", ex);
                    Thread.currentThread().interrupt();
                }
            }));

            logger.info(printBanner());
            AppConfigs.load();
            JsonObject config = AppConfigs.get();
            vertx = Vertx.vertx(new VertxOptions(config.getJsonObject(AppConfigs.SECTION_VERTX)));
            Deployer.collect(config.getJsonObject(AppConfigs.SECTION_SERVICE))
            .compose(serviceConfig -> Deployer.deploy(vertx, serviceConfig))
            .onSuccess(ar -> {
                logger.info("All Vertical deploy done.");
                vertx.deployVerticle(HttpApiServer::new, new DeploymentOptions()
                        .setConfig(config.getJsonObject(AppConfigs.SECTION_HTTP)))
                .onFailure(ex -> System.exit(1));
            }).onFailure(ex -> {
                logger.error("Vertical deploy failed...", ex);
                System.exit(1);
            });
        } catch (Exception ex) {
            logger.error("application error occurred!", ex);
            System.exit(1);
        }
    }

    void onShutdown() throws InterruptedException {
        TaskDefineRepo.release();
        TaskRepo.release();
        CronFlowRepo.release();
        AppConfigs.release();
        if (isNull(vertx)) {
            return;
        }
        logger.info("start stop vertx");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        vertx.close(ar -> countDownLatch.countDown());
        if(countDownLatch.await(10, TimeUnit.SECONDS)) {
            logger.info("stop vertx success");
        }
    }

    String printBanner() {
        String logo = "\n";
        logo += "                                          \n";
        logo += "                                          \n";
        logo += "         ███████╗██████╗ ██████╗          \n";
        logo += "         ██╔════╝██╔══██╗██╔══██╗         \n";
        logo += "         █████╗  ██████╔╝██████╔╝         \n";
        logo += "         ██╔══╝  ██╔══██╗██╔══██╗         \n";
        logo += "         ███████╗██████╔╝██║  ██║         \n";
        logo += "         ╚══════╝╚═════╝ ╚═╝  ╚═╝         \n";
        logo += "                                          \n";
        logo += "             Ver:" + AppMain.VERSION + "                \n";
        return logo;
    }

}
