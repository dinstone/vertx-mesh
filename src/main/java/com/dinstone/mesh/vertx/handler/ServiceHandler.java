
package com.dinstone.mesh.vertx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.message.ServiceRequest;
import com.dinstone.mesh.vertx.message.ServiceResponse;
import com.dinstone.mesh.vertx.registry.ServiceRecord;
import com.dinstone.mesh.vertx.registry.ServiceRegistry;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ServiceHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceHandler.class);

    private ServiceSender serviceSender;

    private ServiceRegistry serviceRegistry;

    public ServiceHandler(Vertx vertx, ServiceRegistry registry) {
        this.serviceRegistry = registry;
        this.serviceSender = new ServiceSender(vertx);
    }

    public void upline(RoutingContext rc) {
        ServiceRecord record = parseServiceRecord(rc);

        LOG.info("service upline: {}://{}:{}{}", record.getName(), record.getHost(), record.getPort(),
            record.getHealth());
        serviceRegistry.publish(record);

        success(rc);
    }

    public void dwline(RoutingContext rc) {
        ServiceRecord record = parseServiceRecord(rc);

        LOG.info("service dwline: {}://{}:{}{}", record.getName(), record.getHost(), record.getPort(),
            record.getHealth());
        serviceRegistry.remove(record);

        success(rc);
    }

    private ServiceRecord parseServiceRecord(RoutingContext rc) {
        String serviceName = rc.request().getParam("service-name");
        String servicePort = rc.request().getParam("service-port");
        String serviceHost = rc.request().getParam("service-host");
        String serviceHealth = rc.request().getParam("service-health");

        ServiceRecord sr = new ServiceRecord(serviceName, Integer.parseInt(servicePort));
        if (serviceHost != null) {
            sr.setHost(serviceHost);
        }
        if (serviceHealth != null && serviceHealth.length() > 0) {
            if (!serviceHealth.startsWith("/")) {
                serviceHealth = "/" + serviceHealth;
            }
            sr.setHealth(serviceHealth);
        }
        return sr;
    }

    public void api(RoutingContext rc) {
        String uri = rc.request().uri();
        String service = null;
        String path = null;

        int index = uri.indexOf('/', 1);
        if (index != -1) {
            service = uri.substring(1, index);
            path = uri.substring(index);
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(service);
        serviceRequest.setPath(path);
        serviceRequest.setMethod(rc.request().method().name());
        serviceRequest.setContentType(rc.request().getHeader(HttpHeaders.CONTENT_TYPE));

        Buffer body = rc.getBody();
        if (body != null && body.length() > 0) {
            serviceRequest.setContent(body.getBytes());
        }

        serviceSender.send(serviceRequest, ar -> {
            if (ar.failed()) {
                failed(rc, ar.cause());
            } else {
                ServiceResponse serviceResponse = ar.result();

                HttpServerResponse response = rc.response();
                response.setStatusCode(serviceResponse.getStatus());
                response.setStatusMessage(serviceResponse.getMessage());
                String contentType = serviceResponse.getContentType();
                if (contentType != null) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
                }
                if (serviceResponse.getContent() == null) {
                    response.end();
                } else {
                    response.end(Buffer.buffer(serviceResponse.getContent()));
                }
            }
        });
    }

}
