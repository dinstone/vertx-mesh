
package com.dinstone.mesh.vertx.registry;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceStatus {

    private AtomicInteger count = new AtomicInteger();

    private ServiceRecord record;

    public ServiceStatus(ServiceRecord record) {
        this.record = record;
    }

    public ServiceRecord getRecord() {
        return record;
    }

    public int getInvokeCount() {
        return count.get();
    }

    public int incrInvokeCount() {
        return count.incrementAndGet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((record == null) ? 0 : record.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServiceStatus other = (ServiceStatus) obj;
        if (record == null) {
            if (other.record != null) {
                return false;
            }
        } else if (!record.equals(other.record)) {
            return false;
        }
        return true;
    }

}
