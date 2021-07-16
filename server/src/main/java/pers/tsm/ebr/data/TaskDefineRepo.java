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
package pers.tsm.ebr.data;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;

import io.vertx.core.json.JsonObject;
import pers.tsm.ebr.common.AppException;

/**
 *
 *
 * @author l.gong
 */
public class TaskDefineRepo {
    private static final Logger logger = LoggerFactory.getLogger(TaskDefineRepo.class);
    private final ReentrantLock poolLock;
    /** key: flow_url, value: prop class*/
    private final Map<String, TaskDefineFileProp> defineFileInfo;
    /** key: flow_url, value: file content(JsonObject)*/
    private Cache<String, JsonObject> defineFileContent;

    public static final RemovalListener<String, JsonObject> removalListener = notification -> {
        logger.debug("define file content cache: Key {} was removed ({})",
                notification.getKey(), notification.getCause());
    };

    private static class InstanceHolder {
        private static final TaskDefineRepo INSTANCE = new TaskDefineRepo();
    }

    private TaskDefineRepo() {
        poolLock = new ReentrantLock();
        defineFileInfo = new ConcurrentHashMap<>();
    }

    public static void release() {
        InstanceHolder.INSTANCE.defineFileContent.invalidateAll();
        InstanceHolder.INSTANCE.defineFileInfo.clear();
    }

    public static void dumpAll() {
        logger.info("############ define file ############");
        InstanceHolder.INSTANCE.defineFileInfo.forEach((k, v) -> logger.info(v.toString()));
        logger.info("############ define file's content ############");
        InstanceHolder.INSTANCE.defineFileContent.asMap().forEach((k, v) -> logger.info("{} = {}", k, v));
    }

    public static void setFileContentCache(Cache<String, JsonObject> cache) {
        requireNonNull(cache);
        synchronized(InstanceHolder.INSTANCE) {
            if (!isNull(InstanceHolder.INSTANCE.defineFileContent)) {
                InstanceHolder.INSTANCE.defineFileContent.invalidateAll();
            }
            InstanceHolder.INSTANCE.defineFileContent = cache;
        }
    }

    public static Map<String, TaskDefineFileProp> copyDefineFileInfo() {
        HashMap<String, TaskDefineFileProp> copyMap = new HashMap<>();
        copyMap.putAll(InstanceHolder.INSTANCE.defineFileInfo);
        return copyMap;
    }

    public static void addDefineFile(TaskDefineFileProp prop) {
        requireNonNull(prop);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.defineFileInfo.put(prop.getFlowUrl(), prop);
            JsonObject obj = new JsonObject(Files.readString(Paths.get(prop.getAbsolutePath())));
            InstanceHolder.INSTANCE.defineFileContent.invalidate(prop.getFlowUrl());
            InstanceHolder.INSTANCE.defineFileContent.put(prop.getFlowUrl(), obj);
        } catch (IOException ex) {
            logger.error("can not read from [{}], caching skipped...", prop.getAbsolutePath(), ex);
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    public static void deleteDefineFile(TaskDefineFileProp prop) {
        requireNonNull(prop);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.defineFileInfo.remove(prop.getFlowUrl());
            InstanceHolder.INSTANCE.defineFileContent.invalidate(prop.getFlowUrl());}
        finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    public static TaskDefineFileProp getDefineFileInfo(String flowUrl) {
        return InstanceHolder.INSTANCE.defineFileInfo.get(flowUrl);
    }

    public static JsonObject getDefineFileContent(String flowUrl) throws ExecutionException {
        TaskDefineFileProp prop = getDefineFileInfo(flowUrl);
        if (isNull(prop)) {
            throw new AppException(String.format("task[%s] is not exist.", flowUrl));
        }
        JsonObject content = InstanceHolder.INSTANCE.defineFileContent.get(flowUrl, new Callable<JsonObject>() {
            public JsonObject call() throws Exception {
                return new JsonObject(Files.readString(Paths.get(prop.getAbsolutePath())));
            }
        });
        if (isNull(content)) {
            throw new AppException(String.format("task[%s] is not exist.(path:[%s])", flowUrl, prop.getAbsolutePath()));
        }
        return content;
    }

}
