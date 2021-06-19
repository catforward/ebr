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
package pers.tsm.ebr.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;

import io.vertx.core.json.JsonObject;


import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


/**
 *
 *
 * @author l.gong
 */
public class TaskDefineRepo {
	private static final Logger logger = LoggerFactory.getLogger(TaskDefineRepo.class);
	
	/** key: full path, value: prop class*/
	private final Map<String, TaskDefineFileProp> defineFiles;
	/** key: full path, value: file content(JsonObject)*/
	private Cache<String, JsonObject> defineFileContent;
	
	public static final RemovalListener<String, JsonObject> removalListener = notification -> {
		logger.info("define file content cache: Key {} was removed ({})",
    			notification.getKey(), notification.getCause());
	};
	
	
	private static class InstanceHolder {
        private static final TaskDefineRepo INSTANCE = new TaskDefineRepo();
    }
	
	private TaskDefineRepo() {
		defineFiles = new ConcurrentHashMap<>();
	}
	
	public static void removeAll() {
		InstanceHolder.INSTANCE.defineFileContent.invalidateAll();
		InstanceHolder.INSTANCE.defineFiles.clear();
	}
	
	public static void dumpAll() {
		logger.info("############ define file ############");
		InstanceHolder.INSTANCE.defineFiles.forEach((k, v) -> logger.info(v.toString()));
		logger.info("############ define file's content ############");
		InstanceHolder.INSTANCE.defineFileContent.asMap().forEach((k, v) -> logger.info("{} = {}", k, v));
	}
	
	public static void setFileContentCache(Cache<String, JsonObject> cache) {
		requireNonNull(cache);
		synchronized(InstanceHolder.INSTANCE) {
			if (!isNull(InstanceHolder.INSTANCE.defineFileContent)) {
				InstanceHolder.INSTANCE.defineFileContent.invalidateAll();
			}
			InstanceHolder.INSTANCE.defineFileContent = cache;
		}
	}
	
	public static Map<String, TaskDefineFileProp> copyDefineFiles() {
		HashMap<String, TaskDefineFileProp> copyMap = new HashMap<>();
		copyMap.putAll(InstanceHolder.INSTANCE.defineFiles);
		return copyMap;
	}
	
	public static void addDefineFile(TaskDefineFileProp prop) {
		InstanceHolder.INSTANCE.defineFiles.put(prop.getFullPath(), prop);
		try {
			JsonObject obj = new JsonObject(Files.readString(Paths.get(prop.getFullPath())));
			InstanceHolder.INSTANCE.defineFileContent.invalidate(prop.getFullPath());
			InstanceHolder.INSTANCE.defineFileContent.put(prop.getFullPath(), obj);
		} catch (IOException ex) {
			logger.error("can not read from [{}], caching skipped...", prop.getFullPath(), ex);
		}
	}
	
	public static void deleteDefineFile(TaskDefineFileProp prop) {
		InstanceHolder.INSTANCE.defineFiles.remove(prop.getFullPath());
		InstanceHolder.INSTANCE.defineFileContent.invalidate(prop.getFullPath());
	}
	
}
