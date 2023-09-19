package com.ben.core.conf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * @author 94391
 */
public class JacksonConfig {

    private static ObjectMapper objectMapper;

    private JacksonConfig() {
    }

    public static ObjectMapper getMapper() {
        if (Objects.isNull(objectMapper)){
            objectMapper = new ObjectMapper();
            //反序列化的时候如果多了其他属性,不抛出异常
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //如果是空对象的时候,不抛异常
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            //属性为null的转换
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            //取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        }
        return objectMapper;
    }
}
