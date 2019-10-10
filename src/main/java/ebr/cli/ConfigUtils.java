/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.cli;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * <pre>
 * 预设置工具类，初始化EBR运行中所需的配置
 * 基于以下目录结构获取配置文件
 * ${EbrRoot}/
 *        |-- conf/
 *        |    |-- ebr.properties
 * </pre>
 *
 * @author catforward
 */
enum ConfigUtils {
    /** 单例 */
    CONFIG;

    /**
     * 配置文件中使用的键名
     */
    public static final String KEY_INSTANT_TASK = "ebr.instant.task";
    public static final String KEY_WORKER_NUM = "ebr.worker.number";

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
