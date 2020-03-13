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

import static pers.ebr.server.com.GlobalConstants.CONFIG_KEY_PORT;

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
        router.get("/static/*").handler(staticHandler);
        router.post("/proc").handler(new HttpProcHandler());

        server.requestHandler(router::handle).listen(config.getInteger(CONFIG_KEY_PORT), res -> {
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
