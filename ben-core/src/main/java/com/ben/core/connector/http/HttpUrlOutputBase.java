package com.ben.core.connector.http;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.configuration.Configuration;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.concurrent.TimeUnit;

/**
 * @author learn
 * @date 2023/2/1 11:39
 */
public class HttpUrlOutputBase extends ConfigBase {

    private PoolingHttpClientConnectionManager poolingConnectionManager;

    public HttpUrlOutputBase(Configuration scpsConfig) {
        super(scpsConfig);
    }

    public void close(){
        if(poolingConnectionManager != null)
            poolingConnectionManager.close();
    }
    public CloseableHttpClient createCloseableHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置客户端和服务端建立连接的超时时间
                .setConnectTimeout(scpsConfig.get(ConfigKeys.httpclient_connect_timeout))
                // 设置客户端从服务端读取数据的超时时间
                .setSocketTimeout(scpsConfig.get(ConfigKeys.httpclient_socket_timeout))
                // 设置从连接池获取连接的超时时间，不宜过长
                .setConnectionRequestTimeout(scpsConfig.get(ConfigKeys.httpclient_connection_request_timeout))
                .build();

        // Httpclient连接池，长连接保持30秒 httpclient_time_to_live
        poolingConnectionManager =
                new PoolingHttpClientConnectionManager(scpsConfig.get(ConfigKeys.httpclient_time_to_live), TimeUnit.SECONDS);
        // 连接池最大连接数
        poolingConnectionManager.setMaxTotal(scpsConfig.get(ConfigKeys.httpclient_max_total));
        // 设置同路由的并发数
        poolingConnectionManager.setDefaultMaxPerRoute(scpsConfig.get(ConfigKeys.httpclient_default_max_per_route));
        // 可用空闲连接过期时间
        poolingConnectionManager.setValidateAfterInactivity(scpsConfig.get(ConfigKeys.httpclient_validate_after_inactivity));

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .build();
    }
    public CloseableHttpAsyncClient createCloseableHttpAsyncClient() throws IOReactorException {
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置客户端和服务端建立连接的超时时间
                .setConnectTimeout(scpsConfig.get(ConfigKeys.httpclient_connect_timeout))
                // 设置客户端从服务端读取数据的超时时间
                .setSocketTimeout(scpsConfig.get(ConfigKeys.httpclient_socket_timeout))
                // 设置从连接池获取连接的超时时间，不宜过长
                .setConnectionRequestTimeout(scpsConfig.get(ConfigKeys.httpclient_connection_request_timeout))
                .build();

        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setConnectTimeout(scpsConfig.getInteger(ConfigKeys.httpclient_connect_timeout))
                .setSoTimeout(scpsConfig.getInteger(ConfigKeys.httpclient_socket_timeout))
                .build();

        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        // 连接池最大连接数
        connManager.setMaxTotal(scpsConfig.getInteger(ConfigKeys.httpclient_max_total));
        // 设置同路由的并发数
        connManager.setDefaultMaxPerRoute(scpsConfig.getInteger(ConfigKeys.httpclient_default_max_per_route));


        return HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .build();
    }
}
