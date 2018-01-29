
package com.dinstone.mesh.vertx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.message.ServiceRequest;
import com.dinstone.mesh.vertx.message.ServiceResponse;
import com.dinstone.mesh.vertx.registry.ServiceRegistry;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ServiceHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceHandler.class);

    private ServiceSender serviceSender;

    public ServiceHandler(Vertx vertx, ServiceRegistry registry) {
        this.serviceSender = new ServiceSender(vertx);
    }

    public void proxy(RoutingContext rc) {
        String service = null;
        String path = null;

        service = rc.request().getHeader("Host");
        if (service != null) {
            path = rc.request().path();
            String query = rc.request().query();
            if (query != null) {
                path = path + "?" + query;
            }
        }

        if (service == null) {
            failed(rc, 400, "can't find service from Host");
            return;
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

    public void api(RoutingContext rc) {
        String service = null;
        String path = null;

        String uri = rc.request().uri();
        int index = uri.indexOf('/', 1);
        if (index != -1) {
            service = uri.substring(1, index);
            path = uri.substring(index);
        }

        if (service == null) {
            failed(rc, 400, "can't find service from Path");
            return;
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
