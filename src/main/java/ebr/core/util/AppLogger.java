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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public final class AppLogger {
    private static Logger errorLogger = null;
    private static Logger messageLogger = null;
    private static final String LOG_HEADER = "=========== EBR START ===========";
    private static final int CALLER_INDEX = 3;

    private AppLogger() {}

    public static void init() throws IOException {
        initMessageFileLogger();
        initErrorFileLogger();
    }

    private static String msgWithCaller(final String level, final String msg) {
        if (msg == null || msg.trim().isBlank()) {
            return "";
        }
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        /*
         * 0 -> thread
         * 1 -> self method
         * 2 -> AppLogger.info or trace or debug
         * 3 -> class in ebr app
         */
        if (elements.length > (CALLER_INDEX + 1)) {
            String className = elements[3].getClassName();
            String lineNumber = String.valueOf(elements[3].getLineNumber());
            return String.format("[%s]: %s(%s) %s", level, className, lineNumber, msg);
        }
        return msg;
    }

    public static void info(final String msg) {
        if (messageLogger == null) {
            return;
        }
        final String infoMsg = msgWithCaller(" INFO", msg);
        messageLogger.info(infoMsg);
    }

    public static void trace(final String msg) {
        if (messageLogger == null) {
            return;
        }
        final String traceMsg = msgWithCaller("TRACE", msg);
        messageLogger.fine(traceMsg);
    }

    public static void debug(final String msg) {
        if (messageLogger == null) {
            return;
        }
        final String debugMsg = msgWithCaller("DEBUG", msg);
        messageLogger.info(debugMsg);
    }

    public static void dumpError(Exception ex) {
        if (errorLogger == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        final String errMsg = String.format("[%s]: %s", "ERROR", writer.toString());
        errorLogger.severe(errMsg);
    }

    /**
     * <pre>
     * 初始化jdk内置logger
     * </pre>
     */
    private static void initMessageFileLogger() throws IOException {
        if (messageLogger != null) {
            return;
        }
        String fileName = PathUtils.getLogPath() + File.separator + "ebr_dev_app.log";
        messageLogger = Logger.getLogger("ebr");
        messageLogger.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler(fileName, true);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatHandler());
        messageLogger.addHandler(fileHandler);
        messageLogger.info(LOG_HEADER);
    }

    private static void initErrorFileLogger() throws IOException {
        if (errorLogger != null) {
            return;
        }
        String fileName = PathUtils.getLogPath() + File.separator + "ebr_dev_error.log";
        errorLogger = Logger.getLogger("ebr.error");
        errorLogger.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler(fileName, true);
        fileHandler.setLevel(Level.SEVERE);
        fileHandler.setFormatter(new LogFormatHandler());
        errorLogger.addHandler(fileHandler);
        errorLogger.severe(LOG_HEADER);
    }
}

class LogFormatHandler extends Formatter {
    private final ThreadLocal<DateTimeFormatter> dtf = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    @Override
    public String format(LogRecord record) {
        return String.format("[%s]: %s%n", LocalDateTime.now().format(dtf.get()), record.getMessage());
    }
}
