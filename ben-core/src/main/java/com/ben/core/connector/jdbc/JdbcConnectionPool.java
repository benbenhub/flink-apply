package com.ben.core.connector.jdbc;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author learn
 * @date 2022/7/29 14:01
 */
public class JdbcConnectionPool {

    private transient ExecutorService executorService;
    private transient DruidDataSource dataSource;

    /*静态初始化*/
    public JdbcConnectionPool(JdbcConnectionBuilder jdbcConnectBuilder) throws Exception {
       initCurrencyJdbc(jdbcConnectBuilder);
    }

    public Connection getConnection() throws SQLException {
/*        if(dataSource.getConnectCount() == maxPoolConn){
            dataSource.getConnection(dataSource.getMaxWait());

        }*/
        return dataSource.getConnection();
    }
    public ExecutorService getExecutorService() {
        return executorService;
    }
    public void close(){
        if(executorService != null && !executorService.isShutdown())
            executorService.shutdownNow();
        if(dataSource != null)
            dataSource.close();
    }
    public static JdbcConnectionBuilder buildJdbcConnectionPool() {
        return new JdbcConnectionBuilder();
    }


    private void initCurrencyJdbc(JdbcConnectionBuilder jdbcConnectBuilder){
        //创建线程池
        executorService = Executors.newFixedThreadPool(jdbcConnectBuilder.getMaxPoolConn());
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(jdbcConnectBuilder.getDriverName());
        dataSource.setUsername(jdbcConnectBuilder.getUsername());
        dataSource.setPassword(jdbcConnectBuilder.getPassword());
        dataSource.setUrl(jdbcConnectBuilder.getJdbcUrl());
        dataSource.setMaxActive(jdbcConnectBuilder.getMaxPoolConn());
        dataSource.setInitialSize(jdbcConnectBuilder.getInitialSize());
        dataSource.setMinIdle(jdbcConnectBuilder.getMinIdle());
        dataSource.setMaxWait(jdbcConnectBuilder.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(jdbcConnectBuilder.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(jdbcConnectBuilder.getMinEvictableIdleTimeMillis());
        dataSource.setMaxEvictableIdleTimeMillis(jdbcConnectBuilder.getMaxEvictableIdleTimeMillis());
        dataSource.setPhyTimeoutMillis(jdbcConnectBuilder.getPhyTimeoutMillis());
        dataSource.setValidationQuery("select 1");
        dataSource.setTestWhileIdle(jdbcConnectBuilder.getTestWhileIdle());
        dataSource.setUseGlobalDataSourceStat(true);
        dataSource.setKeepAlive(jdbcConnectBuilder.getKeepAlive());
        dataSource.setKeepAliveBetweenTimeMillis(jdbcConnectBuilder.getKeepAliveBetweenTimeMillis());
        dataSource.setUsePingMethod(jdbcConnectBuilder.getUsePingMethod());

    }
}
