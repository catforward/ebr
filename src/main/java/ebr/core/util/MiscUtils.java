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

/**
 * <pre>
 * 未分类的工具函数集合
 * </pre>
 *
 * @author catforward
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
