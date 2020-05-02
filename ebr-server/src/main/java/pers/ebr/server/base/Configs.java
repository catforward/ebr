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
package pers.ebr.server.base;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * <pre>
 * The utility of EBR-Server's configurations.
 * </pre>
 *
 * @author l.gong
 */
public final class Configs {

    private final static Logger logger = LoggerFactory.getLogger(Configs.class);

    private final static String CONFIG_EXT = "extConfig";
    private final static String CONFIG_INNER = "innerConfig";

    public final static String KEY_NODE_ID = "node.id";
    public final static String KEY_NODE_TYPE = "node.type";
    public final static String KEY_REPO_DB = "repo.db";
    public final static String KEY_REPO_POOL = "repo.pool";
    public final static String KEY_HTTP_PORT = "http.port";
    public final static String KEY_HTTP_ACL = "http.acl";

    private final JsonObject config = new JsonObject();

    private static class InstanceHolder {
        private final static Configs INSTANCE = new Configs();
    }

    private Configs() {}

    public static void load() throws Exception {
        HashMap<String, JsonObject> map = new HashMap<>(2);
        InstanceHolder.INSTANCE.loadConfigFile(map);
        InstanceHolder.INSTANCE.mergeConfig(map);
        InstanceHolder.INSTANCE.validateConfig();
        logger.info("the config info -> {}", InstanceHolder.INSTANCE.config.encode());
        System.out.println("Load Configuration ........... DONE");
        map.clear();
    }

    public static JsonObject get() {
        return InstanceHolder.INSTANCE.config.copy();
    }

    private void loadConfigFile(Map<String, JsonObject> holder) throws IOException, URISyntaxException {
        String configFile = String.format("%s%s%s", Paths.getConfPath(), File.separator, "ebr-server.json");
        File cfgFile = new File(configFile);
        if (cfgFile.exists() && cfgFile.isFile() && cfgFile.canRead()) {
            holder.put(CONFIG_EXT, new JsonObject(Files.readString(java.nio.file.Paths.get(configFile))));
        }
        URI innerConfigFile = getClass().getResource("/default.config.json").toURI();
        holder.put(CONFIG_INNER, new JsonObject(Files.readString(java.nio.file.Paths.get(innerConfigFile))));
    }

    private void mergeConfig(Map<String, JsonObject> holder) {
        JsonObject extObj = holder.getOrDefault(CONFIG_EXT, null);
        JsonObject innerObj = holder.get(CONFIG_INNER);
        if (extObj == null) {
            config.mergeIn(holder.get(CONFIG_INNER).copy());
            return;
        }
        // basic info
        config.put(KEY_NODE_ID, extObj.getString(KEY_NODE_ID, innerObj.getString(KEY_NODE_ID)).strip());
        config.put(KEY_NODE_TYPE, extObj.getString(KEY_NODE_TYPE, innerObj.getString(KEY_NODE_TYPE)).strip());
        // base info
        config.put(KEY_REPO_DB, extObj.getString(KEY_REPO_DB, innerObj.getString(KEY_REPO_DB)).strip());

        // pool info
        config.put(KEY_REPO_POOL, extObj.getString(KEY_REPO_POOL, innerObj.getString(KEY_REPO_POOL)).strip());

        // http info
        config.put(KEY_HTTP_PORT, extObj.getInteger(KEY_HTTP_PORT, innerObj.getInteger(KEY_HTTP_PORT)));
        config.put(KEY_HTTP_ACL, extObj.getJsonArray(KEY_HTTP_ACL, innerObj.getJsonArray(KEY_HTTP_ACL)));
    }

    private void validateConfig() throws InvalidPropertiesFormatException {
        // basic info
        String value = config.getString(KEY_NODE_ID, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_NODE_ID));
        }
        value = config.getString(KEY_NODE_TYPE, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_NODE_TYPE));
        }
        // base info
        value = config.getString(KEY_REPO_DB, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_REPO_DB));
        }

        // executable pool
        value = config.getString(KEY_REPO_POOL, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] cannot be empty...", KEY_REPO_POOL));
        }

        // http info
        int port = config.getInteger(KEY_HTTP_PORT, 0);
        if (port <= 0) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] must be a positive number...", KEY_HTTP_PORT));
        }
        JsonArray acl = config.getJsonArray(KEY_HTTP_ACL, null);
        if (acl == null || acl.isEmpty()) {
            // do nothing
        }

    }

}