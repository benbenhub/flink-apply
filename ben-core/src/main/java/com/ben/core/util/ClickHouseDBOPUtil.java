package com.ben.core.util;

import com.ben.core.connector.jdbc.JdbcConnectionPool;
import com.ben.core.connector.jdbc.JdbcStatementFunction;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.configuration.Configuration;

/**
 * @author learn
 * @date 2022/3/24 13:54
 */
public class ClickHouseDBOPUtil {

    private JdbcConnectionPool jdbcConnect;
    private JdbcStatementFunction jdbcQueryFunction;

    public ClickHouseDBOPUtil(Configuration scpsConfig) throws Exception {
        jdbcConnect = JdbcConnectionPool.buildJdbcConnectionPool()
                .setJdbcUrl(String.format("jdbc:clickhouse://%s:%s/%s?%s",
                        scpsConfig.get(ConfigKeys.clickhouse_host)
                        , scpsConfig.get(ConfigKeys.clickhouse_port)
                        , scpsConfig.get(ConfigKeys.clickhouse_database)
                        , "socket_timeout="
                                + scpsConfig.get(ConfigKeys.clickhouse_socket_timeout))
                ).setUserName(scpsConfig.get(ConfigKeys.clickhouse_username))
                .setPassword(scpsConfig.get(ConfigKeys.clickhouse_password))
                .setDriverName(scpsConfig.get(ConfigKeys.clickhouse_driver))
                .setMaxPoolConn(scpsConfig.getInteger(ConfigKeys.clickhouse_maximumConnection))
                .setMinEvictableIdleTimeMillis(scpsConfig.getInteger(ConfigKeys.clickhouse_min_evictable_idle_time_millis))
                .setMaxEvictableIdleTimeMillis(scpsConfig.getInteger(ConfigKeys.clickhouse_max_evictable_idle_time_millis))
                .setKeepAliveBetweenTimeMillis(scpsConfig.getInteger(ConfigKeys.clickhouse_keep_alive_between_time_millis))
                .finish();
        jdbcQueryFunction = new JdbcStatementFunction(jdbcConnect);
    }

    public JdbcStatementFunction queryFunction(){
        return jdbcQueryFunction;
    }
    public void close(){
        if(jdbcConnect != null)
            jdbcConnect.close();
    }
}
