package com.ben.core.deserializer;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.MapTypeInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author learn
 * @date 2022/4/7 18:19
 */
public class KafkaDeserializerMap implements DeserializationSchema<Map<String,Object>> {
    @Override
    public Map<String, Object> deserialize(byte[] bytes) {
        Map<String, Object> result;
        String byteStr = new String(bytes, StandardCharsets.UTF_8);
        try {
            result = JSONObject.parseObject(byteStr, Map.class);
        } catch (Exception e){
            result = new HashMap<>();
            result.put("garbage_data","1111111");
        }
        return result;
    }

    @Override
    public boolean isEndOfStream(Map<String, Object> stringObjectMap) {
        return false;
    }

    @Override
    public TypeInformation<Map<String, Object>> getProducedType() {
        return new MapTypeInfo<>(BasicTypeInfo.STRING_TYPE_INFO,TypeInformation.of(Object.class));
    }
}
