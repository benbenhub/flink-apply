package com.ben.core.connector.http;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.constance.ConfigKeys;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author learn
 * @date 2022/9/28 15:56
 */
@Slf4j
public class HttpUrlOutputFormat extends RichOutputFormat<Row> {

    private OkHttpClient client;
    private MediaType mediaType;
    Configuration parameters;
    @Override
    public void configure(Configuration p) {
        // 无操作
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        // 创建http连接
        // client = new OkHttpClient().newBuilder().build();
        client = new OkHttpClient.Builder()
                .connectTimeout(parameters.getInteger(ConfigKeys.httpclient_connect_timeout), TimeUnit.MILLISECONDS) // 设置连接超时时间
                .readTimeout(parameters.getInteger(ConfigKeys.httpclient_socket_timeout), TimeUnit.MILLISECONDS)    // 设置读取超时时间
                .build();
        mediaType = MediaType.parse("application/json");
    }

    @Override
    public void writeRecord(Row row) {
        // 调用url操作数据
        String url = Objects.toString(row.getField(0));
        try {
            String jsonData = JSONObject.toJSON(row.getField(1)).toString();
            RequestBody body = RequestBody.create(mediaType, jsonData);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            log.info("Http 响应结果：{}", response.toString());

            // 关闭响应体
            if (response.body() != null) {
                response.body().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {

    }
}
