/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * <pre>
 * 路径工具类，基于以下目录结构获取相关路径
 *
 * ${EbrRoot}/
 *     |-- EBR_Start.sh
 *     |-- EBR_End.sh
 *     |-- conf/
 *     |     |-- log.properties
 *     |-- def/
 *     |     |-- tasks_win.xml
 *     |     |-- tasks_linux.xml
 * </pre>
 *
 * @author catforward
 */
public final class PathUtils {

    /**
     * 部署根路径
     */
    private static String rootPath;

    /**
     * 得到EBR的log部署目录
     *
     * @return String 部署路径
     */
    public static String getLogPath() {
        return getRootPath() + File.separator + "logs";
    }

    /**
     * 得到EBR的conf部署目录
     *
     * @return String 部署路径
     */
    public static String getConfPath() {
        return getRootPath() + File.separator + "conf";
    }

    /**
     * 得到EBR的def部署目录
     *
     * @return String 部署路径
     */
    public static String getDefPath() {
        return getRootPath() + File.separator + "def";
    }

    /**
     * 取得EBR的部署路径
     *
     * @return String 部署路径
     */
    public static String getRootPath() {

        if (rootPath != null) {
            return rootPath;
        }

        try {
            File jarFile = new File(getJarPath());
            rootPath = URLDecoder.decode(jarFile.getParentFile() // jar所在目录
                    .getParentFile() // jar父路径
                    .getAbsolutePath(), "UTF8");
            return rootPath;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("取得EBR的部署路径失败...", e);
        }
    }

    /**
     * 取得EBR的JAR文件部署路径
     *
     * @return String 部署路径
     */
    private static String getJarPath() throws UnsupportedEncodingException {
        return URLDecoder.decode(PathUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF8");
    }
}
