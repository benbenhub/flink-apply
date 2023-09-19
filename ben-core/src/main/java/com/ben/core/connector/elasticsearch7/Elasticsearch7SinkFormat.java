package com.ben.core.connector.elasticsearch7;

import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.http.HttpHost;

/**
 * @author learn
 * @date 2023/2/1 11:04
 */
public class Elasticsearch7SinkFormat extends ConfigBase {
    public Elasticsearch7SinkFormat(Configuration scpsConfig) {
        super(scpsConfig);
    }
    public <T> ElasticsearchSink<T> createEs7Sink(ElasticsearchEmitter<? super T> emitter){
        return new Elasticsearch7SinkBuilder<T>()
                .setBulkFlushMaxActions(this.scpsConfig.getInteger(ConfigKeys.elasticsearch_bulk_flush_max_actions))
                .setBulkFlushInterval(this.scpsConfig.getLong(ConfigKeys.elasticsearch_bulk_flush_interval))
                .setHosts(new HttpHost(this.scpsConfig.get(ConfigKeys.elasticsearch_host)
                        , this.scpsConfig.getInteger(ConfigKeys.elasticsearch_port)
                        , this.scpsConfig.get(ConfigKeys.elasticsearch_scheme)))

                .setConnectionUsername(this.scpsConfig.get(ConfigKeys.elasticsearch_username))
                .setConnectionPassword(this.scpsConfig.get(ConfigKeys.elasticsearch_password))

                .setSocketTimeout(this.scpsConfig.getInteger(ConfigKeys.elasticsearch_socket_time_out))

                .setConnectionRequestTimeout(this.scpsConfig.getInteger(ConfigKeys.elasticsearch_connection_request_time_out))

                .setConnectionTimeout(this.scpsConfig.getInteger(ConfigKeys.elasticsearch_connect_time_out))

                .setEmitter(emitter)
                .build();
    }
}
