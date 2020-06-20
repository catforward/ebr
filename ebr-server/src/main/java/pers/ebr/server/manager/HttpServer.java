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
package pers.ebr.server.manager;

import io.vertx.ext.web.handler.FaviconHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

import static pers.ebr.server.common.Configs.KEY_HTTP_PORT;

/**
 * The HttpServerVerticle
 *
 * @author l.gong
 */
public class HttpServer extends AbstractVerticle {

    final static String WEB_ROOT = "static";
    final static String INDEX_HTML = "e-panel.html";
    final static String PROC_URL = "/proc";
    final static String FAVICON = "/favicon.ico";
    final static String GET_REQ_ALL = "/*";

    private final static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private io.vertx.core.http.HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route(FAVICON).handler(FaviconHandler.create());
        StaticHandler staticHandler = StaticHandler.create().setWebRoot(WEB_ROOT).setIndexPage(INDEX_HTML)
                .setAlwaysAsyncFS(true).setFilesReadOnly(true).setCachingEnabled(true)
                .setDirectoryListing(false).setIncludeHidden(false).setEnableFSTuning(true);
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create());
        router.get(GET_REQ_ALL).handler(staticHandler);
        router.post(PROC_URL).handler(new HttpRequestProcessor());

        server.requestHandler(router::handle).listen(config.getInteger(KEY_HTTP_PORT), res -> {
            if (res.succeeded()) {
                logger.info("HttpServer Start Success...");
            } else {
                logger.info("HttpServer Start Failure...");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
