package com.ben.core.base;

import com.ben.core.constance.ConfigKeys;
import com.ben.core.factory.StreamSinkFactory;
import com.ben.core.factory.StreamSourceFactory;
import com.ben.core.util.DateTimeUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Map;

/**
 * @author learn
 * @date 2022/6/24 17:15
 */
public abstract class StreamApp extends AppBase {

    /**
     * 批次执行环境
     */
    private transient StreamExecutionEnvironment streamEnv;

    private StreamSinkFactory sink;
    private StreamSourceFactory source;

    public abstract void run(StreamExecutionEnvironment streamEnv);

    public StreamApp(String[] args, String jobName){
        super(args, jobName);
        initStreamEnv();
    }

    public StreamApp(String[] args){
        this(args,null);
    }

    /**
     * 初始化批次执行环境，如果设置了重启策略测已设置为准
     * */
    private void initStreamEnv(){
        streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
//        String jobId = getScpsParams().get("flink_jobid",null);
//        if(TextUtils.isEmpty(jobId)){
//            streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
//        } else {
//            //外部赋值jobID 必须是32位
//            Configuration c = new Configuration();
//            c.setString(PipelineOptionsInternal.PIPELINE_FIXED_JOB_ID, jobId);
//            streamEnv = StreamExecutionEnvironment.getExecutionEnvironment(c);
//        }

        streamEnv.getConfig().setGlobalJobParameters(getScpsConfig());

        sink = new StreamSinkFactory(getScpsConfig());
        source = new StreamSourceFactory(getScpsConfig(),getStreamEnv());

        if (null != getRestartStrategyConfiguration()){
            streamEnv.setRestartStrategy(getRestartStrategyConfiguration());
        }
    }

    @Override
    public void runApp() {
        run(getStreamEnv());
    }

    @Override
    public void execute() throws Exception {
        getStreamEnv().execute(getJobName());
    }

    public StreamExecutionEnvironment getStreamEnv() {
        return streamEnv;
    }

    public StreamSinkFactory sink(){
        return sink;
    }
    public StreamSourceFactory source(){
        return source;
    }

    public void setupCheckpoint() {
        //Flink-CDC将读取binlog的位置信息以状态的方式保存在CK,如果想要做到断点续传,需要从Checkpoint或者Savepoint启动程序
        //触发checkpoint的时间周期
        getStreamEnv().enableCheckpointing(getScpsConfig().getLong(ConfigKeys.checkpoint_enablecheckpointing));

        // 设置checkpoint的周期
        getStreamEnv().getCheckpointConfig().setCheckpointInterval(getScpsConfig().getLong(ConfigKeys.checkpoint_interval));

        //checkpoint最小间隔
        getStreamEnv().getCheckpointConfig().setMinPauseBetweenCheckpoints(
                getScpsConfig().getLong(ConfigKeys.checkpoint_minPauseBetweenCheckpoints));
        //超过时间则丢弃
        getStreamEnv().getCheckpointConfig().setCheckpointTimeout(
                getScpsConfig().getLong(ConfigKeys.checkpoint_timeout));
        //并发次数
        getStreamEnv().getCheckpointConfig().setMaxConcurrentCheckpoints(
                getScpsConfig().getInteger(ConfigKeys.checkpoint_maxConcurrent));

        // 指定CK的一致性语义
        switch (getScpsParams().get(ConfigKeys.checkpoint_mode.key(),ConfigKeys.checkpoint_mode.defaultValue().toString())){
            case "AT_LEAST_ONCE":
                getStreamEnv().getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
            case "EXACTLY_ONCE":
                getStreamEnv().getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        }
        //启用未对齐的检查点，这将大大减少背压下的检查点时间。
        getStreamEnv().getCheckpointConfig().enableUnalignedCheckpoints(getScpsConfig().getBoolean(ConfigKeys.checkpoint_enableUnaligned));

        //启用未对齐的检查点后，还可以通过编程方式指定对齐的检查点超时
        //env.getCheckpointConfig().setAlignedCheckpointTimeout(Duration.ofMinutes(5));
        // 设置任务关闭的时候保留最后一次CK数据
        getStreamEnv().getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 指定从CK自动重启策略
        getStreamEnv().setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                        getScpsConfig().getInteger(ConfigKeys.checkpoint_fixedRestartNumber)
                        , getScpsConfig().getLong(ConfigKeys.checkpoint_fixedRestartDelay)));
        // 设置状态后端
        getStreamEnv().setStateBackend(new FsStateBackend(
                getScpsConfig().get(ConfigKeys.checkpoint_backend) + getJobName()));

//        getStreamEnv().getConfig().setTaskCancellationTimeout(0);
    }
    /**
     *
     * @param source 数据
     * @param field 时间戳字段
     */
    public DataStream<Map<String,Object>> setupWatermarks(DataStream<Map<String,Object>> source, String field){
        return source.assignTimestampsAndWatermarks(
            WatermarkStrategy.<Map<String,Object>>forMonotonousTimestamps().withTimestampAssigner(
                    (SerializableTimestampAssigner<Map<String, Object>>) (map, l) -> {
                        if (map.containsKey(field) && Long.parseLong(map.get(field).toString()) > 0) {
                            return Long.parseLong(map.get(field).toString());
                        } else {
                            //获取系统时间戳
                            return DateTimeUtils.getTimeStamp();
                        }
                    }));
    }

    public WatermarkStrategy<Map<String,Object>> setupWatermarks(String field){
        return WatermarkStrategy.<Map<String,Object>>forMonotonousTimestamps().withTimestampAssigner(
                (SerializableTimestampAssigner<Map<String, Object>>) (map, l) -> {
                    if (map.containsKey(field) && Long.parseLong(map.get(field).toString()) > 0) {
                        return Long.parseLong(map.get(field).toString());
                    } else {
                        //获取系统时间戳
                        return DateTimeUtils.getTimeStamp();
                    }
                }
        );
    }
}
