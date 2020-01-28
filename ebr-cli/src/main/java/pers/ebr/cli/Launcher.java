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

import pers.ebr.cli.core.ExternalBatchRunner;
import pers.ebr.cli.util.AppLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static pers.ebr.cli.util.MiscUtils.checkCommandBanList;

/**
 *
 * @author l.gong
 */
public class Launcher {

    public static void main(String[] args) throws IOException {
        checkCommandBanList(args);
        new Launcher().initAndStart(args);
    }

    private void initAndStart(String[] args) throws IOException {
        // 又不是服务器程序，不处理异常，如果有，那就任其终止程序
        ConfigUtils.merge(makeOptArgMap(args));
        AppLogger.init();
        TaskDefineFileLoader loader = new TaskDefineFileLoader();
        ExternalBatchRunner.getInstance().init().launchJobFlow(loader.load());
    }

    private Map<String, String> makeOptArgMap(String[] args) {
        HashMap<String, String> optArg = new HashMap<>();
        GetOpts opts = new GetOpts(args, "f:d");
        int c;
        try {
            while ((c = opts.getNextOption()) != -1) {
                if ((char) c == 'f') {
                    optArg.put(ConfigUtils.KEY_INSTANT_TASK, opts.getOptionArg());
                } else if ((char) c == 'd') {
                    optArg.put(ConfigUtils.EBR_LOG_ENABLE, Boolean.toString(true));
                } else {
                    showUsage();
                }
            }
        } catch (IllegalArgumentException ex) {
            showUsage();
            Logger.getGlobal().fine(ex.getMessage());
        }
        return optArg;
    }

    private static void showUsage() {
        System.err.println("Usage: <path>/ebr_cli.jar [-f] <name of task define file>");
        System.exit(1);
    }

}
