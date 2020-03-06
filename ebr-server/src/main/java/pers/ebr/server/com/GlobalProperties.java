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
package pers.ebr.server.com;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.com.GlobalConstants.*;

/**
 * <pre>
 * The utility of EBR-Server's configurations.
 * </pre>
 *
 * @author l.gong
 */
public final class GlobalProperties {

    private final static Logger logger = LoggerFactory.getLogger(GlobalProperties.class);

    private final static String CONFIG_EXT = "extConfig";
    private final static String CONFIG_DEF = "defConfig";

    private final JsonObject config;

    private static class InstanceHolder {
        private final static GlobalProperties INSTANCE = new GlobalProperties();
    }

    private GlobalProperties() {
        config = new JsonObject();
    }

    public static void load() throws Exception {
        HashMap<String, JsonObject> map = new HashMap<>(2);
        InstanceHolder.INSTANCE.loadConfigFile(map);
        InstanceHolder.INSTANCE.mergeConfig(map);
        InstanceHolder.INSTANCE.validateConfig();
        logger.info("the config info -> {}", InstanceHolder.INSTANCE.config.encode());
        System.out.println("Load Configuration ........... DONE");
    }

    public static JsonObject getHttpConfig() {
        return InstanceHolder.INSTANCE.config.getJsonObject(CONFIG_KEY_HTTP);
    }

    private void loadConfigFile(Map<String, JsonObject> holder) throws IOException, URISyntaxException {
        String configFile = String.format("%s%s%s", PathUtils.getConfPath(), File.separator, "ebr-server.json");
        File cfgFile = new File(configFile);
        if (cfgFile.exists() && cfgFile.isFile() && cfgFile.canRead()) {
            holder.put(CONFIG_EXT, new JsonObject(Files.readString(Paths.get(configFile))));
        }
        URI innerConfigFile = getClass().getResource("/default.config.json").toURI();
        holder.put(CONFIG_DEF, new JsonObject(Files.readString(Paths.get(innerConfigFile))));
    }

    private void mergeConfig(Map<String, JsonObject> holder) {
        JsonObject extObj = holder.getOrDefault(CONFIG_EXT, null);
        JsonObject defObj = holder.get(CONFIG_DEF);
        if (extObj == null || extObj.getJsonObject(CONFIG_KEY_NODE, null) == null) {
            config.mergeIn(holder.get(CONFIG_DEF).getJsonObject(CONFIG_KEY_NODE).copy());
            return;
        }
        // basic info
        config.put(CONFIG_KEY_ID, extObj.getString(CONFIG_KEY_ID, defObj.getString(CONFIG_KEY_ID)).strip());
        config.put(CONFIG_KEY_TYPE, extObj.getString(CONFIG_KEY_TYPE, defObj.getString(CONFIG_KEY_TYPE)).strip());
        // persist info
        config.mergeIn(extObj.getJsonObject(CONFIG_KEY_PERSIST_DB, defObj.getJsonObject(CONFIG_KEY_PERSIST_DB)).copy());
        // pool info
        config.mergeIn(extObj.getJsonObject(CONFIG_KEY_EXECUTABLE_POOL, defObj.getJsonObject(CONFIG_KEY_EXECUTABLE_POOL)).copy());
        // http info
        config.mergeIn(extObj.getJsonObject(CONFIG_KEY_HTTP, defObj.getJsonObject(CONFIG_KEY_HTTP)).copy());
    }

    private void validateConfig() throws InvalidPropertiesFormatException {
        // basic info
        String value = config.getString(CONFIG_KEY_ID, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] can not be empty...", CONFIG_KEY_ID));
        }
        value = config.getString(CONFIG_KEY_TYPE, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] can not be empty...", CONFIG_KEY_TYPE));
        }
        // persist info
        JsonObject L1JsonObj = config.getJsonObject(CONFIG_KEY_PERSIST_DB, null);
        if (L1JsonObj == null) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] can not be empty...", CONFIG_KEY_PERSIST_DB));
        }
        value = L1JsonObj.getString(CONFIG_KEY_TYPE, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s.%s] can not be empty...", CONFIG_KEY_PERSIST_DB, CONFIG_KEY_TYPE));
        }
        L1JsonObj = config.getJsonObject(CONFIG_KEY_EXECUTABLE_POOL, null);
        if (L1JsonObj == null) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] can not be empty...", CONFIG_KEY_EXECUTABLE_POOL));
        }
        value = L1JsonObj.getString(CONFIG_KEY_TYPE, "").strip();
        if (value.isEmpty()) {
            throw new InvalidPropertiesFormatException(String.format("property [%s.%s] can not be empty...", CONFIG_KEY_EXECUTABLE_POOL, CONFIG_KEY_TYPE));
        }
        // http info
        L1JsonObj = config.getJsonObject(CONFIG_KEY_HTTP, null);
        if (L1JsonObj == null) {
            throw new InvalidPropertiesFormatException(String.format("property [%s] can not be empty...", CONFIG_KEY_HTTP));
        }
        int port = L1JsonObj.getInteger(CONFIG_KEY_PORT, 0);
        if (port <= 0) {
            throw new InvalidPropertiesFormatException(String.format("property [%s.%s] must be a positive number...", CONFIG_KEY_HTTP, CONFIG_KEY_PORT));
        }
    }

}