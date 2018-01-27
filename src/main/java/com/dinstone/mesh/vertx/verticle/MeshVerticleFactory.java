
package com.dinstone.mesh.vertx.verticle;

import com.dinstone.mesh.vertx.handler.ServiceHandler;
import com.dinstone.mesh.vertx.registry.ServiceRegistry;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;

public class MeshVerticleFactory implements VerticleFactory {

    private JsonObject config;

    private ServiceHandler serviceHandler;

    public MeshVerticleFactory(Vertx vertx, JsonObject config) {
        this.config = config;
        ServiceRegistry serviceRegistry = new ServiceRegistry(vertx);
        this.serviceHandler = new ServiceHandler(vertx, serviceRegistry);

    }

    @Override
    public String prefix() {
        return "mesh";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        verticleName = VerticleFactory.removePrefix(verticleName);
        if (HttpServerVerticle.class.getName().equals(verticleName)) {
            return new HttpServerVerticle(config, serviceHandler);
        }
        return null;
    }

    public String verticleName(Class<?> verticleClass) {
        return prefix() + ":" + verticleClass.getName();
    }

}
