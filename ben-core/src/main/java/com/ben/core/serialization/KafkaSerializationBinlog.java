package com.ben.core.serialization;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.modul.BinlogBean;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author learn
 * @date 2022/4/8 17:22
 */
public class KafkaSerializationBinlog implements SerializationSchema<BinlogBean> {
    @Override
    public void open(InitializationContext context) throws Exception {

    }

    @Override
    public byte[] serialize(BinlogBean b) {
        Map<String, Object> map = new HashMap<>();
        map.put("data_after", b.getDataAfter());
        map.put("data_before", b.getDataBefore());
        map.put("database", b.getDatabase());
        map.put("table_name", b.getTableName());
        map.put("table_key", b.getTableKey());
        map.put("operation_type", b.getOperationType());
        map.put("sql_language_type", b.getSqlLanguageType());
        map.put("ts_ms", b.getTSMS());
        map.put("ddl", b.getDdl());

        return JSONObject.toJSON(map).toString().getBytes();
    }
}
