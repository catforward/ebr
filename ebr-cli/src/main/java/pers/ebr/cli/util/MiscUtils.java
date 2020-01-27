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

/**
 * <pre>
 * 未分类的工具函数集合
 * </pre>
 *
 * @author l.gong
 */
public final class MiscUtils {

    /**
     * 命令行或者命令行参数中不应出现的字符
     */
    private static final char[] commandBanList = {'|',
            //';',
            '&',
            '$',
            '>',
            '<',
            '`',
            //'\\',
            '!',
            '\t',
            '\n',
            '\r',
            '\f',
            '\u0000'};

    private MiscUtils() {
    }

    /**
     * <pre>
     * 检查对象是否为空
     * 如果为空则抛出空指针异常
     * 否则返回引用给调用者
     * </pre>
     *
     * @param obj 检查对象
     * @return Object 检查对象
     */
    public static <T extends Object> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    /**
     * <pre>
     * 检查对象中是否有禁止的字符
     * 如果有禁止的字符则抛出异常
     * </pre>
     *
     * @param args 检查对象命令行参数数组
     */
    public static void checkCommandBanList(String[] args) {
        checkNotNull(args);
        for (String arg : args) {
            checkCommandBanList(arg);
        }
    }

    /**
     * <pre>
     * 检查对象中是否有禁止的字符
     * 如果有禁止的字符则抛出异常
     * </pre>
     *
     * @param cmd 检查对象命令行
     */
    public static void checkCommandBanList(String cmd) {
        checkNotNull(cmd);
        for (char c : commandBanList) {
            if (cmd.indexOf(c) != -1) {
                throw new IllegalArgumentException(String.format("存在非法的字符[%s]", c));
            }
        }
    }
}
