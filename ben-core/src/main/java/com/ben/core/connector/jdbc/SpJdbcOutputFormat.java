package com.ben.core.connector.jdbc;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcRowOutputFormat;

/**
 * @author learn
 * @date 2023/2/1 10:12
 */
public class SpJdbcOutputFormat extends ConfigBase {

    public SpJdbcOutputFormat(Configuration scpsConfig) {
        super(scpsConfig);
    }

    public JdbcRowOutputFormat createMySqlConnect(String sql, int[] fieldTypes) {
        return JdbcRowOutputFormat.buildJdbcOutputFormat()
                .setDBUrl(String.format("jdbc:mysql://%s:%s/%s?%s",
                        this.scpsConfig.get(ConfigKeys.mysql_host)
                        , this.scpsConfig.get(ConfigKeys.mysql_port)
                        , this.scpsConfig.get(ConfigKeys.mysql_database)
                        , "socket_timeout="
                                + this.scpsConfig.get(ConfigKeys.mysql_socket_timeout)))
                .setDrivername(this.scpsConfig.get(ConfigKeys.mysql_driver))
                .setUsername(this.scpsConfig.get(ConfigKeys.mysql_username))
                .setPassword(this.scpsConfig.get(ConfigKeys.mysql_password))
                .setQuery(sql)
                .setSqlTypes(fieldTypes)
                .finish();
    }

    public JdbcRowOutputFormat createClickhouseConnect(String sql, int[] fieldTypes) {
        return JdbcRowOutputFormat.buildJdbcOutputFormat()
                .setDBUrl(String.format("jdbc:clickhouse://%s:%s/%s?%s",
                        this.scpsConfig.get(ConfigKeys.clickhouse_host)
                        , this.scpsConfig.get(ConfigKeys.clickhouse_port)
                        , this.scpsConfig.get(ConfigKeys.clickhouse_database)
                        , "socket_timeout="
                                + this.scpsConfig.get(ConfigKeys.clickhouse_socket_timeout)))
                .setDrivername(this.scpsConfig.get(ConfigKeys.clickhouse_driver))
                .setUsername(this.scpsConfig.get(ConfigKeys.clickhouse_username))
                .setPassword(this.scpsConfig.get(ConfigKeys.clickhouse_password))
                .setQuery(sql)
                .setSqlTypes(fieldTypes)
                .finish();
    }
}
