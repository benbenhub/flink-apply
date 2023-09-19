package com.ben.core.connector.cdc;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import com.ververica.cdc.connectors.mongodb.source.MongoDBSource;
import com.ververica.cdc.connectors.mongodb.source.MongoDBSourceBuilder;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.source.MySqlSourceBuilder;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.configuration.Configuration;

/**
 * @author learn
 * @date 2023/2/1 11:17
 */
public class FlinkcdcConnectorFormat extends ConfigBase {
    /**
     * com.ververica.cdc.connectors.mysql.source.MySqlSource 新
     * com.ververica.cdc.connectors.mysql.MySqlSource 旧
     * @param scpsConfig
     */
    public FlinkcdcConnectorFormat(Configuration scpsConfig) {
        super(scpsConfig);
    }
//    @Deprecated
//    public <T> DebeziumSourceFunction<T> createMysqlCdc(StartupOptions ss, DebeziumDeserializationSchema<T> deserializer){
//        return MySqlSource.<T>builder()
//                .hostname(this.scpsConfig.get(ConfigKeys.binlog_host))
//                .port(this.scpsConfig.getInteger(ConfigKeys.binlog_port))
//                .databaseList(this.scpsConfig.get(ConfigKeys.binlog_database_list).split(";"))
//                .tableList(this.scpsConfig.get(ConfigKeys.binlog_table_list).split(";"))
//                .username(this.scpsConfig.get(ConfigKeys.binlog_username))
//                .password(this.scpsConfig.get(ConfigKeys.binlog_password))
//                .deserializer(deserializer)
//                .startupOptions(ss)
//                .serverId(this.scpsConfig.getInteger(ConfigKeys.binlog_server_id))
//                .build();
//    }


    public <T> MySqlSource<T> createMysqlCdcNew(StartupOptions ss, DebeziumDeserializationSchema<T> deserializer){

        MySqlSourceBuilder<T> mySqlSourceBuilder = MySqlSource.<T>builder()
                .hostname(this.scpsConfig.get(ConfigKeys.binlog_host))
                .port(this.scpsConfig.getInteger(ConfigKeys.binlog_port))
                .databaseList(this.scpsConfig.get(ConfigKeys.binlog_database_list).split(";"))
                .tableList(this.scpsConfig.get(ConfigKeys.binlog_table_list).split(";"))
                .username(this.scpsConfig.get(ConfigKeys.binlog_username))
                .password(this.scpsConfig.get(ConfigKeys.binlog_password))
                .deserializer(deserializer)
                .startupOptions(ss)
                .scanNewlyAddedTableEnabled(this.scpsConfig.getBoolean(ConfigKeys.binlog_scan_newly_added_table_enabled))
                .includeSchemaChanges(this.scpsConfig.getBoolean(ConfigKeys.binlog_include_schema_changes));

        String serverId = this.scpsConfig.getString(ConfigKeys.binlog_server_id);
        if(serverId != null && !serverId.equals("")) {
            mySqlSourceBuilder = mySqlSourceBuilder.serverId(serverId);
        }

        return mySqlSourceBuilder.build();
    }

    public <T> MongoDBSource<T> createMongodbCdc(DebeziumDeserializationSchema<T> deserializer){
        MongoDBSourceBuilder<T> mongoDBSourceBuilder =  MongoDBSource.<T>builder()
                .hosts(this.scpsConfig.get(ConfigKeys.mongodb_host) + ":" + this.scpsConfig.get(ConfigKeys.mongodb_port))
                .username(this.scpsConfig.get(ConfigKeys.mongodb_username))
                .password(this.scpsConfig.get(ConfigKeys.mongodb_password))
                .databaseList(this.scpsConfig.get(ConfigKeys.mongodb_binlog_database_list).split(";"))
                .collectionList(this.scpsConfig.get(ConfigKeys.mongodb_binlog_table_list).split(";"))
                .deserializer(deserializer)
                .copyExisting(false);
        return mongoDBSourceBuilder.build();
    }

}
