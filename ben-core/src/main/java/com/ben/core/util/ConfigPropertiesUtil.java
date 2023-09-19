package com.ben.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author learn
 * @date 2022/3/18 18:07
 */
@Slf4j
public class ConfigPropertiesUtil {

    public static ParameterTool initConfig(String[] args) throws Exception {

        ParameterTool argsParam = ParameterTool.fromArgs(args);

        String confPath = argsParam.get("conf",System.getProperty("user.dir") + "/scps.properties");

        // 防止中文乱码
        Properties props = new Properties();
        InputStream inputStream = new FileInputStream(confPath);
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        props.load(bf);
        ParameterTool scpsParams = ParameterTool.fromMap((Map) props).mergeWith(argsParam);
        /*ParameterTool scpsParams = ParameterTool.fromPropertiesFile(in)
                // 获取main函数传入的配置文件
                .mergeWith(argsParam);*/

        // 获取系统环境变量
        ParameterTool sysEnvParams = ParameterTool.fromMap(System.getenv());

        Map<String, String> mapParams = new HashMap<>(scpsParams.toMap());
        // 环境变量解析赋值
        for(Map.Entry<String,String> entry : scpsParams.toMap().entrySet()){
            String val = entry.getValue();
            // 值 开头${结尾}视为参数从环境变量获取
            if( Pattern.matches("^(\\$\\{)[\\s\\S]*(\\})$", val) ){
                val = val.substring(2,val.length()-1);
                mapParams.put(entry.getKey(), sysEnvParams.get(val));
            }
        }

        return ParameterTool.fromMap(mapParams);
    }
}
