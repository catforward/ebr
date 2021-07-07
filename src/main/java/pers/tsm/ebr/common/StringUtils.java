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

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 *
 * @author l.gong
 */
public class StringUtils {
    private static final DateTimeFormatter yyyyMMddHHmmssFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StringUtils() {}

    public static String toDatetimeStr(long time, ZoneId zone) {
        LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
        return yyyyMMddHHmmssFormatter.format(datetime);
    }

    public static String toFlowUrl(Path dataStorePath, Path filePath) {
        String flowUrl = filePath.toUri().toString().replaceAll(dataStorePath.toUri().toString(), "");
        flowUrl = flowUrl.replaceAll(Symbols.FLOW_FILE_SUFFIX, "");
        return (flowUrl.startsWith("/")) ? flowUrl : "/" + flowUrl;
    }

}
