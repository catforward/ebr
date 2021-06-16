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
package pers.tsm.ebr.common;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 *
 *
 * @author l.gong
 */
public final class Configs {
	private final static Logger logger = LoggerFactory.getLogger(Configs.class);
	
	private final JsonObject config = new JsonObject();

	private static class InstanceHolder {
        private final static Configs INSTANCE = new Configs();
    }

    private Configs() {}

    /**
     * 读取配置文件
     * @throws IOException 读取配置文件失败时
     * @throws URISyntaxException 获取配置文件URI资源失败时
     */
    public static void load() throws IOException, URISyntaxException {
        InstanceHolder.INSTANCE.loadConfigFile();
        logger.info("the config info -> {}", InstanceHolder.INSTANCE.config.encode());
        System.out.println("Load Configuration ........... DONE");
    }

    /**
     * 取得一份配置文件的拷贝
     * @return JsonObject
     */
    public static JsonObject get() {
        return InstanceHolder.INSTANCE.config.copy();
    }

    /**
     * 读取配置文件
     * @param holder 保存内部配置文件的集合
     * @throws IOException 读取配置文件失败时
     * @throws URISyntaxException 获取配置文件URI资源失败时
     */
    private void loadConfigFile() throws IOException, URISyntaxException {
        URI innerConfigFile = getClass().getResource("/config.json").toURI();
        JsonObject tmp = new JsonObject(Files.readString(java.nio.file.Paths.get(innerConfigFile)));
        tmp.mergeIn(config);
    }
}
