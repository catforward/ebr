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
package ebr.core.util;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * 路径工具类，基于以下目录结构获取相关路径
 *
 * ${EbrRoot}/
 *     |-- bin/
 *     |     |-- ebr_xxx.sh
 *     |-- def/
 *     |     |-- tasks_win.xml
 *     |     |-- tasks_linux.xml
 *     |-- lib/
 *     |     |-- ebr.jar(this jar)
 *     |-- logs/
 *     |     |-- ebr-dev.log
 * </pre>
 *
 * @author catforward
 */
public final class PathUtils {

    private PathUtils() {}

    /**
     * 部署根路径
     */
    private static String rootPath;

    /**
     * <pre>
     * 得到EBR的log部署目录
     * </pre>
     *
     * @return String 部署路径
     */
    public static String getLogPath() {
        return getRootPath() + File.separator + "logs";
    }

    /**
     * <pre>
     * 得到EBR的def部署目录
     * </pre>
     *
     * @return String 部署路径
     */
    public static String getDefPath() {
        return getRootPath() + File.separator + "def";
    }

    /**
     * <pre>
     * 取得EBR的部署路径
     * </pre>
     *
     * @return String 部署路径
     */
    private static String getRootPath() {

        if (rootPath != null) {
            return rootPath;
        }

        File jarFile = new File(getJarPath());
        rootPath = URLDecoder.decode(jarFile.getParentFile()
                .getParentFile().getAbsolutePath(), StandardCharsets.UTF_8);
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
