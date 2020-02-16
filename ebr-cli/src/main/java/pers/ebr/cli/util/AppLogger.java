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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * <pre>
 *  The Logger in EBR
 * </pre>
 *
 * @author l.gong
 */
public final class AppLogger {
    private Logger messageLogger = null;
    private static final int CALLER_INDEX = 3;

    private static class AppLoggerHolder{
        static final AppLogger LOGGER = new AppLogger();
    }

    private AppLogger() {}

    public static void init() {
        boolean logFileEnable = Boolean.parseBoolean((String) ConfigUtils.getOrDefault(ConfigUtils.EBR_LOG_ENABLE, "false"));
        if (!logFileEnable) {
            return;
        }
        initMessageConsoleLogger();
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
        if (AppLoggerHolder.LOGGER.messageLogger == null) {
            return;
        }
        final String infoMsg = msgWithCaller(" INFO", msg);
        AppLoggerHolder.LOGGER.messageLogger.info(infoMsg);
    }

    public static void trace(final String msg) {
        if (AppLoggerHolder.LOGGER.messageLogger == null) {
            return;
        }
        final String traceMsg = msgWithCaller("TRACE", msg);
        AppLoggerHolder.LOGGER.messageLogger.fine(traceMsg);
    }

    public static void debug(final String msg) {
        if (AppLoggerHolder.LOGGER.messageLogger == null) {
            return;
        }
        final String debugMsg = msgWithCaller("DEBUG", msg);
        AppLoggerHolder.LOGGER.messageLogger.info(debugMsg);
    }

    public static void dumpError(Exception ex) {
        if (AppLoggerHolder.LOGGER.messageLogger == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        final String errMsg = String.format("[%s]: %s", "ERROR", writer.toString());
        AppLoggerHolder.LOGGER.messageLogger.severe(errMsg);
    }

    private static void initMessageConsoleLogger() {
        if (AppLoggerHolder.LOGGER.messageLogger != null) {
            return;
        }
        AppLoggerHolder.LOGGER.messageLogger = Logger.getLogger("ebr");
        AppLoggerHolder.LOGGER.messageLogger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new LogFormatHandler());
        AppLoggerHolder.LOGGER.messageLogger.addHandler(consoleHandler);
    }

}

class LogFormatHandler extends Formatter {
    private final ThreadLocal<DateTimeFormatter> dtf = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    @Override
    public String format(LogRecord record) {
        return String.format("[%s]: %s%n", LocalDateTime.now().format(dtf.get()), record.getMessage());
    }
}