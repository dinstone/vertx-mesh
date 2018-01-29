
package com.dinstone.mesh.vertx.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpProxyTest {

    public static void main(String[] args) throws Exception {
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8484");

        URL url = new URL("http://magnet/info?asd=sd&sdfg=Rasdf");
        System.out.println("host = " + url.getHost());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "agent");
        conn.setUseCaches(false);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        StringBuilder sb = new StringBuilder();
        String strRead = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        while ((strRead = reader.readLine()) != null) {
            sb.append(strRead);
        }
        String rs = sb.toString();

        System.out.println(rs);

        reader.close();
        conn.disconnect();
    }

}
