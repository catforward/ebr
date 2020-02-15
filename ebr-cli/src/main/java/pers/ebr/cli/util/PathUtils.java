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
package pers.ebr.cli.util;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * 路径工具类，基于以下目录结构获取相关路径
 *
 * ${EbrRoot}/
 *     |-- ebr_cli.jar(this jar)
 * </pre>
 *
 * @author l.gong
 */
public final class PathUtils {

    private PathUtils() {}

    /**
     * 部署根路径
     */
    private static String rootPath;

    /**
     * <pre>
     * 取得EBR的部署路径
     * </pre>
     *
     * @return String 部署路径
     */
    public static String getRootPath() {

        if (rootPath != null) {
            return rootPath;
        }

        File jarFile = new File(getJarPath());
        rootPath = URLDecoder.decode(jarFile.getParentFile().getAbsolutePath(), StandardCharsets.UTF_8);
        return rootPath;
    }

    /**
     * <pre>
     * 取得EBR的JAR文件部署路径
     * </pre>
     *
     * @return String 部署路径
     */
    private static String getJarPath() {
        return URLDecoder.decode(PathUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
    }
}
