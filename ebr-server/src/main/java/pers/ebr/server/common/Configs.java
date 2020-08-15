/*
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
package pers.ebr.server.common;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * <p>
 * EBR的配置工具
 * </p>
 *
 * @author l.gong
 */
public final class Configs {

    private final static Logger logger = LoggerFactory.getLogger(Configs.class);

    private final static String CONFIG_EXT = "extConfig";
    private final static String CONFIG_INNER = "innerConfig";

    public final static String KEY_REPO_MODE = "repository.mode";
    public final static String KEY_EXECUTOR_MIN = "executor.min";
    public final static String KEY_EXECUTOR_MAX = "executor.max";
    public final static String KEY_HTTP_PORT = "http.port";

    private final JsonObject config = new JsonObject();

    private static class InstanceHolder {
        private final static Configs INSTANCE = new Configs();
    }

    private Configs() {}

    /**
     * 读取配置文件
     * @throws IOException 读取配置文件失败时
     * @throws URISyntaxException 获取配置文件URI资源失败时
     */
    public static void load() throws IOException, URISyntaxException {
        HashMap<String, JsonObject> map = new HashMap<>(2);
        InstanceHolder.INSTANCE.loadConfigFile(map);
        InstanceHolder.INSTANCE.mergeConfig(map);
        InstanceHolder.INSTANCE.validateConfig();
        logger.info("the config info -> {}", InstanceHolder.INSTANCE.config.encode());
        System.out.println("Load Configuration ........... DONE");
        map.clear();
    }

    /**
     * 取得一份配置文件的拷贝
     * @return JsonObject
     */
    public static JsonObject get() {
        return InstanceHolder.INSTANCE.config.copy();
    }

    /**
     * 读取配置文件
     * @param holder 保存内部配置文件的集合
     * @throws IOException 读取配置文件失败时
     * @throws URISyntaxException 获取配置文件URI资源失败时
     */
    private void loadConfigFile(Map<String, JsonObject> holder) throws IOException, URISyntaxException {
        String configFile = String.format("%s%s%s", Paths.getConfPath(), File.separator, "ebr-server.json");
        File cfgFile = new File(configFile);
        if (cfgFile.exists() && cfgFile.isFile() && cfgFile.canRead()) {
            holder.put(CONFIG_EXT, new JsonObject(Files.readString(java.nio.file.Paths.get(configFile))));
        }
        URI innerConfigFile = getClass().getResource("/default.config.json").toURI();
        holder.put(CONFIG_INNER, new JsonObject(Files.readString(java.nio.file.Paths.get(innerConfigFile))));
    }

    /**
     * 合并外部和默认的配置文件内容
     * @param holder 保存内部配置文件的集合
     */
    private void mergeConfig(Map<String, JsonObject> holder) {
        JsonObject extObj = holder.getOrDefault(CONFIG_EXT, null);
        JsonObject innerObj = holder.get(CONFIG_INNER);
        if (extObj == null) {
            config.mergeIn(holder.get(CONFIG_INNER).copy());
            return;
        }
        // base info
        config.put(KEY_REPO_MODE, extObj.getString(KEY_REPO_MODE, innerObj.getString(KEY_REPO_MODE)).strip());
        config.put(KEY_EXECUTOR_MIN, extObj.getInteger(KEY_EXECUTOR_MIN, innerObj.getInteger(KEY_EXECUTOR_MIN)));
        config.put(KEY_EXECUTOR_MAX, extObj.getInteger(KEY_EXECUTOR_MAX, innerObj.getInteger(KEY_EXECUTOR_MAX)));
        // http info
        config.put(KEY_HTTP_PORT, extObj.getInteger(KEY_HTTP_PORT, innerObj.getInteger(KEY_HTTP_PORT)));
    }

    /**
     * 验证合并后配置文件的合法性
     * @throws InvalidPropertiesFormatException 配置冲突或非法值时
     */
    private void validateConfig() throws InvalidPropertiesFormatException {
        // base info
        String value = config.getString(KEY_REPO_MODE, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_REPO_MODE));
        }
        Integer intValue = config.getInteger(KEY_EXECUTOR_MIN, 0);
        if (intValue.compareTo(0) <= 0) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_EXECUTOR_MIN));
        }
        intValue = config.getInteger(KEY_EXECUTOR_MAX, 0);
        if (intValue.compareTo(0) <= 0) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_EXECUTOR_MAX));
        }
        // http info
        int port = config.getInteger(KEY_HTTP_PORT, 0);
        if (port <= 0) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] must be a positive number...", KEY_HTTP_PORT));
        }

    }

}