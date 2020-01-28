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
package pers.ebr.cli;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author l.gong
 */
public enum ConfigUtils {
    /** 单例 */
    CONFIG;

    /**
     * 配置文件中使用的键名
     */
    public static final String KEY_INSTANT_TASK = "ebr.instant.task";
    public static final String EBR_LOG_ENABLE = "ebr.log.enable";
    public static final String EBR_LOG_LOCAL = "ebr.log.local";

    /**
     * 属性类实例
     */
    private final Properties prop = new Properties();

    /**
     * <pre>
     * 属性类构造函数
     * </pre>
     */
    ConfigUtils() {
    }

    public static void merge(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            CONFIG.prop.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * <pre>
     * 获取属性
     * </pre>
     *
     * @param key 键名
     * @return 字符型值
     */
    public static String get(String key) {
        Optional<String> opt = Optional.ofNullable(CONFIG.prop.getProperty(key));
        return opt.orElse("");
    }

    /**
     * <pre>
     * 获取属性，当不存在指定属性时返回调用者给出的默认值
     * </pre>
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static Object getOrDefault(String key, Object defaultValue) {
        return CONFIG.prop.getOrDefault(key, defaultValue);
    }
}
