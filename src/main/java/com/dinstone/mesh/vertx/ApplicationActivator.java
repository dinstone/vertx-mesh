
package com.dinstone.mesh.vertx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.verticle.HttpAdminVerticle;
import com.dinstone.mesh.vertx.verticle.HttpApiVerticle;
import com.dinstone.mesh.vertx.verticle.HttpProxyVerticle;
import com.dinstone.mesh.vertx.verticle.MeshVerticleFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

public class ApplicationActivator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationActivator.class);

    private static final String APPLICATION_HOME = "application.home";

    private Vertx vertx;

    public static void main(String[] args) throws IOException {
        // launch application activator
        ApplicationActivator vertxApp = new ApplicationActivator();
        try {
            long s = System.currentTimeMillis();
            vertxApp.start();
            long e = System.currentTimeMillis();
            LOG.info("application startup in {} ms.", (e - s));
        } catch (Exception e) {
            LOG.error("application startup error.", e);
            vertxApp.stop();

            System.exit(-1);
        }
    }

    public ApplicationActivator() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        // init applicationHome dir
        String applicationHome = System.getProperty(APPLICATION_HOME);
        if (applicationHome == null || applicationHome.isEmpty()) {
            applicationHome = System.getProperty("user.dir");
            System.setProperty(APPLICATION_HOME, applicationHome);
        }
    }

    public void start() throws Exception {
        // init config
        String configFile = System.getProperty("config.file");
        if (configFile == null || configFile.length() == 0) {
            configFile = "config.json";
        }
        JsonObject config = ConfigHelper.loadConfig(configFile);

        // init vertx
        vertx = VertxHelper.createVertx(loadVertxOptions(config));

        // init verticle
        MeshVerticleFactory factory = new MeshVerticleFactory(vertx, config);

        // deploy admin verticle
        JsonObject adminConfig = config.getJsonObject("admin");
        DeploymentOptions options = new DeploymentOptions().setConfig(adminConfig);
        VertxHelper.deployVerticle(vertx, options, factory, factory.verticleName(HttpAdminVerticle.class));

        // deploy router verticle
        JsonArray listenArray = config.getJsonArray("routers");
        if (listenArray == null || listenArray.isEmpty()) {
            throw new RuntimeException("routers config is empty");
        }

        for (Object object : listenArray) {
            JsonObject listenConfig = (JsonObject) object;
            String type = listenConfig.getString("label");
            String protocol = listenConfig.getString("protocol");
            if ("http".equalsIgnoreCase(protocol) && "api".equalsIgnoreCase(type)) {
                int instances = Runtime.getRuntime().availableProcessors();
                options = new DeploymentOptions().setConfig(listenConfig).setInstances(instances);
                VertxHelper.deployVerticle(vertx, options, factory, factory.verticleName(HttpApiVerticle.class));
            } else if ("http".equalsIgnoreCase(protocol) && "proxy".equalsIgnoreCase(type)) {
                int instances = Runtime.getRuntime().availableProcessors();
                options = new DeploymentOptions().setConfig(listenConfig).setInstances(instances);
                VertxHelper.deployVerticle(vertx, options, factory, factory.verticleName(HttpProxyVerticle.class));
            } else {
                LOG.warn("unkown listen service [{}].[{}]", protocol, protocol);
            }
        }
    }

    private VertxOptions loadVertxOptions(JsonObject vertxConfig) {
        VertxOptions vertxOptions = new VertxOptions();
        int blockedCheckInterval = vertxConfig.getInteger("vertx.blockedThreadCheckInterval", 0);
        if (blockedCheckInterval > 0) {
            vertxOptions.setBlockedThreadCheckInterval(blockedCheckInterval);
        }

        JsonObject config = vertxConfig.getJsonObject("vertx.cluster");
        if (config != null && config.getString("type") != null) {
            ClusterManager clusterManager = null;

            String type = config.getString("type");
            if ("zookeeper".equalsIgnoreCase(type)) {
                clusterManager = new ZookeeperClusterManager(config);
            }

            if (clusterManager != null) {
                vertxOptions.setClustered(true).setClusterManager(clusterManager);
            } else {
                LOG.warn("unkown cluster type [{}]", type);
            }
        }

        return vertxOptions;
    }

    public void stop() {
        if (vertx != null) {
            vertx.close();
        }
    }

}
