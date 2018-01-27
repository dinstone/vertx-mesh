
package com.dinstone.mesh.vertx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.mesh.vertx.verticle.HttpServerVerticle;
import com.dinstone.mesh.vertx.verticle.MeshVerticleFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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

        // deploy verticle
        int instances = Runtime.getRuntime().availableProcessors();
        DeploymentOptions hvOptions = new DeploymentOptions().setConfig(config).setInstances(instances);
        VertxHelper.deployVerticle(vertx, hvOptions, factory, factory.verticleName(HttpServerVerticle.class));
    }

    private VertxOptions loadVertxOptions(JsonObject vertxConfig) {
        JsonObject config = vertxConfig.getJsonObject("vertx.cluster", new JsonObject());
        JsonObject zkConfig = new JsonObject();
        zkConfig.put("zookeeperHosts", config.getString("zk.hosts"));
        zkConfig.put("rootPath", config.getString("zk.root.path", "vertx/mesh"));
        JsonObject defRetry = new JsonObject().put("initialSleepTime", 1000).put("maxTimes", 3);
        zkConfig.put("retry", config.getJsonObject("zk.retry", defRetry));

        ClusterManager clusterManager = new ZookeeperClusterManager(zkConfig);
        VertxOptions vertxOptions = new VertxOptions().setClustered(true).setClusterManager(clusterManager);

        int blockedThreadCheckInterval = vertxConfig.getInteger("vertx.blockedThreadCheckInterval", 0);
        if (blockedThreadCheckInterval > 0) {
            vertxOptions.setBlockedThreadCheckInterval(blockedThreadCheckInterval);
        }
        return vertxOptions;
    }

    public void stop() {
        if (vertx != null) {
            vertx.close();
        }
    }

}
