package com.ben.core.factory;

import com.ben.core.connector.cdc.FlinkcdcConnectorFormat;
import com.ben.core.deserializer.OplogDeserializer;
import com.ben.core.modul.BinlogBean;
import com.ben.core.modul.OplogBean;
import com.ben.core.util.DateTimeUtils;
import com.ben.core.base.ConfigBase;
import com.ben.core.deserializer.BinlogDeserializer;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamSourceFactory extends ConfigBase {

    private static StreamExecutionEnvironment env;

    public StreamSourceFactory(Configuration scpsConfig, StreamExecutionEnvironment env) {
        super(scpsConfig);
        StreamSourceFactory.env = env;
    }

    public DataStream<BinlogBean> mysqlCdcSource(StartupOptions ss) {
        /*
        flink cdc改成新的api
        return env.addSource(new FlinkcdcConnectorFormat(this.scpsConfig).createMysqlCdc(ss, new BinlogDeserializerMap()));
         */
        return env.fromSource(new FlinkcdcConnectorFormat(this.scpsConfig).createMysqlCdcNew(ss, new BinlogDeserializer())
            , WatermarkStrategy.<BinlogBean>forMonotonousTimestamps().withTimestampAssigner(
                (SerializableTimestampAssigner<BinlogBean>) (ob, l) -> {
                    if (ob.getTSMS() == null || ob.getTSMS() == 0) {
                        return DateTimeUtils.getTimeStamp(); // 获取系统时间戳
                    } else {
                        return ob.getTSMS();
                    }
                }
            )
            , "scps binlog source");
    }

    public <T> DataStream<T> kafkaSource(KafkaSource<T> source, WatermarkStrategy<T> wm) {
        return env.fromSource(source, wm, "scps kafka source");
    }

    public DataStream<OplogBean> mongodbCdcSource() {
        return env.fromSource(
            new FlinkcdcConnectorFormat(this.scpsConfig).createMongodbCdc(new OplogDeserializer())
            , WatermarkStrategy.<OplogBean>forMonotonousTimestamps().withTimestampAssigner(
                (SerializableTimestampAssigner<OplogBean>) (ob, l) -> {
                    if (ob.getTSMS() == null || ob.getTSMS() == 0) {
                        return DateTimeUtils.getTimeStamp();//获取系统时间戳
                    } else {
                        return ob.getTSMS();
                    }
                }
            )
            , "scps oplog source");
    }

}
