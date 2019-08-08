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
package tsm.ebr.base;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 跨模块使用的公共数据结构以及常量定义
 * @author catforward
 */
public final class Task {

    public enum State {
        SUCCEEDED,
        ERROR,
        STANDBY,
        RUNNING,
    }

    public enum Type {
        ROOT,
        MODULE,
        TASK,
    }

    /**
     * 描述一个任务所需的名称定义
     */
    public static class Symbols {
        /** symbols in json file */
        public final static String KEY_UID = "uid";
        public final static String KEY_DESC = "desc";
        public final static String KEY_COMMAND = "command";
        public final static String KEY_UNITS = "units";
        public final static String KEY_PREDECESSORS = "predecessors";
        /** internal symbols in app */
        public final static String KEY_ROOT_UNIT = "KEY_ROOT_UNIT";
        public final static String KEY_UNIT_URL = "KEY_UNIT_URL";
        public final static String KEY_UNIT_TYPE = "KEY_UNIT_TYPE";
        public final static String KEY_PARENT_UNIT_URL = "KEY_PARENT_UNIT_URL";
    }

    /**
     * <pre>
     *     封装JSON定义的原始数据
     * </pre>
     */
    public static class Meta {
        public Meta parent = null;
        public final ArrayList<Meta> children = new ArrayList<>(Const.INIT_CAP);
        public final HashMap<String, String> symbols = new HashMap<>(Const.INIT_CAP);
        public final ArrayList<String> predecessorUrl = new ArrayList<>(Const.INIT_CAP);
    }

    /**
     * 可执行任务
     */
    public static class PerformableTask {
        public final String url;
        public final String command;

        public PerformableTask(String newUrl, String newCmd) {
            url = newUrl;
            command = newCmd;
        }
    }
}
