/**
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
package pers.tsm.ebr.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import pers.tsm.ebr.base.BaseService;
import pers.tsm.ebr.base.IResult;
import pers.tsm.ebr.common.AppConfigs;
import pers.tsm.ebr.common.AppPaths;
import pers.tsm.ebr.common.ServiceSymbols;
import pers.tsm.ebr.common.StringUtils;
import pers.tsm.ebr.common.Symbols;
import pers.tsm.ebr.data.TaskDefineFileProp;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.types.ResultEnum;


/**
 *
 *
 * @author l.gong
 */
public class FsRepoWatchService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(FsRepoWatchService.class);

    private long timerID = 0L;
    private long scanInterval = 0L;

    @Override
    public void start() throws Exception {
        super.start();
        registerMsg(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE);
        scanInterval = config().getLong(AppConfigs.SERVICE_FS_DATA_CHECK_INTERVAL_SECONDS, 60L) * 1000;
        // the first time
        vertx.setTimer(2000, id -> emitMsg(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE, emptyJsonObject));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.cancelTimer(timerID);
    }

    @Override
    protected String getServiceName() {
        return FsRepoWatchService.class.getName();
    }

    @Override
    protected Future<IResult> doMsg() {
        return Future.future(promise -> {
            List<TaskDefineFileProp> files = new ArrayList<>();
            scanDataFolder(files)
            .compose(v -> loadFileContent(files))
            .onSuccess(ar -> {
                // for next time
                timerID = vertx.setTimer(scanInterval,
                        id -> emitMsg(ServiceSymbols.MSG_ACTION_REFRESH_FS_DEFINE, emptyJsonObject));
                promise.complete(ResultEnum.SUCCESS);
            })
            .onFailure(promise::fail);
        });
    }

    private Future<Void> scanDataFolder(List<TaskDefineFileProp> files) {
        return Future.future(promise -> {
            Path dataStorePath = new File(AppPaths.getDataPath()).toPath();
            try {
                Files.walkFileTree(dataStorePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                        File file = filePath.toFile();
                        if (file.canRead() && file.getName().endsWith(Symbols.FLOW_FILE_SUFFIX)) {
                            TaskDefineFileProp prop = new TaskDefineFileProp();
                            prop.setAbsolutePath(file.getAbsolutePath());
                            prop.setFlowUrl(StringUtils.toFlowUrl(dataStorePath, filePath));
                            prop.setFileSize(attrs.size());
                            prop.setLastModifiedTime(file.lastModified());
                            files.add(prop);
                        }
                        return super.visitFile(filePath, attrs);
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        logger.error("visit file failed.", exc);
                        return FileVisitResult.CONTINUE;
                    }

                });
            } catch (IOException e) {
                promise.fail(e);
                return;
            }
            promise.complete();
        });
    }

    private Future<Void> loadFileContent(List<TaskDefineFileProp> files) {
        return Future.future(promise -> {
            if (files.isEmpty()) {
                TaskDefineRepo.release();
                promise.complete();
                return;
            }
            Map<String, TaskDefineFileProp> copyMap =  TaskDefineRepo.copyDefineFileInfo();
            files.forEach(prop -> {
                if (prop.isNewerThan(copyMap.remove(prop.getFlowUrl()))) {
                    TaskDefineRepo.addDefineFile(prop);
                }
            });
            copyMap.forEach((k, v) -> {
                TaskDefineRepo.deleteDefineFile(v);
            });
            copyMap.clear();
            promise.complete();
        });
    }

}
