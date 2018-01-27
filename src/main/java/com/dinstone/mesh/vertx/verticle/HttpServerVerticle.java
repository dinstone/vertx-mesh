
package com.dinstone.mesh.vertx.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.handler.AccessLogHandler;
import com.dinstone.mesh.vertx.handler.ServiceHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerVerticle.class);

    private JsonObject configuration;

    private ServiceHandler serviceHandler;

    public HttpServerVerticle(JsonObject configuration, ServiceHandler serviceHandler) {
        super();
        this.configuration = configuration;
        this.serviceHandler = serviceHandler;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        // configuration = config();

        // String registryName = config().getString("app.metrics.registry", "app.metrics");
        // metricRegistry = SharedMetricRegistries.getOrCreate(registryName);
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

        mainRouter.route("/upline").handler(serviceHandler::upline);
        mainRouter.route("/dwline").handler(serviceHandler::dwline);
        mainRouter.route("/*").handler(serviceHandler::api);

        // vertx.setPeriodic(5000, serviceHandler::healthCheck);

        // vertx.eventBus().registerDefaultCodec(ServiceRequest.class, new ServiceRequestCodec());
        // vertx.eventBus().registerDefaultCodec(ServiceResponse.class, new ServiceResponseCodec());

        // http server
        int serverPort = configuration.getInteger("web.http.port", 8080);
        HttpServerOptions serverOptions = new HttpServerOptions().setIdleTimeout(180);
        vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept).listen(serverPort, ar -> {
            if (ar.succeeded()) {
                LOG.info("start web http success, web.http.port={}", serverPort);
                startFuture.complete();
            } else {
                LOG.error("start web http failed, web.http.port={}", serverPort);
                startFuture.fail(ar.cause());
            }
        });
    }

}