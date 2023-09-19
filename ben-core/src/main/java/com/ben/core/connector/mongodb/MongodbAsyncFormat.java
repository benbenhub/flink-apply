package com.ben.core.connector.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.ben.core.constance.ConfigKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * @author learn
 * @date 2023/8/9 13:54
 */
@Slf4j
public abstract class MongodbAsyncFormat<IN,OUT> extends RichAsyncFunction<IN,OUT> {

    private MongoClient mongoClient;
    private MongoCollection mongoCollection;
    private int nThreads;
    private ExecutorService executorService;
    private final String database;
    private final String collection;

    public MongodbAsyncFormat(String database, String collection) {
        this.database = database;
        this.collection = collection;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        nThreads = parameters.getInteger(ConfigKeys.mongodb_async_threads_num);
        mongoClient = MongodbClientBase.getClient(parameters, database);
        // 创建集合
        mongoCollection = mongoClient.getDatabase(database).getCollection(collection);
        executorService = Executors.newFixedThreadPool(nThreads);
    }
    @Override
    public void close() throws SQLException {
        if (mongoClient != null)
            mongoClient.close();
    }

    @Override
    public void asyncInvoke(IN input, ResultFuture<OUT> resultFuture) throws Exception {

        Future<OUT> dbResult = executorService.submit(() -> asyncInvokeInput(input, mongoCollection));

        CompletableFuture.supplyAsync( () -> {
            try {
                return dbResult.get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("Mongodb async error, input data:\n{}\n", input.toString());
                e.printStackTrace();
                return null;
            }
        }).thenAccept( (OUT data) -> {
            if(data != null) {
                resultFuture.complete(Collections.singleton(data));
            } else {
                resultFuture.complete(Collections.emptySet());
            }
        });
    }

    public abstract OUT asyncInvokeInput(IN input, MongoCollection mongoCollection) throws Exception;

    /***
     * 处理单条数据 超过设置的时间时调用此函数
     * 一般的超时原因多数为asyncInvokeInputHandle方法内部bug或者返回值为null
     * @param input
     * @param resultFuture
     */
    @Override
    public void timeout(IN input, ResultFuture<OUT> resultFuture) {
        resultFuture.completeExceptionally(new RuntimeException(" Mongodb op timeout "));
    }
}
