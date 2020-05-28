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

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.common.Const.ENV_EBR_ROOT;

/**
 * <pre>
 * The utility of EBR-Server's path.
 * Made the path info with the folder structure below this:
 *
 * ${EBR_ROOT}/
 *     |-- bin/
 *     |     |-- ebr-server.sh
 *     |-- conf/
 *     |     |-- xxx.json
 *     |-- lib/
 *     |     |-- ebr-server.jar(this jar)
 *     |-- logs/
 *     |     |-- ebr-server.log
 *     |== data/
 *     |     |-- xxx.dat
 * </pre>
 *
 * @author l.gong
 */
public final class Paths {

    private final static Logger logger = LoggerFactory.getLogger(Paths.class);

    private final String rootPath;
    private final String logsPath;
    private final String confPath;
    private final String dataPath;

    private static class PathHolder {
        private final static Paths INSTANCE = new Paths();
    }

    private Paths() {
        rootPath = initRootPath();
        logsPath = String.format("%s%s%s", rootPath, File.separator, "logs");
        confPath = String.format("%s%s%s", rootPath, File.separator, "conf");
        dataPath = String.format("%s%s%s", rootPath, File.separator, "data");
    }

    private String initRootPath() {
        String rawPath = "";
        try {
            rawPath = System.getenv(ENV_EBR_ROOT);
        } catch (Exception ex) {
            logger.debug("Failed to get the environment parameter: {}", ENV_EBR_ROOT, ex);
        }
        if (rawPath == null || rawPath.isBlank()) {
            rawPath = URLDecoder.decode(Paths.class.getProtectionDomain()
                                .getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
            File jarFile = new File(rawPath);
            rawPath = URLDecoder.decode(jarFile.getParentFile()
                                .getParentFile().getAbsolutePath(), StandardCharsets.UTF_8);
        }
        return rawPath;
    }

    /**
     * <pre>
     * get the absolute path of "data" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getDataPath() {
        return PathHolder.INSTANCE.dataPath;
    }

    /**
     * <pre>
     * get the absolute path of "logs" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getLogsPath() {
        return PathHolder.INSTANCE.logsPath;
    }

    /**
     * <pre>
     * get the absolute path of "conf" folder
     * </pre>
     *
     * @return String The path
     */
    public static String getConfPath() {
        return PathHolder.INSTANCE.confPath;
    }

    /**
     * <pre>
     * get the root deploy path of EBR-Server
     * </pre>
     *
     * @return String The path
     */
    public static String getRootPath() {
        return PathHolder.INSTANCE.rootPath;
    }

}