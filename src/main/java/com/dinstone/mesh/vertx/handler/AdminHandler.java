
package com.dinstone.mesh.vertx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.registry.ServiceRecord;
import com.dinstone.mesh.vertx.registry.ServiceRegistry;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class AdminHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AdminHandler.class);

    private ServiceRegistry serviceRegistry;

    public AdminHandler(Vertx vertx, ServiceRegistry registry) {
        this.serviceRegistry = registry;
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

}
