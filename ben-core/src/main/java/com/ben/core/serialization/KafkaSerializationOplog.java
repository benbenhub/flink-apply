package com.ben.core.serialization;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.modul.OplogBean;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author learn
 * @date 2023/6/6 14:33
 */
public class KafkaSerializationOplog implements SerializationSchema<OplogBean> {
    @Override
    public void open(InitializationContext context) throws Exception {

    }

    @Override
    public byte[] serialize(OplogBean element) {
        Map<String, Object> map = new HashMap<>();
        map.put("database", element.getDatabase());
        map.put("collection", element.getCollection());
        map.put("operation_type", element.getOperationType().getName());
        map.put("ts_ms", element.getTSMS());
        map.put("document_key", element.getDocumentKey());
        map.put("full_document", element.getFullDocument());
        map.put("update_description", element.getUpdateDescription());
        return JSONObject.toJSON(map).toString().getBytes();

    }
}
