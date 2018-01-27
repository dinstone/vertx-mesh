
package com.dinstone.mesh.vertx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.message.MessageCodec;
import com.dinstone.mesh.vertx.message.ServiceRequest;
import com.dinstone.mesh.vertx.message.ServiceResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class ServiceSender {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceSender.class);

    private Vertx vertx;

    public ServiceSender(Vertx vertx) {
        this.vertx = vertx;
    }

    public void send(ServiceRequest request, Handler<AsyncResult<ServiceResponse>> handler) {
        LOG.info("proxy servcie: {} http://{}{}", request.getMethod(), request.getService(), request.getPath());

        vertx.eventBus().send(request.getService(), MessageCodec.encode(request), ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                Buffer buffer = (Buffer) ar.result().body();
                ServiceResponse serviceResponse = MessageCodec.decode(buffer, ServiceResponse.class);
                handler.handle(Future.succeededFuture(serviceResponse));
            }
        });
    }

}
