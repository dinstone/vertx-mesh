
package com.dinstone.mesh.vertx.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AbstractHandler {

    public AbstractHandler() {
        super();
    }

    protected void success(RoutingContext ctx) {
        success(ctx, null);
    }

    protected void success(RoutingContext ctx, JsonObject result) {
        JsonObject res = new JsonObject().put("code", "1").put("message", "ok");
        if (result != null) {
            res.put("result", result);
        }
        ctx.response().end(res.encode());
    }

    protected void failed(RoutingContext ctx, String message) {
        JsonObject res = new JsonObject().put("code", "-1").put("message", message);
        ctx.response().setStatusCode(503).end(res.encode());
    }

    protected void failed(RoutingContext ctx, Throwable throwable) {
        JsonObject res = new JsonObject().put("code", "-1").put("message",
            throwable == null ? "" : throwable.getMessage());
        ctx.response().setStatusCode(503).end(res.encode());
    }

}