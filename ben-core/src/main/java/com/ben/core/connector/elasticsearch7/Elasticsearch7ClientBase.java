package com.ben.core.connector.elasticsearch7;

import com.ben.core.constance.ConfigKeys;
import org.apache.flink.configuration.Configuration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * @author learn
 * @date 2023/1/31 15:56
 */
public class Elasticsearch7ClientBase {

    public static RestClientBuilder getRestClientBuilder(HttpHost[] hosts, Configuration parameters) throws Exception {
        return new Elasticsearch7ClientBase().createRestClientBuilder(hosts, parameters);
    }

    private RestClientBuilder createRestClientBuilder(HttpHost[] hosts, Configuration parameters) throws Exception {
        RestClientBuilder builder = RestClient.builder(hosts);

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, null);
        // 账户密码
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                parameters.get(ConfigKeys.elasticsearch_username), parameters.get(ConfigKeys.elasticsearch_password)));
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置请求响应超时时间
                .setSocketTimeout(parameters.get(ConfigKeys.elasticsearch_socket_time_out))
                // 设置请求连接超时时间
                .setConnectTimeout(parameters.get(ConfigKeys.elasticsearch_connect_time_out))
                .build();
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setSSLContext(sslContext);
            httpClientBuilder.setSSLStrategy(new SSLIOSessionStrategy(sslContext, new AllowAllHostnameVerifier()));
            httpClientBuilder.setMaxConnTotal(parameters.get(ConfigKeys.elasticsearch_max_conn_total));
            httpClientBuilder.setMaxConnPerRoute(parameters.get(ConfigKeys.elasticsearch_max_conn_per_route));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            httpClientBuilder.setDefaultRequestConfig(requestConfig);
            return httpClientBuilder;
        });

        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(parameters.get(ConfigKeys.elasticsearch_connect_time_out));
            requestConfigBuilder.setSocketTimeout(parameters.get(ConfigKeys.elasticsearch_socket_time_out));
            requestConfigBuilder.setConnectionRequestTimeout(parameters.get(ConfigKeys.elasticsearch_connection_request_time_out));
            return requestConfigBuilder;
        });
        return builder;
    }

}
