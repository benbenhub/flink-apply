# flink-apply
## 简介
对Flink的连接器进行二次封装，减少学习Flink的成本，可以使用Flink进行快速的数据开发。

## 使用教程

### 批次程序

~~~
public class Batch extends BatchApp {

    public Batch(String[] args, String jobName) {
        super(args, jobName);
    }
    
    @Override
    public void run(ExecutionEnvironment batchEnv) {

        DataSet<Row> source = source().jdbcSource(JdbcConnectionType.clickhouse, "sql");
        ...
        ...
        ...
        sink().elasticsearchSink(source, "");
       
    }
}
~~~

### 流式程序
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
        sink().elasticsearchSink(source, new ElasticsearchSink(log));
    }
}
~~~


~~~

flink run-application -t kubernetes-application \
    -p 1 \
    -c com.*.App \
    -Drest.port=59690 \
    -Dkubernetes.cluster-id=batch-name \
    -Djobmanager.memory.process.size=1024m  \
    -Dtaskmanager.memory.process.size=4096m  \
    -Dkubernetes.pod-template-file=defaultPodTemplete.yaml \
    -Dkubernetes.container.image=image:v1 \
    local:///opt/batch.jar -conf batch.properties -args 1003989321957234944

~~~
