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
package pers.ebr.server.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * The utility of EBR-Server's configurations.
 * </pre>
 *
 * @author l.gong
 */
public final class Configurations {

    private final static Logger logger = LoggerFactory.getLogger(Configurations.class);

    public final static String NODE_NAME = "node.name";
    public final static String NODE_MODE = "node.mode";
    public final static String NODE_ROLE = "node.role";
    public final static String NODE_HTTP_HOST = "node.http.host";
    public final static String NODE_HTTP_PORT = "node.http.port";
    public final static String NODE_STORE_SQLITE_NAME = "node.store.sqlite.name";
    public final static String NODE_STORE_SQLITE_LOCAL = "node.store.sqlite.local";
    public final static String NODE_STORE_TASKS_NAME = "node.store.tasks.name";
    public final static String NODE_STORE_TASKS_LOCAL = "node.store.tasks.local";

    public final static String DEFAULT_PROP_FILE = "ebr.properties";
    public final static String DEFAULT_VALUE = "nothing";

    private static class ConfigHolder {
        private final static Configurations INSTANCE = new Configurations();
    }

    private final Properties prop = new Properties();

    private Configurations() {
    }

    public static void load() {
        String defaultFile = String.format("%s%s%s", Paths.getConfPath(), File.separator, DEFAULT_PROP_FILE);
        if (ConfigHolder.INSTANCE.loadFromFile(defaultFile)) {
            System.out.println("Load Configuration ........... OK");
        }
    }

    public static String get(String key) {
        return ConfigHolder.INSTANCE.prop.getProperty(key, DEFAULT_VALUE);
    }

    public static void dumpAllProperties() {
        Enumeration<?> en = ConfigHolder.INSTANCE.prop.propertyNames();
        while (en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = Configurations.get(strKey);
            logger.debug("config => {}:{}", strKey, strValue);
        }
    }

    boolean loadFromFile(String filePath) {
        boolean ret = true;
        try {
            InputStream in = new BufferedInputStream (new FileInputStream(filePath));
            logger.info("will load config file:{}", filePath);
            prop.load(in);
        } catch (IOException ex) {
            ret = false;
            logger.error("Failed to load configurations from : {}", filePath, ex);
        }
        return ret;
    }

}