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

import ebr.core.ExternalBatchRunnerService;
import ebr.core.ServiceBuilder;
import ebr.core.ServiceEvent;
import ebr.core.util.AppLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static ebr.core.util.MiscUtils.checkCommandBanList;
import static ebr.cli.ConfigUtils.KEY_WORKER_NUM;

/**
 *
 * @author catforward
 */
public class Launcher {

    private static final int INIT_CAP = 8;

    public static void main(String[] args) throws IOException {
        checkCommandBanList(args);
        new Launcher().initAndStart(args);
    }

    private void initAndStart(String[] args) throws IOException {
        // 又不是服务器程序，不处理异常，如果有，那就任其终止程序
        ConfigUtils.merge(makeOptArgMap(args));
        AppLogger.init();
        // load from xml
        TaskDefineFileLoader loader = new TaskDefineFileLoader();
        TaskImpl rootTask = loader.load();
        // create ebr builder
        ServiceBuilder builder = ServiceBuilder.createExternalBatchRunnerBuilder();
        builder.setServiceMode(false);
        builder.setDevMode(true);
        builder.setMinWorkerNum(4);
        builder.setMaxWorkerNum(Integer.valueOf((String) ConfigUtils.getOrDefault(KEY_WORKER_NUM, "4")));
        // launch task flow
        ExternalBatchRunnerService service = builder.buildExternalBatchRunnerService();
        service.setServiceEventListener(this::onServiceEvent);
        String url = service.createJobFlow(rootTask);
        service.launchJobFlow(url);
    }

    private Map<String, String> makeOptArgMap(String[] args) {
        HashMap<String, String> optArg = new HashMap<>(INIT_CAP);
        GetOpts opts = new GetOpts(args, "f:j:");
        int c;
        try {
            while ((c = opts.getNextOption()) != -1) {
                if ((char) c == 'f') {
                    optArg.put(ConfigUtils.KEY_INSTANT_TASK, opts.getOptionArg());
                } else if ((char) c == 'j') {
                    optArg.put(ConfigUtils.KEY_WORKER_NUM, opts.getOptionArg());
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
        System.err.println("Usage: <path>/jar_file [-f] <name of task define file>");
        System.exit(1);
    }

    private void onServiceEvent(ServiceEvent event) {
        System.err.println(String.format("event:[%s], data:[%s]",event.type().name(), event.data().toString()));
    }
}
