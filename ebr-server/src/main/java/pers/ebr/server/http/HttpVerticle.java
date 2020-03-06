package pers.ebr.server.http;

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

public class HttpVerticle extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(HttpVerticle.class);

    private HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
        JsonObject config = config();
        server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        StaticHandler staticHandler = StaticHandler.create().setAlwaysAsyncFS(true).setFilesReadOnly(true)
                .setCachingEnabled(true).setDirectoryListing(false).setIncludeHidden(false)
                .setEnableFSTuning(true).setIndexPage("ebr-panel.html");
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create());
        router.get("/").handler(staticHandler);
        router.get("/static/*").handler(staticHandler);
        router.post("/proc/*").handler(new HttpProcHandler());

        server.requestHandler(router::handle).listen(config.getInteger(CONFIG_KEY_PORT), res -> {
            if (res.succeeded()) {
                logger.info("HttpServer Start Success...");
            } else {
                logger.error("HttpServer Start Failure...");
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
