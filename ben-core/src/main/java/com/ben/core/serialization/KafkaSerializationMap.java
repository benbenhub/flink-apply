package com.ben.core.serialization;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.util.Map;

/**
 * @author learn
 * @date 2022/4/8 17:22
 */
public class KafkaSerializationMap implements SerializationSchema<Map<String,Object>> {
    @Override
    public void open(InitializationContext context) throws Exception {

    }

    @Override
    public byte[] serialize(Map<String, Object> stringObjectMap) {
        return JSONObject.toJSON(stringObjectMap).toString().getBytes();
    }
}
