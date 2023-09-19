package com.ben.core.base;

import com.ben.core.factory.BatchSinkFactory;
import com.ben.core.factory.BatchSourceFactory;
import org.apache.flink.api.java.ExecutionEnvironment;

public abstract class BatchApp extends AppBase{

    /**
     * 批次执行环境
     */
    private transient ExecutionEnvironment batchEnv;
    private BatchSinkFactory sink;
    private BatchSourceFactory source;

    public abstract void run(ExecutionEnvironment batchEnv);

    public BatchApp(String[] args){
        this(args,null);
    }
    public BatchApp(String[] args,String jobName){
        super(args, jobName);
        initBatchEnv();
    }
    /**
     * 初始化批次执行环境，如果设置了重启策略测已设置为准
     * */
    private void initBatchEnv(){
        batchEnv = ExecutionEnvironment.getExecutionEnvironment();
//        String jobId = ConfigPropertiesUtil.getConfig("flink_jobid",null);
//        if(jobId.equals("")){
//            batchEnv = ExecutionEnvironment.getExecutionEnvironment();
//        } else {
//            //外部赋值jobID 必须是32位
//            Configuration c = new Configuration();
//            c.setString(PipelineOptionsInternal.PIPELINE_FIXED_JOB_ID, jobId);
//            batchEnv = ExecutionEnvironment.getExecutionEnvironment();
//        }

        batchEnv.getConfig().setGlobalJobParameters(getScpsConfig());

        source = new BatchSourceFactory(getScpsConfig(),getBatchEnv());
        sink = new BatchSinkFactory(getScpsConfig());

        if (null != getRestartStrategyConfiguration()){
            batchEnv.setRestartStrategy(getRestartStrategyConfiguration());
        }
    }

    @Override
    public void runApp() {
        run(getBatchEnv());
    }

    @Override
    public void execute() throws Exception {
        if(getBatchEnv().getLastJobExecutionResult() == null){
            getBatchEnv().execute(getJobName());
        }
    }

    public ExecutionEnvironment getBatchEnv() {
        return batchEnv;
    }

    public BatchSinkFactory sink(){
        return sink;
    }
    public BatchSourceFactory source(){
        return source;
    }


}
