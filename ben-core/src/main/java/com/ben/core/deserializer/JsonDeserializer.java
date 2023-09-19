package com.ben.core.deserializer;


import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;


public class JsonDeserializer<T> implements DeserializationSchema<T> {

    Class<T> tClass;
    public JsonDeserializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public T deserialize(byte[] message) {
        String byteStr = new String(message, StandardCharsets.UTF_8);
        T object = null;
        try {
            object = JSONObject.parseObject(byteStr, tClass);

        } catch (Exception e){
            LoggerFactory.getLogger(getClass()).info("序列化失败消息：{}", byteStr);
        }
        return object;
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation getProducedType() {
        return TypeInformation.of(tClass);
    }
}
