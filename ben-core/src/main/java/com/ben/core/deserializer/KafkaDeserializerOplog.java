package com.ben.core.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ben.core.enums.SqlOperate;
import com.ben.core.modul.OplogBean;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @author learn
 * @date 2023/6/6 14:49
 */
public class KafkaDeserializerOplog implements DeserializationSchema<OplogBean> {

    @Override
    public OplogBean deserialize(byte[] message) throws IOException {
        String byteStr = new String(message, StandardCharsets.UTF_8);
        Map<String, Object> map = JSONObject.parseObject(byteStr, Map.class);

        OplogBean b = new OplogBean();

        b.setCollection(Objects.toString(map.get("collection"),""));
        b.setDatabase(Objects.toString(map.get("database"),""));
        b.setOperationType(SqlOperate.getByName(Objects.toString(map.get("operation_type"),"")));
        b.setTSMS(Long.valueOf(Objects.toString(map.get("ts_ms"),"")));
        b.setDocumentKey(Objects.toString(map.get("document_key"),""));
        b.setFullDocument(Objects.toString(map.get("full_document"),""));
        b.setUpdateDescription(JSON.parseObject(Objects.toString(map.get("update_description"),"")) );

        return b;
    }

    @Override
    public boolean isEndOfStream(OplogBean nextElement) {
        return false;
    }

    @Override
    public TypeInformation<OplogBean> getProducedType() {
        return TypeInformation.of(OplogBean.class);
    }
}
