/**
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
package pers.tsm.ebr.common;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static pers.tsm.ebr.common.Symbols.*;

/**
 * <pre>
 * The path utility.
 * Made the path info with the folder structure below this:
 *
 * ${EBR_ROOT}/
 *     |-- bin/
 *     |     |-- ebr.sh
 *     |-- conf/
 *     |     |-- config.json
 *     |-- lib/
 *     |     |-- ebr.jar
 *     |-- logs/
 *     |     |-- ebr.log
 *     |-- data/
 *     |     |-- xxx.json
 * </pre>
 *
 * @author l.gong
 */
public final class AppPaths {
    private static final Logger logger = LoggerFactory.getLogger(AppPaths.class);
    private static final String PATH_FROMAT = "%s%s%s";

    private final String rootPath;
    private final String confPath;
    private final String logsPath;
    private final String dataPath;

    private static class InstanceHolder {
        private static final AppPaths INSTANCE = new AppPaths();
    }

    private AppPaths() {
        rootPath = initRootPath();
        confPath = String.format(PATH_FROMAT, rootPath, File.separator, CONF);
        logsPath = String.format(PATH_FROMAT, rootPath, File.separator, LOGS);
        dataPath = String.format(PATH_FROMAT, rootPath, File.separator, DATA);
    }

    private String initRootPath() {
        String rawPath = null;
        try {
            rawPath = System.getenv(ENV_EBR_ROOT);
        } catch (Exception ex) {
            logger.debug("Failed to get the environment parameter: {}", ENV_EBR_ROOT, ex);
        }
        if (isNull(rawPath) || rawPath.isBlank()) {
            rawPath = URLDecoder.decode(AppPaths.class.getProtectionDomain()
                                .getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
            rawPath = URLDecoder.decode(new File(rawPath).getParentFile()
                                .getParentFile().getAbsolutePath(), StandardCharsets.UTF_8);
        }
        return rawPath;
    }

    /**
     * <pre>
     * get the absolute path of "conf" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getConfPath() {
        return InstanceHolder.INSTANCE.confPath;
    }

    /**
     * <pre>
     * get the absolute path of "data" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getDataPath() {
        return InstanceHolder.INSTANCE.dataPath;
    }

    /**
     * <pre>
     * get the absolute path of "logs" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getLogsPath() {
        return InstanceHolder.INSTANCE.logsPath;
    }

    /**
     * <pre>
     * get the root deploy path of EBR-Server
     * </pre>
     *
     * @return String The path
     */
    public static String getRootPath() {
        return InstanceHolder.INSTANCE.rootPath;
    }
}
