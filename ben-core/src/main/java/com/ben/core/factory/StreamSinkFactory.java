package com.ben.core.factory;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import com.ben.core.connector.elasticsearch7.Elasticsearch7SinkFormat;
import com.ben.core.connector.jdbc.JdbcSinkFormat;
import com.ben.core.enums.DBInfo;
import com.ben.core.enums.JdbcConnectionType;
import com.ben.core.modul.EsBundle;
import com.getindata.connectors.http.HttpSink;
import com.getindata.connectors.http.SchemaLifecycleAwareElementConverter;
import com.getindata.connectors.http.internal.sink.HttpSinkRequestEntry;
import com.ben.core.base.ConfigBase;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.shaded.guava30.com.google.common.base.CaseFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;
import org.apache.http.util.TextUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author learn
 * @date 2022/3/18 16:38
 */
public class StreamSinkFactory extends ConfigBase  {

    public StreamSinkFactory(Configuration scpsConfig) {
         super(scpsConfig);
    }

    public void jdbcRowSink(JdbcConnectionType sourceType, DataStream<Row> r, String sql, JdbcStatementBuilder<Row> jsb) {
        r.addSink(new JdbcSinkFormat(this.scpsConfig).createJdbcSink(sql,jsb,sourceType));
    }

    public <T> void jdbcSink(JdbcConnectionType sourceType,DataStream<T> r, Class<T> tClass) {
        Tuple2<String, Object[]> t2 = buildSql(tClass);

        r.addSink(new JdbcSinkFormat(this.scpsConfig).createJdbcSink(t2.f0,buildStatement(t2.f1),sourceType));
    }

    public <T> void jdbcSink(JdbcConnectionType sourceType,DataStream<T> r, String sql, JdbcStatementBuilder<T> jsb) {
        r.addSink(new JdbcSinkFormat(this.scpsConfig).createJdbcSink(sql,jsb,sourceType));
    }

    public <T> void kafkaSink(DataStream<T> r,KafkaRecordSerializationSchema<T> krss) {

        String so = this.scpsConfig.get(ConfigKeys.kafka_deliver_guarantee);
        DeliveryGuarantee dg;

        switch (so) {
            case "exactly-once":
                dg = DeliveryGuarantee.EXACTLY_ONCE;
                break;
            case "none":
                dg = DeliveryGuarantee.NONE;
                break;
            case "at-least-once":
            default:
                dg = DeliveryGuarantee.AT_LEAST_ONCE;
                break;
        }

        KafkaSink<T> sink = KafkaSink.<T>builder()
                .setBootstrapServers(this.scpsConfig.get(ConfigKeys.kafka_bootstrap_servers))
                .setRecordSerializer(krss)
                .setDeliverGuarantee(dg)
                .build();

        r.sinkTo(sink);
    }

    public <T> void elasticsearchSink(DataStream<T> r, String index) {
        elasticsearchSink(r,(element, context, indexer) -> {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(element);
            indexer.add(Requests.indexRequest()
                    .index(index)
                    .source(XContentType.JSON, jsonObject.toString()));
        });
    }

    public <T> void elasticsearchSink(DataStream<EsBundle<T>> r) {
        elasticsearchSink(r,(element, context, indexer) -> {
            switch (element.getAction()){
                case INSERT:
                    IndexRequest indexRequest = Requests.indexRequest();
                    if (element.getId()!=null) {
                        indexRequest = indexRequest.id(element.getId());
                    }
                    String json = JSONObject.toJSONString(element.getData());
                    indexer.add(indexRequest.index(element.getIndex()).source(json, XContentType.JSON));
                    break;
                case DELETE:
                    DeleteRequest deleteRequest = Requests.deleteRequest(element.getIndex());
                    if (element.getId()!=null) {
                        deleteRequest = deleteRequest.id(element.getId());
                    }else {
                        log.error("删除ES索引必须指定 ID !!,已抛弃此条数据。"+element);
                    }
                    indexer.add(deleteRequest);
                    break;
            }

        });
    }

    public <T> void elasticsearchSink(DataStream<T> r , ElasticsearchEmitter<? super T> emitter) {
        r.sinkTo(new Elasticsearch7SinkFormat(this.scpsConfig).createEs7Sink(emitter));
    }

    private <T> JdbcStatementBuilder<T> buildStatement(Object[] methNames) {
        return (preparedStatement, t) -> {
            try {
                setPs(preparedStatement, methNames, t);
            } catch (IllegalAccessException e) {
                throw new SQLException(e);
            }
        };
    }

    public void HttpSink(DataStream<String> r, String url, String method, Properties properties) {
        r.sinkTo(HttpSink.<String>builder()
            .setEndpointUrl(url)
            .setElementConverter(new SchemaLifecycleAwareElementConverter<String, HttpSinkRequestEntry>() {
                @Override
                public void open(Sink.InitContext initContext) {

                }

                @Override
                public HttpSinkRequestEntry apply(String s, SinkWriter.Context context) {
                    //"POST"
                    return new HttpSinkRequestEntry(method, s.getBytes(StandardCharsets.UTF_8));
                }
            })
            .setProperties(properties)
            .build()
        );
    }

    /**
     * 用于构建sql的方法
     *
     * @param tClass     bean类的class
     */
    private <T> Tuple2<String, Object[]> buildSql(Class<T> tClass) {
        //通过注解获取数据库与table信息
        String database = AnnotationUtil.getAnnotationValue(tClass, DBInfo.class, "database");
        String table = AnnotationUtil.getAnnotationValue(tClass, DBInfo.class, "table");
        //信息不完整报错
        if (TextUtils.isEmpty(database) || TextUtils.isEmpty(table)){
            throw new IllegalArgumentException("bean类必须包含database与table的值");
        }
        //字段名
        StringBuilder filedBuilder = new StringBuilder();
        //值，用？替代
        StringBuilder vauleBuilder = new StringBuilder();
        //获取所有方法名，过滤除get以外所有方法
        Method[] methods = ReflectUtil.getMethods(tClass);
        List<String> methNames = new ArrayList<>();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && !name.equals("getClass")) {
                String low_case = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name.replace("get", ""));
                filedBuilder.append(low_case).append(",");
                vauleBuilder.append("?,");
                methNames.add(name);
            }
        }
        //拼接SQL
        String filedSql = filedBuilder.substring(0, filedBuilder.length() - 1);
        String valueSql = vauleBuilder.substring(0, vauleBuilder.length() - 1);
        String sql = "insert into " +
                database +
                "." +
                table +
                " (" +
                filedSql +
                ") values (" +
                valueSql +
                ")";

        log.info("解析之后的SQL语句为: {}",sql);

        return Tuple2.of(sql, methNames.toArray());
    }

    /**
     * 用于设置 PreparedStatement的通用方法
     *
     * @param ps     PreparedStatement实例
     * @param fields 通过”实例对象.getClass().getDeclaredFields()“获得
     * @param bean   实例对象
     * @throws IllegalAccessException field.get抛出的错误
     * @throws SQLException           ps.set抛出的错误
     */
    private void setPs(PreparedStatement ps, Object[] fields, Object bean) throws IllegalAccessException, SQLException {

        for (int i = 1; i <= fields.length; i++) {
            //反射所有filed方法获取值，注意顺序不要错
            Object o = ReflectUtil.invoke(bean, fields[i-1].toString());
            //空值
            if (o == null) {
                ps.setNull(i, 0);
                continue;
            }
            //字段为空
            String fieldValue = o.toString();
            String NA = "null";
            if (!NA.equals(fieldValue) && !"".equals(fieldValue)) {
                ps.setObject(i, fieldValue);
            } else {
                //正常负值
                ps.setNull(i, 0);
            }
        }
    }
}
