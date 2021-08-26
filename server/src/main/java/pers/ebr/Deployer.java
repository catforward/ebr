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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import pers.ebr.base.AppConfigs;
import pers.ebr.base.AppContext;
import pers.ebr.base.ServiceSymbols;
import pers.ebr.data.Flow;
import pers.ebr.data.TaskDefineRepo;
import pers.ebr.data.TaskRepo;
import pers.ebr.data.VerticleProp;
import pers.ebr.schd.ActionSchdVerticle;
import pers.ebr.schd.CronSchdVerticle;
import pers.ebr.schd.TaskExecVerticle;
import pers.ebr.service.FlowDetailService;
import pers.ebr.service.FlowListService;
import pers.ebr.service.FsRepoWatchVerticle;
import pers.ebr.service.TaskSchdActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <pre>App function config（Cache，API，Service...）</pre>
 *
 * @author l.gong
 */
public class Deployer {

    private Deployer() {}

    /**
     * Describe the function in this app
     *
     * @param config service's config in config.json
     * @return service's config
     */
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
            AppContext.addApiServiceMapping(ServiceSymbols.URL_INFO_FLOW_LIST, ServiceSymbols.SERVICE_INFO_FLOW_LIST);
            AppContext.addApiServiceMapping(ServiceSymbols.URL_INFO_FLOW_DETAIL, ServiceSymbols.SERVICE_INFO_FLOW_DETAIL);
            AppContext.addApiServiceMapping(ServiceSymbols.URL_SCHD_ACTION, ServiceSymbols.SERVICE_SCHD_ACTION);
            // Vertical
            AppContext.addVerticle(new VerticleProp(FsRepoWatchVerticle::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(FlowListService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(FlowDetailService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskSchdActionService::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(ActionSchdVerticle::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(TaskExecVerticle::new, makeDefaultWorkerOptions(1, config)));
            AppContext.addVerticle(new VerticleProp(CronSchdVerticle::new, makeDefaultWorkerOptions(1, config)));

            promise.complete(config);
        });
    }

    /**
     * Deploy all verticle in this app
     *
     * @param vertx instance of vertx
     * @param config service's config in config.json
     * @return void
     */
    @SuppressWarnings("rawtypes")
    public static Future<Void> deploy(Vertx vertx, JsonObject config) {
        return Future.future(promise -> {
            List<VerticleProp> descList = AppContext.getVerticleDescList();
            ArrayList<Future> futureList = new ArrayList<>(descList.size());
            descList.forEach(desc -> futureList.add(deployVerticle(vertx, desc.getVerticle(), desc.getOptions().setConfig(config))));
            CompositeFuture.all(futureList).onSuccess(ar -> promise.complete()).onFailure(promise::fail);
        });
    }

    private static Future<Void> deployVerticle(Vertx vertx, Supplier<Verticle> verticleSupplier, DeploymentOptions options) {
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
