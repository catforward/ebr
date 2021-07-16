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
package pers.tsm.ebr.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

import static java.util.Objects.requireNonNull;

/**
 *
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
    public static final String APP_ALLOW_COMMAND_PATH = "allowCommandPath";
    public static final String HTTP_ADDRESS = "address";
    public static final String HTTP_PORT = "port";
    public static final String SERVICE_TIMEZONE = "timezone";
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

    private static final String CONFIG_FILE = "config.json";

    private final JsonObject config = new JsonObject();
    private boolean isDevMode = false;

    private static class InstanceHolder {
        private static final AppConfigs INSTANCE = new AppConfigs();
    }

    private AppConfigs() {}

    /**
     * load configuration from config.json
     */
    public static void load() throws IOException {
        InstanceHolder.INSTANCE.loadConfigFile();
        InstanceHolder.INSTANCE.loadExternalConfigFile();
        if (InstanceHolder.INSTANCE.config.isEmpty()) {
            throw new AppException("config.json --> empty");
        }
        InstanceHolder.INSTANCE.isDevMode = InstanceHolder.INSTANCE.config
                .getJsonObject(SECTION_APP).getBoolean(APP_DEV_MODE, false);
        logger.debug("config info -> {}", InstanceHolder.INSTANCE.config.encodePrettily());
        logger.info("Load Configuration ........... DONE");
    }

    /**
     * clear all configuration
     */
    public static void release() {
        if (!InstanceHolder.INSTANCE.config.isEmpty()) {
            InstanceHolder.INSTANCE.config.clear();
        }
    }

    /**
     * get a configuration copy
     * @return JsonObject
     */
    public static JsonObject get() {
        return InstanceHolder.INSTANCE.config.copy();
    }

    public static boolean isDevMode() {
        return InstanceHolder.INSTANCE.isDevMode;
    }

    /**
     * load configuration from default config.json
     */
    private void loadConfigFile() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            requireNonNull(stream);
            byte[] b = new byte[1024];
            int len = 0;
            int temp = 0;
            while ((temp = stream.read()) != -1) {
                b[len] = (byte) temp;
                len++;
            }
            JsonObject tmpConfig = new JsonObject(new String(b, 0, len));
            config.mergeIn(tmpConfig);
        }
    }

    /**
     * load configuration from user defined config.json
     */
    private void loadExternalConfigFile() throws IOException {
        String extConfFile = String.format("%s%s%s", AppPaths.getConfPath(), File.separator, CONFIG_FILE);
        URI confStorePath = new File(extConfFile).toURI();
        JsonObject tmpConfig = new JsonObject(Files.readString(Paths.get(confStorePath)));
        config.mergeIn(tmpConfig);
    }

}
