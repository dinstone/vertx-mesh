
package com.dinstone.mesh.vertx.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

public class AccessLogHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger("access-log");

    private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    @Override
    public void handle(RoutingContext context) {
        long timestamp = System.currentTimeMillis();
        HttpServerRequest request = context.request();
        String uri = request.uri();
        HttpMethod method = request.method();
        HttpVersion version = request.version();
        String remoteClient = getClientAddress(request.remoteAddress());
        context.addBodyEndHandler(v -> log(context, timestamp, remoteClient, version, method, uri));

        context.next();
    }

    private String getClientAddress(SocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        return inetSocketAddress.host();
    }

    private void log(RoutingContext context, long timestamp, String remoteClient, HttpVersion version,
            HttpMethod method, String uri) {
        long timeMs = System.currentTimeMillis() - timestamp;

        HttpServerRequest request = context.request();
        long contentLength = request.response().bytesWritten();
        String versionFormatted = "-";
        switch (version) {
            case HTTP_1_0:
                versionFormatted = "HTTP/1.0";
                break;
            case HTTP_1_1:
                versionFormatted = "HTTP/1.1";
                break;
            case HTTP_2:
                versionFormatted = "HTTP/2.0";
                break;
        }

        int status = request.response().getStatusCode();
        String referrer = request.headers().get("referrer");
        String userAgent = request.headers().get("user-agent");
        referrer = (referrer == null ? "-" : referrer);
        userAgent = (userAgent == null ? "-" : userAgent);

        String message = String.format("%s - - [%s] \"%s %s %s\" %d %d \"%s\" \"%s\" %d", remoteClient,
            dateTimeFormat.format(new Date(timestamp)), method, uri, versionFormatted, status, contentLength, referrer,
            userAgent, timeMs);

        doLog(status, message);
    }

    protected void doLog(int status, String message) {
        LOG.info(message);
    }

}
