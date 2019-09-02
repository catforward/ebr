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
package tsm.ebr.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * <pre>
 * 日志工具类，初始化jdk内置logger
 * 基于以下目录结构获取日志配置文件
 * ${EbrRoot}/
 *        |-- conf/
 *        |    |-- logging.properties
 * </pre>
 *
 * @author catforward
 */
public final class LogUtils {
    private static Logger errorLogger;
    private final static String LOG_HEADER = "=========== START ===========";

    public static void init() throws IOException {
        initAppLogger();
        initErrLogger();
    }

    public static void dumpError(Exception ex) {
        if (errorLogger == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        errorLogger.warning(writer.toString());
    }

    public static boolean isEventLogEnabled() {
        return Boolean.parseBoolean((String) ConfigUtils.getOrDefault(ConfigUtils.Item.KEY_EVENT_LOG, "false"));
    }

    /**
     * <pre>
     * 初始化jdk内置logger
     * </pre>
     */
    private static void initAppLogger() throws IOException {
        String confPath = PathUtils.getConfPath();
        LogManager.getLogManager()
                .readConfiguration(new FileInputStream(confPath + File.separator + "logging.properties"));
        Logger logger = Logger.getLogger("ebr");
        logger.info(LOG_HEADER);
    }

    private static void initErrLogger() throws SecurityException, IOException {
        if (errorLogger != null) {
            return;
        }
        String fileName = PathUtils.getLogPath() + File.separator + "ebr_error.log";
        errorLogger = Logger.getLogger("ebr.error");
        errorLogger.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler(fileName, true);
        fileHandler.setLevel(Level.WARNING);
        fileHandler.setFormatter(new ErrorLogHandler());
        errorLogger.addHandler(fileHandler);
        errorLogger.info(LOG_HEADER);
        // LogManager.getLogManager().addLogger(logger);
    }
}

class ErrorLogHandler extends Formatter {
    private final ThreadLocal<DateTimeFormatter> dtf = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    @Override
    public String format(LogRecord record) {
        return String.format("[%s]: %s\n", LocalDateTime.now().format(dtf.get()), record.getMessage());
    }
}
