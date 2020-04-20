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
package pers.ebr.server;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pers.ebr.server.base.db.DBStore;
import pers.ebr.server.base.pool.TaskStore;
import pers.ebr.server.base.Properties;
import pers.ebr.server.verticle.TaskManageVerticle;
import pers.ebr.server.verticle.TaskScheduleVerticle;
import pers.ebr.server.verticle.HttpServerVerticle;
import pers.ebr.server.verticle.ServerInfoVerticle;

/**
 * The Launcher of EBR-Server
 * 
 * @author l.gong
 */
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private static Vertx vertx = null;

    /**
     * Initialize and start the EBR-Server
     */
    public static void main(String[] args) {
        try {
            initBasicComponents();
            vertx = Vertx.vertx();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (vertx != null) vertx.close();
                DBStore.finish();
                TaskStore.finish();
            }));
            deployWorkerVerticle();
            deployHttpVerticle();
            logger.info("Starting EBR Server achieved by Vert.x(default event loop pool size is {})", VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
        } catch (Exception ex) {
            logger.error("application error occurred!", ex);
            System.err.println(ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    private static void initBasicComponents() throws Exception {
        Properties.load();
        DBStore.init(Properties.getConfig());
        TaskStore.init(Properties.getConfig());
    }

    private static void deployWorkerVerticle() {
        DeploymentOptions serverInfoOpts = new DeploymentOptions()
                .setConfig(Properties.getConfig())
                .setInstances(1).setWorker(true);
        vertx.deployVerticle(ServerInfoVerticle::new, serverInfoOpts);

        DeploymentOptions mngOpts = new DeploymentOptions().setInstances(1).setWorker(true);
        vertx.deployVerticle(TaskManageVerticle::new, mngOpts);

        DeploymentOptions schdOpts = new DeploymentOptions().setInstances(1).setWorker(true);
        vertx.deployVerticle(TaskScheduleVerticle::new, schdOpts);
    }

    private static void deployHttpVerticle() {
        DeploymentOptions httpOpts = new DeploymentOptions()
                .setConfig(Properties.getConfig())
                .setInstances(1);
        vertx.deployVerticle(HttpServerVerticle::new, httpOpts);
    }

}
