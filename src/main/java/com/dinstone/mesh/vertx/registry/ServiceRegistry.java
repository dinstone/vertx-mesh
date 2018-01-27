
package com.dinstone.mesh.vertx.registry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dinstone.mesh.vertx.handler.ServiceConsumer;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.ConcurrentHashSet;

public class ServiceRegistry {

    private Map<String, ServiceRecord> serviceRecordMap = new ConcurrentHashMap<>();

    private Map<String, Set<ServiceStatus>> serviceStatusMap = new ConcurrentHashMap<>();

    private Vertx vertx;

    private HttpClient httpClient;

    private ServiceConsumer serviceConsumer;

    public ServiceRegistry(Vertx vertx) {
        this.vertx = vertx;
        this.httpClient = vertx.createHttpClient();
        this.serviceConsumer = new ServiceConsumer(vertx, this);

        this.vertx.setPeriodic(5000, this::healthCheck);
    }

    public void publish(ServiceRecord record) {
        serviceRecordMap.putIfAbsent(getRecordKey(record), record);
    }

    public ServiceRecord remove(ServiceRecord record) {
        return serviceRecordMap.remove(getRecordKey(record));
    }

    private String getRecordKey(ServiceRecord record) {
        return record.getName() + "/" + record.getHost() + ":" + record.getPort();
    }

    public ServiceRecord lookup(String service) {
        Set<ServiceStatus> serviceStatusSet = serviceStatusMap.get(service);
        if (serviceStatusSet != null) {
            ServiceStatus nextService = null;
            int minCount = Integer.MAX_VALUE;
            for (ServiceStatus serviceStatus : serviceStatusSet) {
                int invokeCount = serviceStatus.getInvokeCount();
                if (invokeCount < minCount) {
                    minCount = invokeCount;
                    nextService = serviceStatus;
                }
            }

            if (nextService != null) {
                nextService.incrInvokeCount();
                return nextService.getRecord();
            }
        }

        return null;
    }

    private void healthCheck(Long id) {
        serviceRecordMap.forEach((key, record) -> {

            httpClient.get(record.getPort(), record.getHost(), record.getHealth(), res -> {
                if (res == null || res.statusCode() != 200) {
                    if (inactive(record)) {
                        unregistry(record);
                    }
                } else {
                    if (active(record)) {
                        registry(record);
                    }
                }
            }).setTimeout(1000).end();
        });
    }

    private boolean active(ServiceRecord record) {
        Set<ServiceStatus> serviceStatus = serviceStatusMap.get(record.getName());
        if (serviceStatus == null) {
            serviceStatus = new ConcurrentHashSet<>();
            serviceStatusMap.put(record.getName(), serviceStatus);
        }

        return serviceStatus.add(new ServiceStatus(record));
    }

    private boolean inactive(ServiceRecord record) {
        Set<ServiceStatus> serviceStatusSet = serviceStatusMap.get(record.getName());
        if (serviceStatusSet != null) {
            return serviceStatusSet.removeIf(serviceStatus -> {
                if (serviceStatus.getRecord().equals(record)) {
                    return true;
                }
                return false;
            });
        }

        return false;
    }

    private void registry(ServiceRecord r) {
        vertx.eventBus().consumer(r.getName(), serviceConsumer::consume);
    }

    private void unregistry(ServiceRecord r) {
        vertx.eventBus().consumer(r.getName()).unregister();
    }

}
