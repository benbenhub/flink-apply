package com.ben.core.util;

import com.ben.core.connector.http.HttpUrlAccessFunction;
import com.ben.core.connector.http.HttpUrlOutputBase;
import org.apache.flink.configuration.Configuration;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class HttpClientUtil {

    private final CloseableHttpClient http_client;

    private final HttpUrlOutputBase httpUrlOutputBase;
    private final HttpUrlAccessFunction httpUrlAccessFunction;

    public HttpClientUtil(Configuration scpsConfig) {
        httpUrlOutputBase = new HttpUrlOutputBase(scpsConfig);
        http_client = httpUrlOutputBase.createCloseableHttpClient();

        httpUrlAccessFunction = new HttpUrlAccessFunction();
    }

    public void close() throws IOException {
        if (http_client != null)
            http_client.close();
        if (httpUrlOutputBase != null)
            httpUrlOutputBase.close();
    }
    /**
     * 通过连接池获取HttpClient
     *
     * @return HttpClient
     */
    public CloseableHttpClient getHttpClient() {
        return http_client;
    }
    public HttpUrlAccessFunction getHttpUrlAccessFunction() {
        return httpUrlAccessFunction;
    }
}
