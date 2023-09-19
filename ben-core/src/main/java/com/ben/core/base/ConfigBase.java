package com.ben.core.base;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;

/**
 * @author learn
 * @date 2023/1/29 11:12
 */
public abstract class ConfigBase implements Serializable {

    protected final Configuration scpsConfig;
    protected final Log log = LogFactory.get();

    protected ConfigBase(Configuration scpsConfig){
        this.scpsConfig = scpsConfig;
    }
}
