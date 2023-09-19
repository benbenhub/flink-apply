package com.ben.core.connector.jdbc;

import com.ben.core.constance.ConfigKeys;
import com.ben.core.enums.JdbcConnectionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author learn
 * @date 2022/8/3 9:55
 */
@Slf4j
public abstract class JdbcAsyncFormat<IN,OUT> extends RichAsyncFunction<IN,OUT> {

    private JdbcConnectionPool jdbcConnect;
    private JdbcStatementFunction jdbcQueryFunction;
    private JdbcConnectionType jdbcType;
    private ExecutorService executorService;

    public JdbcAsyncFormat(JdbcConnectionType jdbcType){
        this.jdbcType = jdbcType;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        switch (jdbcType){
            case clickhouse:
                initClickhouse(parameters);
                break;
            case doris:
                initDoris(parameters);
                break;
        }

        executorService = jdbcConnect.getExecutorService();
    }
    @Override
    public void close() throws SQLException {
        if(jdbcConnect != null)
            jdbcConnect.close();
    }

    @Override
    public void asyncInvoke(IN input, ResultFuture<OUT> resultFuture) throws Exception {

        Future<OUT> dbResult = executorService.submit(() -> asyncInvokeInput(input,jdbcQueryFunction));

        CompletableFuture.supplyAsync( () -> {
            try {
                return dbResult.get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("JDBC async error, input data:\n{}\n", input.toString());
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

    public OUT asyncInvokeInput(IN input,JdbcStatementFunction jdbcQueryFunction) throws Exception {
        return asyncInvokeInputHandle(input,jdbcQueryFunction);
    }

    /***
     * 处理单条数据 超过设置的时间时调用此函数
     * 一般的超时原因多数为asyncInvokeInputHandle方法内部bug或者返回值为null
     * @param input
     * @param resultFuture
     */
    @Override
    public void timeout(IN input, ResultFuture<OUT> resultFuture) {
        resultFuture.completeExceptionally(new RuntimeException(" ClickHouse op timeout "));

    }


    public JdbcStatementFunction getJdbcQueryFunction(){
        return jdbcQueryFunction;
    }

    public abstract OUT asyncInvokeInputHandle(IN input, JdbcStatementFunction jdbcQueryFunction) throws Exception ;

    private void initClickhouse(Configuration parameters) throws Exception {
        jdbcConnect = JdbcConnectionPool.buildJdbcConnectionPool()
                .setJdbcUrl(String.format("jdbc:clickhouse://%s:%s/%s?%s",
                        parameters.get(ConfigKeys.clickhouse_host)
                        , parameters.get(ConfigKeys.clickhouse_port)
                        , parameters.get(ConfigKeys.clickhouse_database)
                        , "socket_timeout="+parameters.get(ConfigKeys.clickhouse_socket_timeout).toString())
                ).setUserName(parameters.get(ConfigKeys.clickhouse_username))
                .setPassword(parameters.get(ConfigKeys.clickhouse_password))
                .setDriverName(parameters.get(ConfigKeys.clickhouse_driver))
                .setMaxPoolConn(parameters.getInteger(ConfigKeys.clickhouse_maximumConnection))
                .finish();

        jdbcQueryFunction = new JdbcStatementFunction(jdbcConnect);
    }
    private void initDoris(Configuration parameters) throws Exception {
        jdbcConnect = JdbcConnectionPool.buildJdbcConnectionPool()
                .setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&connectTimeout=%s&socketTimeout=%s",
                        parameters.get(ConfigKeys.doris_host)
                        , parameters.get(ConfigKeys.doris_port)
                        , parameters.get(ConfigKeys.doris_database)
                        ,parameters.getInteger(ConfigKeys.connectTimeout)
                        ,parameters.getInteger(ConfigKeys.socketTimeout)
                        )
                ).setUserName(parameters.get(ConfigKeys.doris_username))
                .setPassword(parameters.get(ConfigKeys.doris_password))
                .setDriverName(parameters.get(ConfigKeys.doris_driver))
                .setMaxPoolConn(parameters.getInteger(ConfigKeys.doris_maximumConnection))
                .setInitialSize(parameters.get(ConfigKeys.doris_initialSize))
                .setMinIdle(parameters.getInteger(ConfigKeys.doris_minIdle))
                .setMaxWait(parameters.getInteger(ConfigKeys.doris_maxWait))
                .setTimeBetweenEvictionRunsMillis(parameters.getInteger(ConfigKeys.doris_timeBetweenEvictionRunsMillis))
                .setMinEvictableIdleTimeMillis(parameters.getInteger(ConfigKeys.doris_minEvictableIdleTimeMillis))
                .setMaxEvictableIdleTimeMillis(parameters.getInteger(ConfigKeys.doris_maxEvictableIdleTimeMillis))
                .setPhyTimeoutMillis(parameters.getInteger(ConfigKeys.doris_phyTimeoutMillis))
                .setKeepAlive(parameters.getBoolean(ConfigKeys.doris_keepAlive))
                .setKeepAliveBetweenTimeMillis(parameters.getInteger(ConfigKeys.doris_keepAliveBetweenTimeMillis))
                .setTestWhileIdle(parameters.getBoolean(ConfigKeys.doris_testWhileIdle))
                .setUsePingMethod(parameters.getBoolean(ConfigKeys.doris_usePingMethod))
                .finish();
        jdbcQueryFunction = new JdbcStatementFunction(jdbcConnect);
    }
}
