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
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import pers.tsm.ebr.common.BaseService;
import pers.tsm.ebr.common.IResult;
import pers.tsm.ebr.common.AppPaths;
import pers.tsm.ebr.data.TaskDefineFileProp;
import pers.tsm.ebr.data.TaskDefineRepo;
import pers.tsm.ebr.types.ServiceResultEnum;


/**
 *
 *
 * @author l.gong
 */
public class FsDataWatchService extends BaseService {
	private static final Logger logger = LoggerFactory.getLogger(FsDataWatchService.class);
	
	private long timerID = 0L;
	private long scanInterval = 0;
	
	@Override
    public void start() throws Exception {
        super.start();
        registerMsg(ServiceSymbols.MSG_REFRESH_FS_TASK_DEFINE);
        scanInterval = config().getLong("fsDataCheckIntervalSeconds", 60L) * 1000;
        // the first time
        vertx.setTimer(5000, id -> pubMsg(ServiceSymbols.MSG_REFRESH_FS_TASK_DEFINE, emptyJsonObject));
    }
	@Override
    public void stop() throws Exception {
        super.start();
        vertx.cancelTimer(timerID);
    }

	@Override
	protected String getServiceName() {
		return FsDataWatchService.class.getName();
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
						id -> pubMsg(ServiceSymbols.MSG_REFRESH_FS_TASK_DEFINE, emptyJsonObject));
				promise.complete(ServiceResultEnum.NORMAL);
			})
			.onFailure(promise::fail);
		});
	}
	
	private Future<Void> scanDataFolder(List<TaskDefineFileProp> files) {
		return Future.future(promise -> {
			try {
				Files.walkFileTree(new File(AppPaths.getDataPath()).toPath(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						File scanFile = file.toFile();
						if (scanFile.canRead() && scanFile.getName().endsWith(".json")) {
							TaskDefineFileProp prop = new TaskDefineFileProp();
							prop.setFullPath(scanFile.getAbsolutePath());
							prop.setLastModifiedTime(scanFile.lastModified());
							//logger.debug(prop.toString());
							files.add(prop);
						}
						return super.visitFile(file, attrs);
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
				TaskDefineRepo.removeAll();
				promise.complete();
				return;
			}
			Map<String, TaskDefineFileProp> copyMap =  TaskDefineRepo.copyDefineFiles();
			files.forEach(prop -> {
				if (prop.isNewerThan(copyMap.remove(prop.getFullPath()))) {
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
