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
import pers.tsm.ebr.common.VerticleProp;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.service.FsDataWatchService;
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
					// 设置并发级别为CPU核心数
	                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
	                //设置缓存容器的初始容量
	                .initialCapacity(config.getInteger("fsDataCacheInitialCapacity", 10))
	                //设置缓存最大容量，超过后就会按照LRU最近虽少使用算法来移除缓存项
	                .maximumSize(config.getInteger("fsDataCacheMaximumSize", 100))
	                //是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
	                //.recordStats()
	                //设置写缓存后n秒钟过期
	                //.expireAfterWrite(config.getInteger("fsDataCacheExpireSeconds", 300), TimeUnit.SECONDS)
	                //设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
	                .expireAfterAccess(config.getInteger("fsDataCacheExpireSeconds", 300), TimeUnit.SECONDS)
	                //设置缓存的移除通知
	                .removalListener(TaskDefineRepo.removalListener)
	                .build();
			TaskDefineRepo.setFileContentCache(cache);
	        // API
			AppContext.addApiServiceMapping(URL_INFO_FLOWS, SERVICE_INFO_FLOWS);
			AppContext.addApiServiceMapping(URL_INFO_FLOW, SERVICE_INFO_FLOW);
	        // Vertical
			AppContext.addVerticle(FsDataWatchService::new, new DeploymentOptions().setInstances(1).setWorker(true));
			AppContext.addVerticle(TaskFlowListService::new, new DeploymentOptions().setInstances(1).setWorker(true));
			AppContext.addVerticle(TaskFlowDetailService::new, new DeploymentOptions().setInstances(1).setWorker(true));
			// complete
			promise.complete(config);
		});
	}

	@SuppressWarnings("rawtypes")
	public static Future<Void> deploy(Vertx vertx, JsonObject config) {
		return Future.future(promise -> {
			List<VerticleProp> descList = AppContext.getVerticleDescList();
			ArrayList<Future> futureList = new ArrayList<>(descList.size());
            descList.forEach(desc -> 
            	futureList.add(deployVerticle(vertx, desc.getVerticle(), desc.getOptions().setConfig(config)))
            );
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
