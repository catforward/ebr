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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.isNull;
import static pers.ebr.base.AppConsts.*;

/**
 * <pre>
 * App's path utility
 *
 * ${EBR_ROOT}/
 *     |-- bin/
 *     |     |-- xxx.sh
 *     |-- conf/
 *     |     |-- config.json
 *     |-- libs/
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
    private static final String PATH_FORMAT = "%s%s%s";

    private final String rootPath;
    private final String confPath;
    private final String logsPath;
    private final String dataPath;
    private final String binPath;

    private static class InstanceHolder {
        private static final AppPaths INSTANCE = new AppPaths();
    }

    private AppPaths() {
        rootPath = initRootPath();
        confPath = String.format(PATH_FORMAT, rootPath, File.separator, CONF);
        logsPath = String.format(PATH_FORMAT, rootPath, File.separator, LOGS);
        dataPath = String.format(PATH_FORMAT, rootPath, File.separator, DATA);
        binPath = String.format(PATH_FORMAT, rootPath, File.separator, BIN);
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

    public static String getConfPath() {
        return InstanceHolder.INSTANCE.confPath;
    }

    public static String getDataPath() {
        return InstanceHolder.INSTANCE.dataPath;
    }

    public static String getLogsPath() {
        return InstanceHolder.INSTANCE.logsPath;
    }

    public static String getRootPath() {
        return InstanceHolder.INSTANCE.rootPath;
    }

    public static String getBinPath() {
        return InstanceHolder.INSTANCE.binPath;
    }

}
