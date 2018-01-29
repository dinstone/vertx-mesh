
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

public class HttpProxyVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpProxyVerticle.class);

    private JsonObject configuration;

    private ServiceHandler serviceHandler;

    public HttpProxyVerticle(JsonObject configuration, ServiceHandler serviceHandler) {
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

        mainRouter.route("/*").handler(serviceHandler::proxy);

        // vertx.setPeriodic(5000, serviceHandler::healthCheck);

        // vertx.eventBus().registerDefaultCodec(ServiceRequest.class, new ServiceRequestCodec());
        // vertx.eventBus().registerDefaultCodec(ServiceResponse.class, new ServiceResponseCodec());

        // http server
        String label = config().getString("label");
        int port = config().getInteger("port", 8484);
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