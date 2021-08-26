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
package pers.ebr.base;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;

import static java.util.Objects.requireNonNull;

/**
 * <pre>App's config</pre>
 *
 * @author l.gong
 */
public final class AppConfigs {
    private static final Logger logger = LoggerFactory.getLogger(AppConfigs.class);

    public static final String SECTION_APP = "app";
    public static final String SECTION_VERTX = "vertx";
    public static final String SECTION_HTTP = "http";
    public static final String SECTION_SERVICE = "service";

    public static final String APP_DEV_MODE = "devMode";
    public static final String APP_TIMEZONE = "timezone";
    public static final String APP_ALLOW_COMMAND_PATH = "allowCommandPath";
    public static final String HTTP_ADDRESS = "address";
    public static final String HTTP_PORT = "port";
    public static final String SERVICE_FS_DATA_CHECK_INTERVAL_SECONDS = "fsDataCheckIntervalSeconds";
    public static final String SERVICE_FS_DATA_CACHE_EXPIRE_SECONDS = "fsDataCacheExpireSeconds";
    public static final String SERVICE_FS_DATA_CACHE_INITIAL_CAPACITY = "fsDataCacheInitialCapacity";
    public static final String SERVICE_FS_DATA_CACHE_MAXIMUM_SIZE = "fsDataCacheMaximumSize";
    public static final String SERVICE_TASK_CACHE_EXPIRE_SECONDS = "taskCacheExpireSeconds";
    public static final String SERVICE_TASK_CACHE_INITIAL_CAPACITY = "taskCacheInitialCapacity";
    public static final String SERVICE_TASK_CACHE_MAXIMUM_SIZE = "taskCacheMaximumSize";
    public static final String SERVICE_TASK_EXECUTOR_MAXIMUM_SIZE = "taskExecutorMaximumSize";
    public static final String SERVICE_TASK_EXECUTOR_MINIMUM_SIZE = "taskExecutorMinimumSize";
    public static final String SERVICE_TASK_EXECUTOR_CHECK_INTERVAL_SECONDS = "taskExecutorCheckIntervalSeconds";
    public static final String SERVICE_CRON_SCHD_CHECK_INTERVAL_SECONDS = "cronSchdCheckIntervalSeconds";

    private static final String CONFIG_FILE = "config.json";
    private static final String DEF_ZONE = "Asia/Tokyo";

    private final JsonObject config = new JsonObject();
    private boolean isDevMode = false;
    private ZoneId zoneId;

    private static class InstanceHolder {
        private static final AppConfigs INSTANCE = new AppConfigs();
    }

    private AppConfigs() {}

    public static void load() throws IOException {
        InstanceHolder.INSTANCE.loadConfigFile();
        InstanceHolder.INSTANCE.loadExternalConfigFile();
        if (InstanceHolder.INSTANCE.config.isEmpty()) {
            throw new AppException("config.json --> empty");
        }
        InstanceHolder.INSTANCE.isDevMode = InstanceHolder.INSTANCE.config
                .getJsonObject(SECTION_APP).getBoolean(APP_DEV_MODE, false);
        String timezone = InstanceHolder.INSTANCE.config.getJsonObject(AppConfigs.SECTION_APP)
                .getString(AppConfigs.APP_TIMEZONE, DEF_ZONE);
        InstanceHolder.INSTANCE.zoneId = ZoneId.of(timezone);
        String prettyStr = InstanceHolder.INSTANCE.config.encodePrettily();
        logger.debug("config info -> {}", prettyStr);
        logger.info("Load Configuration ........... DONE");
    }

    public static void release() {
        if (!InstanceHolder.INSTANCE.config.isEmpty()) {
            InstanceHolder.INSTANCE.config.clear();
        }
    }

    public static JsonObject get() {
        return InstanceHolder.INSTANCE.config.copy();
    }

    public static boolean isDevMode() {
        return InstanceHolder.INSTANCE.isDevMode;
    }

    public static ZoneId getZoneId() {
        return InstanceHolder.INSTANCE.zoneId;
    }

    private void loadConfigFile() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            requireNonNull(stream);
            byte[] b = new byte[1024];
            int len = 0;
            int temp;
            while ((temp = stream.read()) != -1) {
                b[len] = (byte) temp;
                len++;
            }
            JsonObject tmpConfig = new JsonObject(new String(b, 0, len));
            config.mergeIn(tmpConfig);
        }
    }

    private void loadExternalConfigFile() throws IOException {
        String extConfFile = String.format("%s%s%s", AppPaths.getConfPath(), File.separator, CONFIG_FILE);
        URI confStorePath = new File(extConfFile).toURI();
        JsonObject tmpConfig = new JsonObject(Files.readString(Paths.get(confStorePath)));
        config.mergeIn(tmpConfig);
    }

}
