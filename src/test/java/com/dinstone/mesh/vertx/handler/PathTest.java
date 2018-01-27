
package com.dinstone.mesh.vertx.handler;

public class PathTest {

    public static void main(String[] args) {
        String uri = "/magnet/api/info?asd=sd&sdfg=Rasdf";
        int index = uri.indexOf('/', 1);
        System.out.println("service: " + uri.substring(1, index));
        System.out.println("path: " + uri.substring(index));
    }

}
