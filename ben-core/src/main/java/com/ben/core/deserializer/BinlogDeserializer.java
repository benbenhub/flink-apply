package com.ben.core.deserializer;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.enums.SqlOperate;
import com.ben.core.modul.BinlogBean;
import com.ben.core.util.DateTimeUtils;
import com.ben.core.util.StringUtils;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BinlogDeserializer implements DebeziumDeserializationSchema<BinlogBean> {

    /**
     * 封装的数据格式
     * {
     * "database":"",
     * "tableName":"",
     * "before":{"id":"","tm_name":""....},
     * "after":{"id":"","tm_name":""....},
     * "type":"c u d",
     * //"ts":156456135615
     * }
     */
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<BinlogBean> collector) throws ParseException {
        String topic = sourceRecord.topic();
        // ddl的topic不包含库名和表名
        String[] fields = topic.split("\\.");

        String key = sourceRecord.keySchema().fields().get(0).name();
        Struct value = (Struct) sourceRecord.value();
        if (fields.length > 1) {
            // Dml 解析
            Envelope.Operation operation = Envelope.operationFor(sourceRecord);
            String type = operation.toString().toLowerCase();
            collector.collect(analysisDml(fields, key, value, type));
        } else {
            // ddl解析
            collector.collect(analysisDdl(value));
        }
    }

    @Override
    public TypeInformation<BinlogBean> getProducedType() {
        return TypeInformation.of(BinlogBean.class);
    }

    private BinlogBean analysisDml(String[] fields,String key,Struct value,String type) throws ParseException {

        BinlogBean resultRow = new BinlogBean();
        String database = fields[1];
        String tableName = fields[2];

        Struct before = value.getStruct("before");

        Map<String,Object> beforeRow = new HashMap<>();
        if (before != null) {
            Schema beforeSchema = before.schema();
            List<Field> beforeFields = beforeSchema.fields();
            for (Field field : beforeFields) {
                Object beforeValue = before.get(field);
                if(field.schema().name() != null && field.schema().name().contains("io.debezium.time"))
                    beforeValue = StringUtils.isNumeric(beforeValue.toString())
                            ? beforeValue
                            : DateTimeUtils.dateToStamp(beforeValue.toString());

                //8 * 60 * 60 * 1000
                if(field.schema().name() != null
                        && field.schema().name().equals("io.debezium.time.Timestamp")
                        && field.schema().type().name().equals("INT64"))
                    beforeValue = (long)beforeValue - (8 * 60 * 60 * 1000);

                beforeRow.put(field.name(), beforeValue);
            }
        }

        //4.获取"after"数据
        Struct after = value.getStruct("after");
        Map<String,Object> afterRow = new HashMap<>();
        if (after != null) {
            Schema afterSchema = after.schema();
            List<Field> afterFields = afterSchema.fields();
            for (Field field : afterFields) {

                Object afterValue = after.get(field);
                if(field.schema().name() != null && field.schema().name().contains("io.debezium.time"))
                    afterValue = StringUtils.isNumeric(afterValue.toString())
                            ? afterValue
                            : DateTimeUtils.dateToStamp(afterValue.toString());
                if(field.schema().name() != null
                        && field.schema().name().equals("io.debezium.time.Timestamp")
                        && field.schema().type().name().equals("INT64"))
                    afterValue = (long)afterValue - (8 * 60 * 60 * 1000);

                afterRow.put(field.name(), afterValue);
            }
        }

        //5.获取操作类型  CREATE UPDATE DELETE
        if ("create".equals(type)) {
            type = "insert";
        }
        long ts_ms = value.getInt64("ts_ms");

        resultRow.setDatabase(database);
        resultRow.setTableName(tableName);
        resultRow.setDataBefore(beforeRow);
        resultRow.setDataAfter(afterRow);
        resultRow.setOperationType(SqlOperate.getByName(type));
        resultRow.setSqlLanguageType(SqlOperate.dml);
        resultRow.setTSMS(ts_ms);
        resultRow.setTableKey(key);
        resultRow.setDdl("");

        return resultRow;
    }
    private BinlogBean analysisDdl(Struct value) {
        BinlogBean resultRow = new BinlogBean();
        Struct source = value.getStruct("source");

        Map<String,Object> historyRecord = JSONObject.parseObject(value.getString("historyRecord"), Map.class);

        resultRow.setDatabase(source.getString("db"));
        resultRow.setTableName(source.getString("table"));
        resultRow.setDdl(Objects.toString(historyRecord.get("ddl"), "" ));
        resultRow.setSqlLanguageType(SqlOperate.ddl);
        resultRow.setTSMS(source.getInt64("ts_ms"));
        resultRow.setTableKey("");
        resultRow.setDataAfter(new HashMap<>());
        resultRow.setDataBefore(new HashMap<>());
        resultRow.setOperationType(SqlOperate.ddl);

        return resultRow;
    }

}
