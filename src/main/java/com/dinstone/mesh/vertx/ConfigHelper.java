
package com.dinstone.mesh.vertx;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class ConfigHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigHelper.class);

    public static JsonObject loadConfig(String resourceLocation) {
        BufferedReader reader = null;
        try {
            InputStream resourceStream = getResourceStream(resourceLocation);
            if (resourceStream == null) {
                resourceStream = new FileInputStream(resourceLocation);
            }

            reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(resourceStream), "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }

            return new JsonObject(sb.toString());
        } catch (IOException e) {
            LOG.error("failed to load config : " + resourceLocation, e);
            throw new RuntimeException("failed to load config : " + resourceLocation, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static InputStream getResourceStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigHelper.class.getClassLoader();
        }
        return classLoader.getResourceAsStream(resource);
    }

}
