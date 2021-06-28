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
package pers.tsm.ebr.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import pers.tsm.ebr.AppMain;
import pers.tsm.ebr.common.AppContext;

import static java.util.Objects.isNull;
import static pers.tsm.ebr.common.Symbols.*;

import java.util.Map;

/**
 *
 *
 * @author l.gong
 */
public class HttpApiServer extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(HttpApiServer.class);
    private HttpServer server;
    
    //private static final String WEB_ROOT = "static";
    //private static final String INDEX_HTML = "index.html";
    //private static final String FAVICON = "/favicon.ico";
    //private static final String HTTP_GET_ALL = BASE_URL + "/*";

    @Override
    public void start() throws Exception {
        super.start();
        server = createHttpServer();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (!isNull(server)) {
            server.close();
        }
    }

    private HttpServer createHttpServer() {
        Router router = Router.router(vertx);
        // session
//        LocalSessionStore store = LocalSessionStore.create(vertx);
//        router.route().handler(SessionHandler.create(store));
        // default handler
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create(vertx));
//        router.route(FAVICON).handler(FaviconHandler.create(vertx));
//        router.get(HTTP_GET_ALL).handler(StaticHandler.create()
//        		.setWebRoot(WEB_ROOT)
//        		.setIndexPage(INDEX_HTML)
//                .setAlwaysAsyncFS(true)
//                .setFilesReadOnly(true)
//                .setCachingEnabled(true)
//                .setDirectoryListing(false)
//                .setIncludeHidden(false)
//                .setEnableFSTuning(true));
        // API
        router.route(BASE_URL + "/api").handler(context ->
            context.response().end(new JsonObject().put("version", AppMain.VERSION).encodePrettily())
        );
        Map<String, String> mapping = AppContext.getApiServiceMapping();
        BaseHandler handler = new BaseHandler(mapping);
        mapping.forEach((apiUrl, serviceId) -> router.post(apiUrl).handler(handler));

        // server
        String host = config().getString("address", "localhost");
        int port = config().getInteger("port", 8081);
        return vertx.createHttpServer().requestHandler(router).listen(port, host, ar -> {
            if (ar.succeeded()) {
                logger.info("EBR Web Api Server is running on port: {} ", port);
            } else {
                logger.error("Failed to start EBR Web Api Server", ar.cause());
            }
        });
    }
    
}
