# flink-apply


### 使用教程

#### 批次程序

~~~
public class Batch extends BatchApp {

    public Batch(String[] args, String jobName) {
        super(args, jobName);
    }
    
    @Override
    public void run(ExecutionEnvironment batchEnv) {

        source().jdbcSource(JdbcConnectionType.clickhouse, "sql");
        ...
        ...
        ...
        sink().elasticsearchSink(sz, "");
       
    }
}
~~~

#### 流式程序
~~~
public class Stream extends StreamApp {

    public Stream(String[] args, String jobName) {
        super(args, jobName);
    }
    public static void main(String[] args) throws Exception {
        new Stream(args, "app name").start();
    }
    @Override
    public void run(ExecutionEnvironment batchEnv) {

        DataStream<Map<String, Object>> source = source(
        ).kafkaSource(KafkaSource.<BinlogBean>builder(
                ).setBootstrapServers(getScpsConfig().get(ConfigKeys.kafka_bootstrap_servers)
                ).setTopics(OdsBasicsConf.odsTopic
                ).setGroupId(odsTopicGroup
                ).setStartingOffsets(StringUtils.toKafkaOffset(offset)
                ).setValueOnlyDeserializer(new KafkaDeserializerBinlog()
                ).build()
                , WatermarkStrategy.<BinlogBean>forMonotonousTimestamps(
                ).withTimestampAssigner((SerializableTimestampAssigner<BinlogBean>) (ob, l) -> ob.getTSMS())
                // 过滤掉不需要的表数据
        ).flatMap(new SourceFlatMap()
        ).name("source adapter filter");
        ...
        ...
        ...
        sink().elasticsearchSink(sinkData, new ElasticsearchSink(log));
    }
}
~~~

#### 调用
~~~

flink run-application -t kubernetes-application -p $PARALIZE \
    -c $MAIN_CLASS \
    -Dkubernetes.cluster-id=$K8S_CLUSTER_NAME \
    -Djobmanager.memory.process.size=$JB_MEM \
    -Dtaskmanager.memory.process.size=$TM_MEM \
    -Dkubernetes.pod-template-file=$POD_TEMPLATE_PATH \
    -Drest.port=$PORT \
    $APP_IMAGE \
    $POD_TEMPLATE_OPTION \
    $FLINK_OPTION \
    local://$APP_PATH $APP_OPTION -flink_jobname $APP_NAME
~~~