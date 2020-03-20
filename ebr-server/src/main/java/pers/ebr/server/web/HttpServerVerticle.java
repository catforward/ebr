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
package pers.ebr.server.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pers.ebr.server.com.Properties.KEY_HTTP_PORT;

/**
 * The HttpServerVerticle
 *
 * @author l.gong
 */
public class HttpServerVerticle extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        StaticHandler staticHandler = StaticHandler.create().setAlwaysAsyncFS(true).setFilesReadOnly(true)
                .setCachingEnabled(true).setDirectoryListing(false).setIncludeHidden(false)
                .setEnableFSTuning(true).setIndexPage("panel.html");
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create());
        router.get("/").handler(staticHandler);
        router.get("/*").handler(staticHandler);
        router.post("/proc").handler(new HttpProcHandler());

        server.requestHandler(router::handle).listen(config.getInteger(KEY_HTTP_PORT), res -> {
            if (res.succeeded()) {
                System.out.println("HttpServer Start Success...");
            } else {
                System.err.println("HttpServer Start Failure...");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
