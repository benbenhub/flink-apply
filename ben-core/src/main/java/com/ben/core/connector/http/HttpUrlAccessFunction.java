package com.ben.core.connector.http;

import org.apache.commons.collections.MapUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * @author learn
 * @date 2023/2/1 14:09
 */
public class HttpUrlAccessFunction {
    public HttpRequestBase get(String url) throws Exception {
        return get(url, Collections.emptyMap(), Collections.emptyMap());
    }

    public HttpRequestBase get(String url, Map<String, Object> params) throws Exception {
        return get(url, Collections.emptyMap(), params);
    }

    public HttpRequestBase get(String url, Map<String, Object> headers, Map<String, Object> params)
            throws Exception {
        URIBuilder ub = new URIBuilder(url);
        if (MapUtils.isNotEmpty(params)) {
            ArrayList<NameValuePair> pairs = buildParameters(params);
            ub.setParameters(pairs);
        }
        HttpGet httpGet = new HttpGet(ub.build());
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }
        return httpGet;
    }

    public HttpRequestBase post(String url) {
        return new HttpPost(url);
    }

    public HttpRequestBase post(String url, String jsonStr) {
        return post(url, Collections.emptyMap(), jsonStr);
    }

    public HttpRequestBase post(String url, Map<String, Object> headers, String jsonStr) {

        HttpPost httpPost = new HttpPost(url);
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }
        StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
        entity.setContentEncoding(StandardCharsets.UTF_8.toString());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        return httpPost;
    }

    public HttpRequestBase post(String url, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = buildParameters(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        return httpPost;
    }

    public HttpRequestBase post(String url, Map<String, Object> headers, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(url);
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }

        ArrayList<NameValuePair> pairs = buildParameters(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        return httpPost;
    }

    private ArrayList<NameValuePair> buildParameters(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return pairs;
    }
}
