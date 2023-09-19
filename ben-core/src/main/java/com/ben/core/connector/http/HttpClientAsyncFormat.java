package com.ben.core.connector.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class HttpClientAsyncFormat<IN,OUT> extends RichAsyncFunction<IN,OUT> {

    private CloseableHttpAsyncClient http_client = null;
    protected HttpUrlAccessFunction httpUrlAccessFunction;

    @Override
    public void open(Configuration parameters) throws Exception {

        parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();

        http_client = new HttpUrlOutputBase(parameters).createCloseableHttpAsyncClient();

        httpUrlAccessFunction = new HttpUrlAccessFunction();
    }

    @Override
    public void close() throws Exception {
        if( http_client != null)
            http_client.close();
    }

    @Override
    public void timeout(IN input, ResultFuture<OUT> resultFuture) throws Exception {
        resultFuture.completeExceptionally(new RuntimeException(" HttpClient async op timeout "));
    }

    @Override
    public void asyncInvoke(IN in, ResultFuture<OUT> resultFuture) throws Exception {
        http_client.start();
        Future<HttpResponse> dbResult = http_client.execute(asyncInvokeInputHandle(in),new FutureCallback<HttpResponse>(){

            @Override
            public void completed(HttpResponse httpResponse) {
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void cancelled() {
            }
        });


        CompletableFuture.supplyAsync( () -> {
            try {
                return dbResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept( (HttpResponse data) -> {
            if(data != null) {
                try {
                    String res = EntityUtils.toString(data.getEntity(), StandardCharsets.UTF_8);
                    OUT resultMap = (OUT) JSONObject.parse(res);
                    resultFuture.complete(Collections.singleton(resultMap));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public abstract HttpRequestBase asyncInvokeInputHandle(IN in) throws Exception;


}
