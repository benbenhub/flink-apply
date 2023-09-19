package com.ben.core.deserializer;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.enums.SqlOperate;
import com.ben.core.modul.OplogBean;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.ConverterConfig;
import org.apache.kafka.connect.storage.ConverterType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author learn
 * @date 2023/6/6 14:36
 */
public class OplogDeserializer implements DebeziumDeserializationSchema<OplogBean> {

    private transient JsonConverter jsonConverter;
    private final Boolean includeSchema;

    public OplogDeserializer() {
        this(false);
    }

    public OplogDeserializer(Boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    @Override
    public void deserialize(SourceRecord record, Collector<OplogBean> out) throws Exception {
        if (jsonConverter == null) {
            initializeJsonConverter();
        }
        byte[] bytes =
                jsonConverter.fromConnectData(record.topic(), record.valueSchema(), record.value());
        Map<String, Object> map = JSONObject.parseObject(new String(bytes), Map.class);
        OplogBean ob = new OplogBean();
        ob.setTSMS(Long.parseLong(map.get("ts_ms").toString()));
        ob.setCollection(((JSONObject)map.get("ns")).getString("coll"));
        ob.setDatabase(((JSONObject)map.get("ns")).getString("db"));
        ob.setOperationType(SqlOperate.getByName(map.get("operationType").toString()));
        ob.setUpdateDescription((JSONObject) map.get("updateDescription"));
        ob.setFullDocument(Objects.toString(map.get("fullDocument")));
        ob.setDocumentKey(map.get("documentKey").toString());
        out.collect(ob);
    }

    @Override
    public TypeInformation<OplogBean> getProducedType() {
        return TypeInformation.of(OplogBean.class);
    }

    private void initializeJsonConverter() {
        jsonConverter = new JsonConverter();
        final HashMap<String, Object> configs = new HashMap<>(2);
        configs.put(ConverterConfig.TYPE_CONFIG, ConverterType.VALUE.getName());
        configs.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, includeSchema);
        jsonConverter.configure(configs);
    }
}
