package com.ben.core.connector.jdbc;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import com.ben.core.enums.JdbcConnectionType;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author learn
 * @date 2023/2/1 10:46
 */
public class JdbcSinkFormat extends ConfigBase {
    public JdbcSinkFormat(Configuration scpsConfig) {
        super(scpsConfig);
    }
    public <T> SinkFunction<T> createJdbcSink(String sql, JdbcStatementBuilder<T> jsb , JdbcConnectionType sourceType){
        return JdbcSink.sink(sql, jsb
                , getJdbcExecutionOptions(sourceType)
                , getJdbcConnectionOptions(sourceType));
    }
    private JdbcExecutionOptions getJdbcExecutionOptions(JdbcConnectionType sourceType) {
        switch (sourceType) {
            case mysql:
                return JdbcExecutionOptions.builder()
                        .withBatchSize(this.scpsConfig.getInteger(ConfigKeys.mysql_batch_size))
                        .withBatchIntervalMs(this.scpsConfig.getLong(ConfigKeys.mysql_batch_interval))
                        .withMaxRetries(this.scpsConfig.getInteger(ConfigKeys.mysql_max_retry))
                        .build();
            case doris:
                return JdbcExecutionOptions.builder()
                    .withBatchSize(this.scpsConfig.getInteger(ConfigKeys.doris_batch_size))
                    .withBatchIntervalMs(this.scpsConfig.getLong(ConfigKeys.doris_batch_interval))
                    .withMaxRetries(this.scpsConfig.getInteger(ConfigKeys.doris_max_retry))
                    .build();
            case clickhouse:
            default:
                return JdbcExecutionOptions.builder()
                        .withBatchSize(this.scpsConfig.getInteger(ConfigKeys.clickhouse_batch_size))
                        .withBatchIntervalMs(this.scpsConfig.getLong(ConfigKeys.clickhouse_batch_interval))
                        .withMaxRetries(this.scpsConfig.getInteger(ConfigKeys.clickhouse_max_retry))
                        .build();
        }
    }

    private JdbcConnectionOptions getJdbcConnectionOptions(JdbcConnectionType sourceType) {
        switch (sourceType) {
            case mysql:
                return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://" + this.scpsConfig.get(ConfigKeys.mysql_host) + ":"
                                + this.scpsConfig.get(ConfigKeys.mysql_port))
                        .withDriverName(this.scpsConfig.get(ConfigKeys.mysql_driver))
                        .withUsername(this.scpsConfig.get(ConfigKeys.mysql_username))
                        .withPassword(this.scpsConfig.get(ConfigKeys.mysql_password))
                        .build();
            case doris:
                return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://" + this.scpsConfig.get(ConfigKeys.doris_host) + ":"
                                + this.scpsConfig.get(ConfigKeys.doris_port))
                        .withDriverName(this.scpsConfig.get(ConfigKeys.doris_driver))
                        .withUsername(this.scpsConfig.get(ConfigKeys.doris_username))
                        .withPassword(this.scpsConfig.get(ConfigKeys.doris_password))
                        .build();
            case clickhouse:
            default:
                return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:clickhouse://" + this.scpsConfig.get(ConfigKeys.clickhouse_host) + ":"
                                + this.scpsConfig.get(ConfigKeys.clickhouse_port))
                        .withDriverName(this.scpsConfig.get(ConfigKeys.clickhouse_driver))
                        .withUsername(this.scpsConfig.get(ConfigKeys.clickhouse_username))
                        .withPassword(this.scpsConfig.get(ConfigKeys.clickhouse_password))
                        .build();
        }
    }
}
