/**
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
package pers.tsm.ebr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.Configs;
import pers.tsm.ebr.service.WebApiServer;

import static java.util.Objects.isNull;

import java.util.concurrent.CountDownLatch;

/**
 *
 *
 * @author l.gong
 */
public final class AppMain {
	private static final Logger logger = LoggerFactory.getLogger(AppMain.class);
    private Vertx vertx;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AppMain().launch();
	}
	
	void launch() {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { onShutdown(); } catch (Exception ex) {
                    logger.error("unknown error occurred!", ex);
                }
            }));
			
			Configs.load();
			JsonObject config = Configs.get();
			vertx = Vertx.vertx(new VertxOptions(config.getJsonObject("vertx")));
			Deployer.collect()
			.compose(v -> Deployer.deploy(vertx, config.getJsonObject("server")))
			.onSuccess(ar -> {
				logger.info("All Vertical deploy done.");
				vertx.deployVerticle(WebApiServer::new,
						new DeploymentOptions().setConfig(config.getJsonObject("http")))
				.onSuccess(ar2 -> logger.info(Logo.print()))
				.onFailure(ex2 -> {
					logger.error("WebApiServer deploy failed...", ex2);
					System.exit(1);
				});
			}).onFailure(ex -> {
				logger.error("Vertical deploy failed...", ex);
                System.exit(1);
			});
		} catch (Exception ex) {
			logger.error("application error occurred!", ex);
            System.exit(1);
		}
	}
	
	void onShutdown() {
        Configs.release();
        if (isNull(vertx)) return;
        logger.info("start stop vertx");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        vertx.close(res -> {
        	countDownLatch.countDown();
        	if (countDownLatch.getCount() == 0) {
        		logger.info("stop vertx success");
        	}
        });
    }

}
