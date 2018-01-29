
package com.dinstone.mesh.vertx.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.handler.AccessLogHandler;
import com.dinstone.mesh.vertx.handler.AdminHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpAdminVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpAdminVerticle.class);

    private AdminHandler adminHandler;

    public HttpAdminVerticle(JsonObject configuration, AdminHandler adminHandler) {
        super();
        this.adminHandler = adminHandler;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router mainRouter = Router.router(vertx);
        mainRouter.route().failureHandler(rc -> {
            LOG.error("handler logic occur error", rc.failure());
            rc.response().end();
        });
        mainRouter.route().handler(new AccessLogHandler());
        mainRouter.route().handler(BodyHandler.create());

        mainRouter.route("/upline").handler(adminHandler::upline);
        mainRouter.route("/dwline").handler(adminHandler::dwline);

        // http server
        String label = config().getString("label", "admin");
        int port = config().getInteger("port", 8888);
        String host = config().getString("host", "0.0.0.0");
        HttpServerOptions serverOptions = new HttpServerOptions().setIdleTimeout(180);
        vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept).listen(port, host, ar -> {
            if (ar.succeeded()) {
                LOG.info("start http success, {} on {}:{}", label, host, port);
                startFuture.complete();
            } else {
                LOG.error("start http failed, {} on {}:{}", label, host, port);
                startFuture.fail(ar.cause());
            }
        });
    }

}