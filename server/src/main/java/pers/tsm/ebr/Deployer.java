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
package pers.tsm.ebr;

import static pers.tsm.ebr.common.ServiceSymbols.SERVICE_INFO_FLOW;
import static pers.tsm.ebr.common.ServiceSymbols.SERVICE_INFO_FLOWS;
import static pers.tsm.ebr.common.ServiceSymbols.SERVICE_SCHD_ACTION;
import static pers.tsm.ebr.common.ServiceSymbols.URL_INFO_FLOW;
import static pers.tsm.ebr.common.ServiceSymbols.URL_INFO_FLOWS;
import static pers.tsm.ebr.common.ServiceSymbols.URL_SCHD_ACTION;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppConfigs;
import pers.tsm.ebr.common.AppContext;
import pers.tsm.ebr.data.Flow;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.data.TaskRepo;
import pers.tsm.ebr.data.VerticleProp;
import pers.tsm.ebr.schd.TaskExecVerticle;
import pers.tsm.ebr.schd.TaskSchdVerticle;
import pers.tsm.ebr.service.FsRepoWatchService;
import pers.tsm.ebr.service.TaskInfoDetailService;
import pers.tsm.ebr.service.TaskInfoListService;
import pers.tsm.ebr.service.TaskSchdActionService;

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
            Cache<String, JsonObject> fileContentCache = CacheBuilder.newBuilder()
                    .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                    .initialCapacity(config.getInteger(AppConfigs.SERVICE_FS_DATA_CACHE_INITIAL_CAPACITY, 1))
                    .maximumSize(config.getInteger(AppConfigs.SERVICE_FS_DATA_CACHE_MAXIMUM_SIZE, 10))
                    .expireAfterWrite(config.getInteger(AppConfigs.SERVICE_FS_DATA_CACHE_EXPIRE_SECONDS, 300), TimeUnit.SECONDS)
                    .removalListener(TaskDefineRepo.removalListener)
                    .build();
            TaskDefineRepo.setFileContentCache(fileContentCache);
            Cache<String, Flow> taskCache = CacheBuilder.newBuilder()
                    .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                    .initialCapacity(config.getInteger(AppConfigs.SERVICE_TASK_CACHE_INITIAL_CAPACITY, 1))
                    .maximumSize(config.getInteger(AppConfigs.SERVICE_TASK_CACHE_MAXIMUM_SIZE, 20))
                    .expireAfterWrite(config.getInteger(AppConfigs.SERVICE_TASK_CACHE_EXPIRE_SECONDS, 600), TimeUnit.SECONDS)
                    .build();
            TaskRepo.setIdleFlowPoolCache(taskCache);
            // API
            AppContext.addApiServiceMapping(URL_INFO_FLOWS, SERVICE_INFO_FLOWS);
            AppContext.addApiServiceMapping(URL_INFO_FLOW, SERVICE_INFO_FLOW);
            AppContext.addApiServiceMapping(URL_SCHD_ACTION, SERVICE_SCHD_ACTION);
            // Vertical
            AppContext.addVerticle(new VerticleProp(FsRepoWatchService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskInfoListService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskInfoDetailService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskSchdActionService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskSchdVerticle::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskExecVerticle::new, makeDefaultWorkerOptions(1, config)));

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

    private static DeploymentOptions makeDefaultWorkerOptions(int instances, JsonObject config) {
        return new DeploymentOptions().setInstances(instances).setWorker(true).setConfig(config);
    }
}
