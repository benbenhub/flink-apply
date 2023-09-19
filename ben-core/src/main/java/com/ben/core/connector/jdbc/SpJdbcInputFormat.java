package com.ben.core.connector.jdbc;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcInputFormat;

/**
 * @author learn
 * @date 2023/2/1 10:23
 */
public class SpJdbcInputFormat extends ConfigBase {
    public SpJdbcInputFormat(Configuration scpsConfig) {
        super(scpsConfig);
    }
    public JdbcInputFormat createMySqlConnect(String sql, RowTypeInfo rowTypeInfo) {
        return new JdbcInputFormat.JdbcInputFormatBuilder()
                .setDBUrl(String.format("jdbc:mysql://%s:%s/%s?%s",
                        this.scpsConfig.get(ConfigKeys.mysql_host)
                        , this.scpsConfig.get(ConfigKeys.mysql_port)
                        , this.scpsConfig.get(ConfigKeys.mysql_database)
                        , "socket_timeout=" + this.scpsConfig.get(ConfigKeys.mysql_socket_timeout)))
                .setDrivername(this.scpsConfig.get(ConfigKeys.mysql_driver))
                .setUsername(this.scpsConfig.get(ConfigKeys.mysql_username))
                .setPassword(this.scpsConfig.get(ConfigKeys.mysql_password))
                .setQuery(sql)
                .setRowTypeInfo(rowTypeInfo)
                .finish();
    }

    public JdbcInputFormat createClickhouseConnect(String sql, RowTypeInfo rowTypeInfo) {
        return new JdbcInputFormat.JdbcInputFormatBuilder()
                .setDBUrl(String.format("jdbc:clickhouse://%s:%s/%s?%s",
                        this.scpsConfig.get(ConfigKeys.clickhouse_host)
                        , this.scpsConfig.get(ConfigKeys.clickhouse_port)
                        , this.scpsConfig.get(ConfigKeys.clickhouse_database)
                        , "socket_timeout=" + this.scpsConfig.get(ConfigKeys.clickhouse_socket_timeout)))
                .setDrivername(this.scpsConfig.get(ConfigKeys.clickhouse_driver))
                .setUsername(this.scpsConfig.get(ConfigKeys.clickhouse_username))
                .setPassword(this.scpsConfig.get(ConfigKeys.clickhouse_password))
                .setQuery(sql)
                .setRowTypeInfo(rowTypeInfo)
                .finish();
    }

    public JdbcInputFormat createDorisConnect(String sql, RowTypeInfo rowTypeInfo) {
        return new JdbcInputFormat.JdbcInputFormatBuilder()
                .setDBUrl(String.format("jdbc:mysql://%s:%s/%s?%s",
                        this.scpsConfig.get(ConfigKeys.doris_host)
                        , this.scpsConfig.get(ConfigKeys.doris_port)
                        , this.scpsConfig.get(ConfigKeys.doris_database)
                        , "socket_timeout="
                                + this.scpsConfig.get(ConfigKeys.doris_socket_timeout)))
                .setDrivername(this.scpsConfig.get(ConfigKeys.doris_driver))
                .setUsername(this.scpsConfig.get(ConfigKeys.doris_username))
                .setPassword(this.scpsConfig.get(ConfigKeys.doris_password))
                .setQuery(sql)
                .setRowTypeInfo(rowTypeInfo)
                .finish();
    }
}
