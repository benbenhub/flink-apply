package com.ben.core.base;

import com.ben.core.util.ConfigPropertiesUtil;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class AppBase implements Serializable {

    //App名称
    private String jobName;
    //失败重启策略
    private RestartStrategies.RestartStrategyConfiguration restartStrategyConfiguration;
    private ParameterTool scpsParams;
    private Configuration scpsConfig;
    protected Logger log = LoggerFactory.getLogger(getClass());

    public abstract void runApp();
//    public abstract void init(String[] args);
    public abstract void execute() throws Exception;


    public AppBase(String[] args, String jobName){

        this.jobName =jobName;
        initConfig(args);

    }
    public AppBase(String[] args){
        initConfig(args);
    }
    public void start() throws Exception {
        runApp();
        execute();
    }
    public void start(String jobName,String[] args) throws Exception {
        this.jobName =jobName;
        initConfig(args);
        runApp();
        execute();
    }
    /**
     * 解析命令行参数，目前仅需要解析 -conf参数获取外部配置文件
     * @param args 命令行参数
     *
     * */
    private void initConfig(String[] args){
        try {
            //初始化全局配置
            scpsParams = ConfigPropertiesUtil.initConfig(args);
            scpsConfig = scpsParams.getConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(jobName == null || jobName.equals("")){
            jobName = scpsParams.get("flink_jobname","default_flink_job");
        }
    }
    public String getJobName() {
        return jobName;
    }

    public ParameterTool getScpsParams(){
        return scpsParams;
    }
    public Configuration getScpsConfig(){
        return scpsConfig;
    }

    public RestartStrategies.RestartStrategyConfiguration getRestartStrategyConfiguration() {
        return restartStrategyConfiguration;
    }


    public void setRestartStrategyConfiguration(RestartStrategies.RestartStrategyConfiguration restartStrategyConfiguration) {
        this.restartStrategyConfiguration = restartStrategyConfiguration;
    }
}
