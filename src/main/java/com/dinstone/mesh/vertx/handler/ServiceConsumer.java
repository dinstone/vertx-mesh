
package com.dinstone.mesh.vertx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.message.MessageCodec;
import com.dinstone.mesh.vertx.message.ServiceRequest;
import com.dinstone.mesh.vertx.message.ServiceResponse;
import com.dinstone.mesh.vertx.registry.ServiceRecord;
import com.dinstone.mesh.vertx.registry.ServiceRegistry;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;

public class ServiceConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceConsumer.class);

    private HttpClient httpClient;

    private ServiceRegistry serviceRegistry;

    public ServiceConsumer(Vertx vertx, ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

        this.httpClient = vertx.createHttpClient();
    }

    public void consume(Message<Buffer> message) {
        ServiceRequest request = MessageCodec.decode(message.body(), ServiceRequest.class);
        ServiceRecord record = serviceRegistry.lookup(request.getService());
        if (record == null) {
            ServiceResponse response = new ServiceResponse();
            response.setStatus(500);
            response.setMessage("service is not avilabe");

            message.reply(MessageCodec.encode(response));

            return;
        }

        LOG.info("invoke servcie: {} {} http://{}:{}{}", request.getService(), request.getMethod(), record.getHost(),
            record.getPort(), request.getPath());
        httpClient.request(HttpMethod.valueOf(request.getMethod()), record.getPort(), record.getHost(),
            request.getPath(), res -> {
                res.bodyHandler(buffer -> {
                    ServiceResponse response = new ServiceResponse();
                    response.setStatus(res.statusCode());
                    response.setMessage(res.statusMessage());
                    response.setContentType(res.getHeader(HttpHeaders.CONTENT_TYPE));
                    response.setContent(buffer.getBytes());

                    message.reply(MessageCodec.encode(response));
                });
            }).setTimeout(1000).end();
    }

}
