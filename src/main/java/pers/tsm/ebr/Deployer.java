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
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.data.VerticleProp;
import pers.tsm.ebr.service.FsRepoWatchService;
import pers.tsm.ebr.service.TaskFlowDetailService;
import pers.tsm.ebr.service.TaskFlowListService;

import static pers.tsm.ebr.service.ServiceSymbols.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 *
 *
 * @author l.gong
 */
public class Deployer {
	
	private Deployer() {}
	
	public static Future<JsonObject> collect(JsonObject config) {
		return Future.future(promise -> {
			// Cache
			Cache<String, JsonObject> cache = CacheBuilder.newBuilder()
	                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
	                .initialCapacity(config.getInteger("fsDataCacheInitialCapacity", 10))
	                .maximumSize(config.getInteger("fsDataCacheMaximumSize", 100))
	                .expireAfterWrite(config.getInteger("fsDataCacheExpireSeconds", 300), TimeUnit.SECONDS)
	                .removalListener(TaskDefineRepo.removalListener)
	                .build();
			TaskDefineRepo.setFileContentCache(cache);
	        // API
			AppContext.addApiServiceMapping(URL_INFO_FLOWS, SERVICE_INFO_FLOWS);
			AppContext.addApiServiceMapping(URL_INFO_FLOW, SERVICE_INFO_FLOW);
	        // Vertical
			AppContext.addVerticle(new VerticleProp(FsRepoWatchService::new, new DeploymentOptions().setInstances(1).setWorker(true)));
			AppContext.addVerticle(new VerticleProp(TaskFlowListService::new, new DeploymentOptions().setInstances(1).setWorker(true)));
			AppContext.addVerticle(new VerticleProp(TaskFlowDetailService::new, new DeploymentOptions().setInstances(1).setWorker(true)));

			promise.complete(config);
		});
	}

	@SuppressWarnings("rawtypes")
	public static Future<Void> deploy(Vertx vertx, JsonObject config) {
		return Future.future(promise -> {
			List<VerticleProp> descList = AppContext.getVerticleDescList();
			ArrayList<Future> futureList = new ArrayList<>(descList.size());
            descList.forEach(desc -> futureList.add(deployVerticle(vertx, desc.getVerticle(), desc.getOptions().setConfig(config))));
            CompositeFuture.all(futureList).onSuccess(ar -> promise.complete()).onFailure(promise::fail);
        });
	}
	
	public static Future<Void> deployVerticle(Vertx vertx, Supplier<Verticle> verticleSupplier, DeploymentOptions options) {
        return Future.future(promise -> 
            vertx.deployVerticle(verticleSupplier, options, ar -> {
                if (ar.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(ar.cause());
                }
            })
        );
    }
}
