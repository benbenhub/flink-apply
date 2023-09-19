package com.ben.core.deserializer;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.enums.SqlOperate;
import com.ben.core.modul.BinlogBean;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author learn
 * @date 2023/5/26 16:30
 */
public class KafkaDeserializerBinlog implements DeserializationSchema<BinlogBean> {

    @Override
    public BinlogBean deserialize(byte[] message) throws IOException {
        String byteStr = new String(message, StandardCharsets.UTF_8);
        Map<String, Object> map = JSONObject.parseObject(byteStr, Map.class);

        BinlogBean b = new BinlogBean();

        b.setDataAfter(JSONObject.parseObject(map.get("data_after").toString(), Map.class));
        b.setDataBefore(JSONObject.parseObject(map.get("data_before").toString(), Map.class));
        b.setDatabase(map.get("database").toString());
        b.setTableName(map.get("table_name").toString());
        b.setTableKey(map.get("table_key").toString());
        b.setOperationType(SqlOperate.getByName(map.get("operation_type").toString()));
        b.setSqlLanguageType(SqlOperate.getByName(map.get("sql_language_type").toString()));
        b.setTSMS(Long.valueOf(map.get("ts_ms").toString()));
        b.setDdl(map.get("ddl").toString());

        return b;
    }

    @Override
    public boolean isEndOfStream(BinlogBean nextElement) {
        return false;
    }

    @Override
    public TypeInformation<BinlogBean> getProducedType() {
        return TypeInformation.of(BinlogBean.class);
    }
}
