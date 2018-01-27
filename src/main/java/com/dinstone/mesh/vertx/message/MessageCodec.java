
package com.dinstone.mesh.vertx.message;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class MessageCodec {

    public static <T> T decode(Buffer buffer, Class<T> c) {
        return Json.decodeValue(buffer, c);
    }

    public static Buffer encode(Object message) {
        return Json.encodeToBuffer(message);
    }

}
