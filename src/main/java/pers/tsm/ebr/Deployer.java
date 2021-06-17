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

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppContext;
import pers.tsm.ebr.common.VerticleDesc;
import pers.tsm.ebr.service.FsDataWatchService;

import static pers.tsm.ebr.service.ServiceSymbols.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 *
 * @author l.gong
 */
public class Deployer {
	
	public static Future<Void> collect() {
		return Future.future(promise -> {
			// Cache
	        // API
			AppContext.addApiServiceMapping(URL_TASKS_INFO, SERVICE_TASKS_INFO);
	        // Vertical
			AppContext.addVerticle(FsDataWatchService::new, new DeploymentOptions().setInstances(1).setWorker(true));
			// complete
			promise.complete();
		});
	}

	public static Future<Void> deploy(Vertx vertx, JsonObject config) {
		return Future.future(promise -> {
			List<VerticleDesc> descList = AppContext.getVerticleDescList();
			ArrayList<Future> futureList = new ArrayList<>(descList.size());
            descList.forEach(desc -> {
            	futureList.add(deployVerticle(vertx, desc.getVerticle(), desc.getOptions().setConfig(config)));
            });
            CompositeFuture.all(futureList).onSuccess(ar -> promise.complete()).onFailure(promise::fail);
        });
	}
	
	public static Future<Void> deployVerticle(Vertx vertx, Supplier<Verticle> verticleSupplier, DeploymentOptions options) {
        return Future.future(promise -> {
            vertx.deployVerticle(verticleSupplier, options, ar -> {
                if (ar.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(ar.cause());
                }
            });
        });
    }
}
