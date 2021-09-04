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
package pers.ebr.data;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.AppConsts;
import pers.ebr.base.AppException;
import pers.ebr.base.AppPaths;
import pers.ebr.base.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * <pre>Folder's loader/cache</pre>
 *
 * @author l.gong
 */
public class TaskDefineRepo {
    private static final Logger logger = LoggerFactory.getLogger(TaskDefineRepo.class);
    private final ReentrantLock poolLock;
    /** key: flow_url, value: prop class*/
    private final Map<String, TaskDefineFileProp> defineFileInfo;
    /** key: flow_url, value: file content(JsonObject)*/
    private Cache<String, JsonObject> defineFileContentCache;

    public static final RemovalListener<String, JsonObject> removalListener = notification -> {
        logger.debug("define file content cache: Key {} was removed ({})",
                notification.getKey(), notification.getCause());
        clearDefineContent(notification.getKey());
    };

    private static class InstanceHolder {
        private static final TaskDefineRepo INSTANCE = new TaskDefineRepo();
    }

    private TaskDefineRepo() {
        poolLock = new ReentrantLock();
        defineFileInfo = new ConcurrentHashMap<>();
    }

    public static void release() {
        InstanceHolder.INSTANCE.defineFileContentCache.invalidateAll();
        InstanceHolder.INSTANCE.defineFileInfo.clear();
    }

    public static void reload() throws IOException {
        List<TaskDefineFileProp> files = new ArrayList<>();
        // scan data folder
        Path dataStorePath = new File(AppPaths.getDataPath()).toPath();
        Files.walkFileTree(dataStorePath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                File file = filePath.toFile();
                if (file.canRead() && file.getName().endsWith(AppConsts.FLOW_FILE_SUFFIX)) {
                    TaskDefineFileProp prop = new TaskDefineFileProp();
                    prop.setAbsolutePath(file.getAbsolutePath());
                    prop.setFlowUrl(StringUtils.toFlowUrl(dataStorePath, filePath));
                    prop.setFileSize(attrs.size());
                    prop.setLastModifiedTime(file.lastModified());
                    files.add(prop);
                }
                return super.visitFile(filePath, attrs);
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                logger.error("visit file failed.", exc);
                return FileVisitResult.CONTINUE;
            }
        });
        // load file content
        if (files.isEmpty()) {
            release();
            return;
        }

        Map<String, TaskDefineFileProp> copyMap =  copyDefineFileInfo();
        files.forEach(prop -> {
            if (prop.isNewerThan(copyMap.remove(prop.getFlowUrl()))) {
                addDefineFileProp(prop);
            }
        });
        copyMap.forEach((k, v) -> deleteDefineFileProp(v));
        copyMap.clear();
    }

    /**
     * Give a cache to store the task define content
     *
     * @param cache Cache object
     */
    public static void setFileContentCache(Cache<String, JsonObject> cache) {
        requireNonNull(cache);
        synchronized(InstanceHolder.INSTANCE) {
            if (!isNull(InstanceHolder.INSTANCE.defineFileContentCache)) {
                InstanceHolder.INSTANCE.defineFileContentCache.invalidateAll();
            }
            InstanceHolder.INSTANCE.defineFileContentCache = cache;
        }
    }

    /**
     * Add a prop object to memory store
     *
     * @param prop property object of task define file
     */
    private static void addDefineFileProp(TaskDefineFileProp prop) {
        requireNonNull(prop);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.defineFileInfo.put(prop.getFlowUrl(), prop);
            JsonObject obj = new JsonObject(Files.readString(Paths.get(prop.getAbsolutePath())));
            InstanceHolder.INSTANCE.defineFileContentCache.invalidate(prop.getFlowUrl());
            InstanceHolder.INSTANCE.defineFileContentCache.put(prop.getFlowUrl(), obj);
        } catch (IOException ex) {
            logger.error("can not read from [{}], caching skipped...", prop.getAbsolutePath(), ex);
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    private static void clearDefineContent(String flowUrl) {
        requireNonNull(flowUrl);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            TaskDefineFileProp prop = InstanceHolder.INSTANCE.defineFileInfo.get(flowUrl);
            if (isNull(prop)) {
                logger.debug("can not clear define content. url:[{}]", flowUrl);
                return;
            }
            prop.setContent(null);
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    private static void deleteDefineFileProp(TaskDefineFileProp prop) {
        requireNonNull(prop);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            InstanceHolder.INSTANCE.defineFileInfo.remove(prop.getFlowUrl());
            InstanceHolder.INSTANCE.defineFileContentCache.invalidate(prop.getFlowUrl());
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

    static Map<String, TaskDefineFileProp> copyDefineFileInfo() {
        return new HashMap<>(InstanceHolder.INSTANCE.defineFileInfo);
    }

    static TaskDefineFileProp getDefineFileInfo(String flowUrl) throws ExecutionException {
        requireNonNull(flowUrl);
        InstanceHolder.INSTANCE.poolLock.lock();
        try {
            TaskDefineFileProp prop = InstanceHolder.INSTANCE.defineFileInfo.get(flowUrl);
            if (isNull(prop)) {
                throw new AppException(String.format("task[%s] is not exist.", flowUrl));
            }
            JsonObject content = InstanceHolder.INSTANCE.defineFileContentCache.get(flowUrl, () ->
                    new JsonObject(Files.readString(Paths.get(prop.getAbsolutePath()))));
            if (isNull(content)) {
                throw new AppException(String.format("task[%s] is not exist.(path:[%s])", flowUrl, prop.getAbsolutePath()));
            }
            prop.setContent(content);
            return prop;
        } finally {
            InstanceHolder.INSTANCE.poolLock.unlock();
        }
    }

}
