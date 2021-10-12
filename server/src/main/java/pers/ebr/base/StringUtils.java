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

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;

/**
 * <pre>Misc string utility</pre>
 *
 * @author l.gong
 */
public class StringUtils {
    private static final DateTimeFormatter yyyyMMddHHmmssFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StringUtils() {}

    /**
     * Format a time to [yyyy-MM-dd HH:mm:ss]
     *
     * @param time target time
     * @param zone target zone
     * @return a formatted time string
     */
    public static String toDatetimeStr(long time, ZoneId zone) {
        LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
        return yyyyMMddHHmmssFormatter.format(datetime);
    }

    /**
     * Convert path name to Flow's Url <br/>
     * eg: <br/>
     * filename: /your_path/data/sub/flow-x.json <br/>
     * url: /sub/flow-x.json
     *
     * @param dataStorePath data store path instance
     * @param filePath json file's path instance
     * @return flow's url
     */
    public static String toFlowUrl(Path dataStorePath, Path filePath) {
        String flowUrl = filePath.toUri().toString().replaceAll(dataStorePath.toUri().toString(), "");
        flowUrl = flowUrl.replaceAll(AppSymbols.FLOW_FILE_SUFFIX, "");
        return (flowUrl.startsWith("/")) ? flowUrl : "/" + flowUrl;
    }

    /**
     * Validate the string instance
     *
     * @param str string instance
     * @return true: string instance is null or empty content
     */
    public static boolean isNullOrBlank(String str) {
    	return isNull(str) || str.isBlank();
    }

    /**
     * Convert the script/bin's path which define in json file
     *
     * case_1:<br/>
     * script tag: echo.sh <br/>
     * result: /your_path/bin/echo.sh <br/>
     *
     * case_2:<br/>
     * script tag: /your_path/bin/echo.sh <br/>
     * result: /your_path/bin/echo.sh <br/>
     *
     * @param defineScript external script/bin's path in json file
     * @return converted path
     */
    public static String warpIfEmbedScriptPath(String defineScript) {
        if (isNullOrBlank(defineScript)) {
            return AppSymbols.BLANK_STR;
        }
        if (!new File(defineScript).isAbsolute()) {
            return String.format("%s%s%s", AppPaths.getBinPath(), File.separator, defineScript);
        }
        return defineScript;
    }

}
