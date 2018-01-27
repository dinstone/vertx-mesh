
package com.dinstone.mesh.vertx.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dinstone.mesh.vertx.message.MessageCodec;
import com.dinstone.mesh.vertx.message.ServiceRequest;
import com.dinstone.mesh.vertx.message.ServiceResponse;

import io.vertx.core.buffer.Buffer;

public class ServiceMessageCodecTest {

    @Test
    public void testRequest() {
        ServiceRequest r = new ServiceRequest();
        r.setService("service-a");
        r.setMethod("Get");
        r.setPath("/asdasd/sdf?sdfsdf");
        r.setContent(new byte[] { 32, 56 });

        Buffer b = MessageCodec.encode(r);

        ServiceRequest t = MessageCodec.decode(b, ServiceRequest.class);

        assertEquals(r.getService(), t.getService());

    }

    @Test
    public void testResponse() {
        ServiceResponse r = new ServiceResponse();
        r.setStatus(500);
        r.setMessage("error");
        r.setContent(new byte[] { 32, 56 });

        Buffer b = MessageCodec.encode(r);

        ServiceResponse t = MessageCodec.decode(b, ServiceResponse.class);
        assertEquals(r.getStatus(), t.getStatus());
    }

}
